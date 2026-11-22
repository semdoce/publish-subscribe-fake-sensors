package subscriber;

import broker.RabbitMQConfig;

public class AppEmergencia extends PainelHospitalar {

    private String idPaciente;

    public AppEmergencia(String idPaciente) {
        this.idPaciente = idPaciente;
    }

    @Override
    protected String getNomeFila() {
        return RabbitMQConfig.FILA_ALERTA;
    }

    @Override
    protected void processarMensagem(String mensagem) {
        if (idPaciente != null && !idPaciente.isEmpty()) {
            if (!mensagem.toUpperCase().contains("[" + idPaciente.toUpperCase() + "]")) {
                return;
            }
        }
        System.out.println("\n==========================================================================");
        System.out.println(" [!] ALERTA CRITICO DE EMERGENCIA - CODIGO AZUL");
        System.out.println("==========================================================================");
        System.out.println("  " + mensagem);
        System.out.println("==========================================================================\u0007");
    }

    @Override
    protected String getNomePainel() {
        if (idPaciente != null && !idPaciente.isEmpty()) {
            return "APP EMERGENCIA (Paciente: " + idPaciente + " | Somente Leituras CRITICAS)";
        }
        return "APP EMERGENCIA (Todos os Pacientes | Somente Leituras CRITICAS)";
    }
}
