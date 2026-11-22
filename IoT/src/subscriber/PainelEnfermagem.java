package subscriber;

import broker.RabbitMQConfig;

public class PainelEnfermagem extends PainelHospitalar {

    @Override
    protected String getNomeFila() {
        return RabbitMQConfig.FILA_ENFERMAGEM;
    }

    @Override
    protected void processarMensagem(String mensagem) {
        System.out.println("  [ENFERMAGEM] " + mensagem);
    }

    @Override
    protected String getNomePainel() {
        return "PAINEL DE ENFERMAGEM (Monitoramento Geral de Todos os Leitos)";
    }
}
