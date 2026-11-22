package mediator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import broker.RabbitMQConfig;
import sensors.Cardiaco;
import sensors.Pressao;
import sensors.Saturacao;
import sensors.Sensor;
import sensors.Temperatura;

public class MonitorMediator implements SensorMediator {
    /* Mediator do sistema.
     * Elimina a necessidade de Exchange no RabbitMQ:
     * 1. Consome das 4 filas dedicadas aos sensores (fila.cardiaco, fila.pressao, etc.)
     * 2. Aplica as regras de negocio e classificacao clinica (rotina, atencao, critico)
     * 3. Roteia diretamente para as filas dos monitores:
     *    - fila.enfermagem: recebe todas as medicoes
     *    - fila.medico.<paciente>: recebe as medicoes do paciente do medico
     *    - fila.alerta: recebe apenas leituras de prioridade critica
     */

    private Channel channel;

    @Override
    public void iniciar() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfig.HOST);

        try {
            Connection connection = factory.newConnection();
            this.channel = connection.createChannel();

            // Declara todas as filas (sem exchanges)
            RabbitMQConfig.inicializarFilas(channel);

            System.out.println("==================================================");
            System.out.println(" SERVICO MONITOR MEDIATOR (SEM EXCHANGES)");
            System.out.println(" Filas de Entrada (Sensores):");
            for (String fila : RabbitMQConfig.FILAS_SENSORES) {
                System.out.println("   [<-] " + fila);
            }
            System.out.println(" Filas de Saida (Monitores gerenciados pelo Mediator):");
            System.out.println("   [->] " + RabbitMQConfig.FILA_ENFERMAGEM + " (Todas as leituras)");
            System.out.println("   [->] " + RabbitMQConfig.FILA_MEDICO_PREFIXO + "<id> (Por paciente)");
            System.out.println("   [->] " + RabbitMQConfig.FILA_ALERTA + " (Apenas CRITICOS)");
            System.out.println(" Aguardando leituras dos sensores... (CTRL+C para sair)");
            System.out.println("==================================================\n");

            // Registra os consumidores em cada uma das 4 filas de sensores
            for (String nomeFila : RabbitMQConfig.FILAS_SENSORES) {
                final String f = nomeFila;
                channel.basicConsume(f, true, (consumerTag, delivery) -> {
                    String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    processarMensagemFila(f, payload);
                }, consumerTag -> { });
            }

            // Mantem a execucao ativa
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("MonitorMediator interrompido.");
        } catch (IOException | TimeoutException e) {
            System.err.println("Erro ao iniciar MonitorMediator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processarMensagemFila(String fila, String payload) {
        try {
            String[] partes = payload.split(";");
            if (partes.length < 2) {
                System.err.println("Mensagem com formato invalido na fila " + fila + ": " + payload);
                return;
            }

            String pacienteId = partes[0];
            Sensor sensor = null;

            switch (fila) {
                case RabbitMQConfig.FILA_CARDIACO:
                    double bpm = Double.parseDouble(partes[1]);
                    sensor = new Cardiaco(bpm);
                    break;

                case RabbitMQConfig.FILA_PRESSAO:
                    if (partes.length >= 3) {
                        double sis = Double.parseDouble(partes[1]);
                        double dia = Double.parseDouble(partes[2]);
                        sensor = new Pressao(sis, dia);
                    }
                    break;

                case RabbitMQConfig.FILA_SATURACAO:
                    double spo2 = Double.parseDouble(partes[1]);
                    sensor = new Saturacao(spo2);
                    break;

                case RabbitMQConfig.FILA_TEMPERATURA:
                    double temp = Double.parseDouble(partes[1]);
                    sensor = new Temperatura(temp);
                    break;

                default:
                    System.err.println("Fila desconhecida: " + fila);
                    break;
            }

            if (sensor != null) {
                processar(pacienteId, sensor);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem da fila " + fila + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public ResultadoAnalise processar(String pacienteId, Sensor sensor) {
        ResultadoAnalise resultado = analisar(sensor);
        String prioridade = resultado.getPrioridade().toLowerCase();
        String tipoSinal = sensor.getTipoSinal().toUpperCase();
        String dataHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String mensagem = "[" + dataHora + "] [" + pacienteId.toUpperCase() + "] " + sensor.getDescricao() 
                        + " - " + resultado.getStatus() 
                        + " [" + prioridade.toUpperCase() + "]";

        try {
            byte[] corpo = mensagem.getBytes(StandardCharsets.UTF_8);

            // 1. Mediator repassa para a Enfermagem (todas as leituras)
            channel.basicPublish("", RabbitMQConfig.FILA_ENFERMAGEM, null, corpo);

            // 2. Mediator repassa para a fila dedicada do Medico responsavel
            String filaMedico = RabbitMQConfig.getFilaMedico(pacienteId);
            channel.queueDeclare(filaMedico, true, false, false, null);
            channel.basicPublish("", filaMedico, null, corpo);

            // 3. Exibicao formatada no console do Mediator (Estilo Feed Notificacao com data a esquerda)
            if ("critico".equals(prioridade)) {
                // Publica na fila de alerta exclusivamente quando for critico
                channel.basicPublish("", RabbitMQConfig.FILA_ALERTA, null, corpo);
                System.out.printf("[%s]   +-- [CRITICO]  (!) %-11s (%s) - %s -> [DESPACHADO PARA APP EMERGENCIA!]%n",
                        dataHora, tipoSinal, sensor.getDescricao(), resultado.getStatus());
            } else if ("atencao".equals(prioridade)) {
                System.out.printf("[%s]   |-- [ATENCAO]  (^) %-11s (%s) - %s -> [Enfermagem e Medico]%n",
                        dataHora, tipoSinal, sensor.getDescricao(), resultado.getStatus());
            } else {
                System.out.printf("[%s]   |-- [ROTINA]   (*) %-11s (%s) - %s -> [Enfermagem e Medico]%n",
                        dataHora, tipoSinal, sensor.getDescricao(), resultado.getStatus());
            }

        } catch (Exception e) {
            System.err.println("Erro ao encaminhar mensagens pelo Mediator: " + e.getMessage());
            e.printStackTrace();
        }

        return resultado;
    }

    @Override
    public ResultadoAnalise analiseGeral(String relatorio, int nivelGeral) {
        if (nivelGeral == 2) {
            return new ResultadoAnalise(relatorio, "critico");
        } else if (nivelGeral == 1) {
            return new ResultadoAnalise(relatorio, "atencao");
        } else {
            return new ResultadoAnalise(relatorio, "rotina");
        }
    }

    @Override
    public ResultadoAnalise analisar(Sensor sensor) {
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

        return new ResultadoAnalise("Status Desconhecido", "rotina");
    }

    private ResultadoAnalise analisarCardiaco(double bpm) {
        if (bpm > 140) {
            return new ResultadoAnalise("TAQUICARDIA SEVERA (Alerta Critico)", "critico");
        } else if (bpm > 100) {
            return new ResultadoAnalise("TAQUICARDIA (Batimento Elevado)", "atencao");
        } else if (bpm < 60) {
            return new ResultadoAnalise("BRADICARDIA (Batimento Baixo)", "critico");
        } else {
            return new ResultadoAnalise("Batimento NORMAL", "rotina");
        }
    }

    private ResultadoAnalise analisarPressao(double sistolica, double diastolica) {
        if (sistolica >= 140 || diastolica >= 90) {
            return new ResultadoAnalise("HIPERTENSAO", "critico");
        } else if (sistolica < 90 || diastolica < 60) {
            return new ResultadoAnalise("Pressao BAIXA", "critico");
        } else if (sistolica > 120 || diastolica > 80) {
            return new ResultadoAnalise("ELEVADA / PRE-HIPERTENSAO", "atencao");
        } else {
            return new ResultadoAnalise("Pressao NORMAL", "rotina");
        }
    }

    private ResultadoAnalise analisarSaturacao(double saturacao) {
        if (saturacao >= 95) {
            return new ResultadoAnalise("Saturacao NORMAL", "rotina");
        } else if (saturacao >= 91) {
            return new ResultadoAnalise("HIPOXIA LEVE (Atencao)", "atencao");
        } else if (saturacao >= 81) {
            return new ResultadoAnalise("HIPOXIA MODERADA (Alerta)", "atencao");
        } else {
            return new ResultadoAnalise("HIPOXIA SEVERA (Alerta Critico)", "critico");
        }
    }

    private ResultadoAnalise analisarTemperatura(double temperatura) {
        if (temperatura >= 37.8) {
            return new ResultadoAnalise("FEBRE / HIPERTERMIA (Alerta)", "critico");
        } else if (temperatura >= 37.3) {
            return new ResultadoAnalise("ESTADO FEBRIL (Atencao)", "atencao");
        } else if (temperatura < 35.0) {
            return new ResultadoAnalise("HIPOTERMIA (Alerta Critico)", "critico");
        } else {
            return new ResultadoAnalise("Temperatura NORMAL", "rotina");
        }
    }

    public static void main(String[] args) {
        MonitorMediator mediator = new MonitorMediator();
        mediator.iniciar();
    }
}
