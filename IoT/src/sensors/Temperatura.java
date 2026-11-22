package sensors;

public class Temperatura implements Sensor {
    private double temp;

    public Temperatura() {
    }

    public Temperatura(double temp) {
        this.temp = temp;
    }

    @Override
    public double medir() {
        int categoria = (int) Math.floor(Math.random() * 6) + 1;

        if (categoria == 1) { 
            // Hipotermia (Temperatura Baixa / Crítica)
            temp = (Math.random() * 5.0) + 30.0;
        } 
        else if (categoria == 2 || categoria == 3 || categoria == 4) { 
            // Temperatura Normal
            temp = (Math.random() * 2.3) + 35.0;
        } 
        else if (categoria == 5) {
            // Estado Febril / Febrícula
            temp = (Math.random() * 0.5) + 37.3;
        }
        else { 
            // Febre / Febre Alta (Crítica)
            temp = (Math.random() * 4.3) + 37.8;
        }
        
        temp = Math.round(temp * 10.0) / 10.0;
        return temp;
    }

    @Override
    public String getTipoSinal() {
        return "temperatura";
    }

    @Override
    public String getDescricao() {
        return temp + " °C";
    }

    public double getTemperatura() {
        return this.temp;
    }

}
