package mediator;
import sensors.Sensor;

public interface SensorMediator {
    void checkup(String pacienteId);
    ResultadoAnalise processar(String pacienteId, Sensor sensor);
}