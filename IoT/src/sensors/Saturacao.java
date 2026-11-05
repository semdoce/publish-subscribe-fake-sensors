package sensors;

public class Saturacao {
	private double spo2;

    public void medirSpo2() {
        // Sorteia de 1 a 6 para incluir os extremos de forma balanceada
        int categoria = (int) Math.floor(Math.random() * 6) + 1;

        if (categoria == 1) { 
            // Hipóxia Severa / Crítica
            spo2 = Math.floor(Math.random() * 21) + 60;   // Gera de 60% a 80%
        } 
        else if (categoria == 2 || categoria == 3 || categoria == 4) { 
            // Saturação Normal / Ideal
            spo2 = Math.floor(Math.random() * 6) + 95;    // Gera de 95% a 100%
        } 
        else if (categoria == 5) {
            // Hipóxia Leve / Atenção
            spo2 = Math.floor(Math.random() * 4) + 91;    // Gera de 91% a 94%
        }
        else { 
            // Hipóxia Moderada
            spo2 = Math.floor(Math.random() * 10) + 81;   // Gera de 81% a 90%
        }
        
        analiseSpo2();
    }

    public void analiseSpo2() {
        System.out.println(spo2 + "% SpO2");
        
        if (spo2 >= 95) {
            System.out.println("Status: Saturação NORMAL");
        } 
        else if (spo2 >= 91) {
            System.out.println("Status: HIPÓXIA LEVE (Atenção)");
        }
        else if (spo2 >= 81) {
            System.out.println("Status: HIPÓXIA MODERADA (Alerta)");
        } 
        else {
            System.out.println("Status: HIPÓXIA SEVERA (Alerta Crítico)");
        }
    }

    public double getSpo2() {
        return this.spo2;
    }
}
