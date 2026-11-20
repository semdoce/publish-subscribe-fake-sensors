package paciente;

//import sensors.FacadeSensors;
import mediator.MonitorMediator;

public class Paciente {
    private String nome;
    private MonitorMediator monitorSensores;

    public Paciente(String nome) {
        this.nome = nome;
        this.monitorSensores = new MonitorMediator();
    }

    public void checkup() {
        // Dispara o monitoramento continuo dos sensores via RabbitMQ
        this.monitorSensores.checkup(this.nome);
    }

    public String getNome() {
        return nome;
    }
}
