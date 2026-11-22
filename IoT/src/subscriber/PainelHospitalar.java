package subscriber;

import java.nio.charset.StandardCharsets;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import broker.RabbitMQConfig;

public abstract class PainelHospitalar {

    protected abstract String getNomeFila();
    protected abstract void processarMensagem(String mensagem);
    protected abstract String getNomePainel();

    // Template method: conecta ao RabbitMQ, garante a fila e consome sem depender de Exchange
    public final void iniciar() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfig.HOST);

        try {
            final Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            String nomeFila = getNomeFila();

            // Declara a fila direta como duravel (sem exchange)
            channel.queueDeclare(nomeFila, true, false, false, null);

            System.out.println("==================================================");
            System.out.println(" " + getNomePainel());
            System.out.println(" Fila Direta: " + nomeFila + " (Sem Exchange)");
            System.out.println(" Aguardando mensagens enviadas pelo Mediator...");
            System.out.println(" (Pressione CTRL+C para sair)");
            System.out.println("==================================================\n");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String mensagem = new String(delivery.getBody(), StandardCharsets.UTF_8);
                processarMensagem(mensagem);
            };

            channel.basicConsume(nomeFila, true, deliverCallback, consumerTag -> { });

        } catch (Exception e) {
            System.err.println("Erro na conexao com RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
