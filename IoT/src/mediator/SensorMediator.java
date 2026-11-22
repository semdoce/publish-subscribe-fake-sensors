package mediator;

import sensors.Sensor;

public interface SensorMediator {
    void iniciar();
    ResultadoAnalise processar(String pacienteId, Sensor sensor);
    ResultadoAnalise analisar(Sensor sensor);
    ResultadoAnalise analiseGeral(String relatorio, int nivelGeral);
}
