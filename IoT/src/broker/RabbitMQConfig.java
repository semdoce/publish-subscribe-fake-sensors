package broker;

import com.rabbitmq.client.Channel;
import java.io.IOException;

public class RabbitMQConfig {
    public static final String HOST = "localhost";

    // Filas individuais de entrada (Sensores -> Mediator)
    public static final String FILA_CARDIACO    = "fila.cardiaco";
    public static final String FILA_PRESSAO     = "fila.pressao";
    public static final String FILA_SATURACAO   = "fila.saturacao";
    public static final String FILA_TEMPERATURA = "fila.temperatura";

    public static final String[] FILAS_SENSORES = {
        FILA_CARDIACO,
        FILA_PRESSAO,
        FILA_SATURACAO,
        FILA_TEMPERATURA
    };

    // Filas individuais de saida (Mediator -> Monitores) - SEM EXCHANGES
    public static final String FILA_ENFERMAGEM = "fila.enfermagem";
    public static final String FILA_ALERTA     = "fila.alerta";
    public static final String FILA_MEDICO_PREFIXO = "fila.medico.";

    public static String getFilaMedico(String idPaciente) {
        String pac = (idPaciente != null && !idPaciente.isEmpty()) ? idPaciente : "paciente1";
        return FILA_MEDICO_PREFIXO + pac.toLowerCase();
    }

    /**
     * Declara todas as filas ponto-a-ponto no RabbitMQ.
     * Nao ha criacao de nenhuma Exchange customizada (Topic, Direct ou Fanout).
     */
    public static void inicializarFilas(Channel channel) throws IOException {
        // Filas dos sensores (durable = true exigido pelo RabbitMQ 4+)
        for (String fila : FILAS_SENSORES) {
            channel.queueDeclare(fila, true, false, false, null);
        }

        // Filas gerais dos paineis (durable = true)
        channel.queueDeclare(FILA_ENFERMAGEM, true, false, false, null);
        channel.queueDeclare(FILA_ALERTA, true, false, false, null);
    }
}
