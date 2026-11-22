package main;

import mediator.MonitorMediator;
import paciente.Paciente;

public class Main {

    public static void main(String[] args) {
        String idPaciente = "paciente1";

        if (args.length >= 1) {
            idPaciente = args[0];
        }

        // 1. Sobe o MonitorMediator em segundo plano para consumir as filas dos sensores e rotear para os paineis
        Thread mediatorThread = new Thread(() -> {
            MonitorMediator mediator = new MonitorMediator();
            mediator.iniciar();
        });
        mediatorThread.setDaemon(true);
        mediatorThread.setName("Thread-MonitorMediator");
        mediatorThread.start();

        // Breve pausa para garantir a conexao e registro dos listeners no RabbitMQ
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) { }

        // 2. Inicia o envio continuo das leituras dos sensores para suas filas individuais
        System.out.println("==================================================");
        System.out.println(" INICIANDO SIMULACAO DO PACIENTE: " + idPaciente.toUpperCase());
        System.out.println(" Sensores publicando nas filas dedicadas do RabbitMQ...");
        System.out.println(" Mediator escutando e roteando para os monitores...");
        System.out.println("==================================================\n");

        Paciente paciente = new Paciente(idPaciente);
        paciente.checkup();
    }
}
