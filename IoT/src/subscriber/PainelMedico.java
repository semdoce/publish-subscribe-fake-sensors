package subscriber;

import broker.RabbitMQConfig;

public class PainelMedico extends PainelHospitalar {

    private String idPaciente;

    public PainelMedico(String idPaciente) {
        this.idPaciente = (idPaciente != null && !idPaciente.isEmpty()) ? idPaciente : "paciente1";
    }

    @Override
    protected String getNomeFila() {
        return RabbitMQConfig.getFilaMedico(idPaciente);
    }

    @Override
    protected void processarMensagem(String mensagem) {
        System.out.println("  [MEDICO - " + idPaciente.toUpperCase() + "] " + mensagem);
    }

    @Override
    protected String getNomePainel() {
        return "PAINEL MEDICO (Acompanhando Paciente: " + idPaciente + ")";
    }
}
