package sensors;

public class Saturacao implements Sensor {
    private double spo2;

    public Saturacao() {
    }

    public Saturacao(double spo2) {
        this.spo2 = spo2;
    }

    @Override
    public double medir() {
        int categoria = (int) Math.floor(Math.random() * 6) + 1;

        if (categoria == 1) {
            // Hipóxia Severa / Crítica
            spo2 = Math.floor(Math.random() * 21) + 60; // Gera de 60% a 80%
        } else if (categoria == 2 || categoria == 3 || categoria == 4) {
            // Saturação Normal / Ideal
            spo2 = Math.floor(Math.random() * 6) + 95; // Gera de 95% a 100%
        } else if (categoria == 5) {
            // Hipóxia Leve / Atenção
            spo2 = Math.floor(Math.random() * 4) + 91; // Gera de 91% a 94%
        } else {
            // Hipóxia Moderada
            spo2 = Math.floor(Math.random() * 10) + 81; // Gera de 81% a 90%
        }
        return spo2;
    }

    @Override
    public String getTipoSinal() {
        return "spo2";
    }

    @Override
    public String getDescricao() {
        return spo2 + "% SpO2";
    }

    public double getSpo2() {
        return this.spo2;
    }
}
