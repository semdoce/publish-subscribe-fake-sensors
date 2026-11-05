package sensors;

public class FacadeSensors {
	private Pressao sensorPressao;
    private Cardiaco sensorCardiaco;
    private Saturacao sensorSaturacao;
    private Temperatura sensorTemperatura;

    public FacadeSensors() {
        this.sensorPressao = new Pressao();
        this.sensorCardiaco = new Cardiaco();
        this.sensorSaturacao = new Saturacao();
        this.sensorTemperatura = new Temperatura();
    }

    public void checkup(String nomePaciente) {
        System.out.println("\n==================================================");
        System.out.println(" Triagem: " + nomePaciente.toUpperCase());
        System.out.println("==================================================");
        
        sensorPressao.medirPressao();
        sensorCardiaco.medirBpm();
        sensorSaturacao.medirSpo2();
        sensorTemperatura.medirTemperatura();
        
        /*
        sensorPressao.analisePressao();
        sensorCardiaco.analiseBpm();
        sensorSaturacao.analiseSpo2();
        sensorTemperatura.analiseTemperatura();
        */
        
        System.out.println("==================================================\n");
    }
}
