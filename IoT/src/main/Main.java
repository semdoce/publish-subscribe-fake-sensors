package main;

//import sensors.FacadeSensors;
import paciente.Paciente;

public class Main {

    public static void main(String[] args) {
        // Paciente padrao se nao for passado argumento
        String idPaciente = "paciente1";

        if (args.length >= 1) {
            idPaciente = args[0];
        }

        System.out.println("Iniciando monitoramento para: " + idPaciente);
        Paciente paciente = new Paciente(idPaciente);
        paciente.checkup();
    }
}
