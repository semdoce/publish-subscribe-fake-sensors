package mediator;

import sensors.Cardiaco;
import sensors.Pressao;
import sensors.Saturacao;
import sensors.Sensor;
import sensors.Temperatura;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import broker.RabbitMQConfig;

public class MonitorMediator implements SensorMediator {
    /* Mediator do sistema. Centraliza as regras que antes estavam 
    * distribuídas entre sensores e FacadeSensors.
    */

    private Channel channel;
    private final Sensor[] sensores;

    public MonitorMediator() {

        this.sensores = new Sensor[] {
            new Cardiaco(),
            new Pressao(),
            new Saturacao(),
            new Temperatura()
        };
    }

    // Inicia o monitoramento continuo dos sensores
    @Override
    public void checkup(String pacienteId) {
        // Método herdado do antigo FacadeSensors. Agora o MonitorMediator coordena todo o monitoramento.

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfig.HOST);

        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {

            this.channel = channel;

            channel.exchangeDeclare(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.EXCHANGE_TYPE);

            System.out.println("==================================================");
            System.out.println(" Monitor iniciado para: " + pacienteId.toUpperCase());
            System.out.println(" Exchange: " + RabbitMQConfig.EXCHANGE_NAME + " (topic)");
            System.out.println("==================================================\n");

            while (!Thread.currentThread().isInterrupted()) {

                String relatorio = "";
                int nivelGeral = 0; // 0 = rotina, 1 = atenção, 2 = crítico

                for (Sensor sensor : sensores) {

                    ResultadoAnalise resultado = processar(pacienteId, sensor);

                    relatorio += sensor.getDescricao()
                            + " - "
                            + resultado.getStatus()
                            + "\n";

                    if (resultado.getPrioridade().equals("critico")) {
                        nivelGeral = Math.max(nivelGeral, 2);
                    }
                    else if (resultado.getPrioridade().equals("atencao")) {
                        nivelGeral = Math.max(nivelGeral, 1);
                    }
                }

                ResultadoAnalise resultadoGeral = analiseGeral(relatorio, nivelGeral);

                System.out.println("\nANÁLISE GERAL DO PACIENTE");
                System.out.println(resultadoGeral.getStatus());
                System.out.println("Prioridade geral: " + resultadoGeral.getPrioridade());
                System.out.println("--------------------------------------------------");

                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Monitoramento interrompido.");
        } catch (Exception e) {
            System.err.println("Erro no monitoramento: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public ResultadoAnalise processar(String pacienteId, Sensor sensor) {
        // Substitui o antigo publicarLeitura() do Facade. Mede, analisa e publica a leitura no RabbitMQ.

        sensor.medir();

        ResultadoAnalise resultado = analisar(sensor);
        String mensagem = sensor.getDescricao() + " - " + resultado.getStatus();
        String routingKey = pacienteId + "." + sensor.getTipoSinal() + "." + resultado.getPrioridade();

        try {
            channel.basicPublish(RabbitMQConfig.EXCHANGE_NAME, routingKey, null, mensagem.getBytes("UTF-8"));
        } catch (Exception e) {
            System.err.println("Erro ao publicar mensagem no RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(" [Pub] " + routingKey + " -> " + mensagem);

        return resultado;
    }

    public ResultadoAnalise analiseGeral(String relatorio, int nivelGeral) {
        // Gera o estado geral do paciente a partir de todos os sensores.

        if (nivelGeral == 2) {
            return new ResultadoAnalise(relatorio, "critico");
        }
        else if (nivelGeral == 1) {
            return new ResultadoAnalise(relatorio, "atencao");
        }
        else {
            return new ResultadoAnalise(relatorio, "rotina");
        }
    }

    private ResultadoAnalise analisar(Sensor sensor) {
        // Centraliza as classificações que antes ficavam nos sensores.

        if (sensor instanceof Cardiaco) {
            Cardiaco cardiaco = (Cardiaco) sensor;
            return analisarCardiaco(cardiaco.getBpm());
        }
        
        if (sensor instanceof Pressao) {
            Pressao pressao = (Pressao) sensor;
            return analisarPressao(
                pressao.getSistolica(),
                pressao.getDiastolica()
            );
        }

        if (sensor instanceof Saturacao) {
            Saturacao saturacao = (Saturacao) sensor;
            return analisarSaturacao(saturacao.getSpo2());
        }

        if (sensor instanceof Temperatura) {
            Temperatura temperatura = (Temperatura) sensor;
            return analisarTemperatura(temperatura.getTemperatura());
        }

        return new ResultadoAnalise("Status Desconhecido","rotina");
    }

    private ResultadoAnalise analisarCardiaco(double bpm) {
        // Regra movida de Cardiaco.classificar().

        if (bpm > 140) {
            return new ResultadoAnalise("TAQUICARDIA SEVERA (Alerta Crítico)", "critico");
        } 
        else if (bpm > 100) {
            return new ResultadoAnalise("TAQUICARDIA (Batimento Elevado)", "atencao");
        }
        else if (bpm < 60) {
            return new ResultadoAnalise("BRADICARDIA (Batimento Baixo)", "critico");
        } 
        else {
            return new ResultadoAnalise("Batimento NORMAL", "rotina");
        }
    }

    private ResultadoAnalise analisarPressao(double sistolica, double diastolica) {
        // Regra movida de Pressao.classificar().

        if (sistolica >= 140 || diastolica >= 90) {
            return new ResultadoAnalise("HIPERTENSÃO", "critico");
        } 
        else if (sistolica < 90 || diastolica < 60) {
            return new ResultadoAnalise("Pressão BAIXA", "critico");
        } 
        else if (sistolica > 120 || diastolica > 80) {
            return new ResultadoAnalise("ELEVADA / PRÉ-HIPERTENSÃO", "atencao");
        } 
        else {
            return new ResultadoAnalise("Pressão NORMAL", "rotina");
        }
    }

    private ResultadoAnalise analisarSaturacao(double saturacao) {
        // Regra movida de Saturacao.classificar().

        if (saturacao >= 95) {
            return new ResultadoAnalise("Saturação NORMAL", "rotina");
        } 
        else if (saturacao >= 91) {
            return new ResultadoAnalise("HIPÓXIA LEVE (Atenção)", "atencao");
        } 
        else if (saturacao >= 81) {
            return new ResultadoAnalise("HIPÓXIA MODERADA (Alerta)", "atencao");
        } 
        else {
            return new ResultadoAnalise("HIPÓXIA SEVERA (Alerta Crítico)", "critico");
        }
    }
    
    private ResultadoAnalise analisarTemperatura(double temperatura) {
        // Regra movida de Temperatura.classificar().

        if (temperatura >= 37.8) {
            return new ResultadoAnalise("FEBRE / HIPERTERMIA (Alerta)", "critico");
        } 
        else if (temperatura >= 37.3) {
            return new ResultadoAnalise("ESTADO FEBRIL (Atenção)", "atencao");
        } 
        else if (temperatura < 35.0) {
            return new ResultadoAnalise("HIPOTERMIA (Alerta Crítico)", "critico");
        } 
        else {
            return new ResultadoAnalise("Temperatura NORMAL", "rotina");
        }
    }

}
