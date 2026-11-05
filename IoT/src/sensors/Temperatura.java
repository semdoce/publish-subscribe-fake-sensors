package sensors;

public class Temperatura {
	private double temp;

    public void medirTemperatura() {
        // Sorteia de 1 a 6 para incluir os extremos de forma balanceada
        int categoria = (int) Math.floor(Math.random() * 6) + 1;

        if (categoria == 1) { 
            // Hipotermia (Temperatura Baixa / Crítica)
            // Gera de 30.0°C até 34.9°C (Multiplicar por 5.0 gera de 0.0 a 4.99...)
            temp = (Math.random() * 5.0) + 30.0;
        } 
        else if (categoria == 2 || categoria == 3 || categoria == 4) { 
            // Temperatura Normal
            // Gera de 35.0°C até 37.2°C (Multiplicar por 2.3 gera de 0.0 a 2.29...)
            temp = (Math.random() * 2.3) + 35.0;
        } 
        else if (categoria == 5) {
            // Estado Febril / Febrícula
            // Gera de 37.3°C até 37.7°C (Multiplicar por 0.5 gera de 0.0 a 0.49...)
            temp = (Math.random() * 0.5) + 37.3;
        }
        else { 
            // Febre / Febre Alta (Crítica)
            // Gera de 37.8°C até 42.0°C (Multiplicar por 4.3 gera de 0.0 a 4.29...)
            temp = (Math.random() * 4.3) + 37.8;
        }
        
        // Arredonda para 1 casa decimal para parecer um termômetro real (Ex: 36.6)
        temp = Math.round(temp * 10.0) / 10.0;
        
        analiseTemperatura();
    }

    public void analiseTemperatura() {
        System.out.println(temp + " °C");
        
        if (temp >= 37.8) {
            System.out.println("Status: FEBRE / HIPERTERMIA (Alerta)");
        } 
        else if (temp >= 37.3) {
            System.out.println("Status: ESTADO FEBRIL (Atenção)");
        }
        else if (temp < 35.0) {
            System.out.println("Status: HIPOTERMIA (Alerta Crítico)");
        } 
        else {
            System.out.println("Status: Temperatura NORMAL");
        }
    }

    public double getTemperatura() {
        return this.temp;
    }
}
