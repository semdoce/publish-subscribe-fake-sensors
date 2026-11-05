package paciente;
import sensors.FacadeSensors;
public class Paciente {
	private String nome;
    private FacadeSensors monitorSensores;

    public Paciente(String nome) {
        this.nome = nome;
        // O paciente usa a Facade para gerenciar os exames
        this.monitorSensores = new FacadeSensors();
    }

    public void analiseGeral() {
        this.monitorSensores.checkup(this.nome);
    }

    public String getNome() {
        return nome;
    }
}
