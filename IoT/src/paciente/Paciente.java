package paciente;

import sensors.PublicadorSensores;

public class Paciente {
    private String nome;
    private PublicadorSensores publicador;

    public Paciente(String nome) {
        this.nome = nome;
        this.publicador = new PublicadorSensores(nome);
    }

    public void checkup() {
        // Dispara o envio continuo das medicoes dos sensores para suas respectivas filas
        this.publicador.iniciar();
    }

    public String getNome() {
        return nome;
    }
}
