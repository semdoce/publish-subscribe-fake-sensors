package mediator;

import sensors.Cardiaco;
import sensors.Pressao;
import sensors.Saturacao;
import sensors.Sensor;
import sensors.Temperatura;

public class MonitorMediator implements SensorMediator {

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

        System.out.println("==================================================");
        System.out.println(" Monitor iniciado para: " + pacienteId.toUpperCase());
        //System.out.println(" Exchange: " + RabbitMQConfig.EXCHANGE_NAME + " (topic)");
        System.out.println("==================================================\n");

        try {
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
            System.err.println("Erro no monitoramento: "+ e.getMessage());
        }
    }


    @Override
    public ResultadoAnalise processar(String pacienteId, Sensor sensor) {

        sensor.medir();

        ResultadoAnalise resultado = analisar(sensor);

        System.out.println(
            "Paciente: " + pacienteId
            + " | "
            + sensor.getDescricao()
            + " - "
            + resultado.getStatus()
            + " | Prioridade: "
            + resultado.getPrioridade()
        );

        return resultado;
    }

    public ResultadoAnalise analiseGeral(String relatorio, int nivelGeral) {

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

    private ResultadoAnalise analisarTemperatura(double temperatura) {
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

    private ResultadoAnalise analisarSaturacao(double saturacao) {
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

}
