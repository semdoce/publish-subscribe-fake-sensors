package sensors;

import java.nio.charset.StandardCharsets;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import broker.RabbitMQConfig;

public class PublicadorSensores {
    private final String pacienteId;
    private final Sensor[] sensores;
    private final String filtroTipo;

    public PublicadorSensores(String pacienteId) {
        this(pacienteId, null);
    }

    public PublicadorSensores(String pacienteId, String filtroTipo) {
        this.pacienteId = pacienteId;
        this.filtroTipo = filtroTipo != null ? filtroTipo.toLowerCase() : null;

        if (this.filtroTipo == null || this.filtroTipo.equals("todos")) {
            this.sensores = new Sensor[] {
                new Cardiaco(),
                new Pressao(),
                new Saturacao(),
                new Temperatura()
            };
        } else if (this.filtroTipo.equals("cardiaco") || this.filtroTipo.equals("bpm")) {
            this.sensores = new Sensor[] { new Cardiaco() };
        } else if (this.filtroTipo.equals("pressao")) {
            this.sensores = new Sensor[] { new Pressao() };
        } else if (this.filtroTipo.equals("saturacao") || this.filtroTipo.equals("spo2") || this.filtroTipo.equals("o2")) {
            this.sensores = new Sensor[] { new Saturacao() };
        } else if (this.filtroTipo.equals("temperatura") || this.filtroTipo.equals("temp")) {
            this.sensores = new Sensor[] { new Temperatura() };
        } else {
            System.err.println("Tipo de sensor desconhecido: " + filtroTipo + ". Carregando todos os sensores.");
            this.sensores = new Sensor[] {
                new Cardiaco(),
                new Pressao(),
                new Saturacao(),
                new Temperatura()
            };
        }
    }

    public void iniciar() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfig.HOST);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            RabbitMQConfig.inicializarFilas(channel);

            System.out.println("==================================================");
            System.out.println(" PUBLICADOR DE SENSORES INICIADO");
            System.out.println(" Paciente: " + pacienteId.toUpperCase());
            System.out.println(" Modo: " + (filtroTipo != null ? filtroTipo : "todos os sensores"));
            System.out.println(" Publicando nas filas dedicadas do RabbitMQ...");
            System.out.println("==================================================\n");

            while (!Thread.currentThread().isInterrupted()) {
                String hora = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

                // Realiza as medicoes de todos os sensores e gera linha de resumo compacta
                StringBuilder resumo = new StringBuilder();
                for (int i = 0; i < sensores.length; i++) {
                    sensores[i].medir();
                    resumo.append(sensores[i].getDescricao());
                    if (i < sensores.length - 1) {
                        resumo.append(" | ");
                    }
                }

                System.out.println("\n[" + hora + "] [TELEMETRIA] [" + pacienteId.toUpperCase() + "] " + resumo);

                // Publica cada medicao na sua respectiva fila
                for (Sensor sensor : sensores) {
                    publicar(channel, sensor);
                }

                Thread.sleep(15000);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Publicacao de sensores interrompida.");
        } catch (Exception e) {
            System.err.println("Erro na publicacao dos sensores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void publicar(Channel channel, Sensor sensor) throws Exception {
        String filaDestino = "";
        String payload = "";

        if (sensor instanceof Cardiaco) {
            Cardiaco c = (Cardiaco) sensor;
            filaDestino = RabbitMQConfig.FILA_CARDIACO;
            payload = pacienteId + ";" + c.getBpm();
        } else if (sensor instanceof Pressao) {
            Pressao p = (Pressao) sensor;
            filaDestino = RabbitMQConfig.FILA_PRESSAO;
            payload = pacienteId + ";" + p.getSistolica() + ";" + p.getDiastolica();
        } else if (sensor instanceof Saturacao) {
            Saturacao s = (Saturacao) sensor;
            filaDestino = RabbitMQConfig.FILA_SATURACAO;
            payload = pacienteId + ";" + s.getSpo2();
        } else if (sensor instanceof Temperatura) {
            Temperatura t = (Temperatura) sensor;
            filaDestino = RabbitMQConfig.FILA_TEMPERATURA;
            payload = pacienteId + ";" + t.getTemperatura();
        }

        if (!filaDestino.isEmpty()) {
            channel.basicPublish("", filaDestino, null, payload.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) {
        String paciente = "paciente1";
        String tipo = "todos";

        if (args.length >= 1) {
            paciente = args[0];
        }
        if (args.length >= 2) {
            tipo = args[1];
        }

        PublicadorSensores publicador = new PublicadorSensores(paciente, tipo);
        publicador.iniciar();
    }
}
