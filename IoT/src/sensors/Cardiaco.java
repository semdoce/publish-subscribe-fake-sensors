package sensors;

public class Cardiaco {
	private double bpm;

    public void medirBpm() {
        // Sorteia de 1 a 6 para incluir os extremos de forma balanceada
        int categoria = (int) Math.floor(Math.random() * 6) + 1;

        if (categoria == 1) { 
            // Bradicardia (Batimento Baixo / Crítico)
            bpm = Math.floor(Math.random() * 20) + 40;   // Gera de 40 a 59 BPM
        } 
        else if (categoria == 2 || categoria == 3 || categoria == 4) { 
            // Batimento Normal
            bpm = Math.floor(Math.random() * 41) + 60;   // Gera de 60 a 100 BPM
        } 
        else if (categoria == 5) {
            // Taquicardia Leve / Ritmo Acelerado
            bpm = Math.floor(Math.random() * 40) + 101;  // Gera de 101 a 140 BPM
        }
        else { 
            // Taquicardia Severa (Batimento Muito Alto / Crítico)
            bpm = Math.floor(Math.random() * 61) + 141;  // Gera de 141 a 201 BPM
        }
        
        analiseBpm();
    }

    public void analiseBpm() {
        System.out.println(bpm + " BPM");
        
        if (bpm > 140) {
            System.out.println("Status: TAQUICARDIA SEVERA (Alerta Crítico)");
        } 
        else if (bpm > 100) {
            System.out.println("Status: TAQUICARDIA (Batimento Elevado)");
        }
        else if (bpm < 60) {
            System.out.println("Status: BRADICARDIA (Batimento Baixo)");
        } 
        else {
            System.out.println("Status: Batimento NORMAL");
        }
    }

    public double getBpm() {
        return this.bpm;
    }
}
