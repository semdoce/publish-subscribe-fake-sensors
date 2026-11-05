package sensors;

public class Pressao {
	private double sistolica;
	private double diastolica;
	
	public void medirPressao() {
		int categoria = (int) Math.floor(Math.random() * 6) + 1;

	    if (categoria == 1) { 
	        // Pressão baixa
	        sistolica = Math.floor(Math.random() * 30) + 60;   // Gera de 60 a 89
	        diastolica = Math.floor(Math.random() * 20) + 40;  // Gera de 40 a 59
	    } 
	    else if (categoria == 2 || categoria == 3 || categoria == 4) { 
	        // Pressão normal
	        sistolica = Math.floor(Math.random() * 31) + 90;   // Gera de 90 a 120
	        diastolica = Math.floor(Math.random() * 21) + 60;  // Gera de 60 a 80
	    } 
	    else if (categoria == 5) {
	        // Pressão Elevada / Pré-Hipertensão
	        sistolica = Math.floor(Math.random() * 19) + 121;  // Gera de 121 a 139
	        diastolica = Math.floor(Math.random() * 9) + 81;    // Gera de 81 a 89
	    }
	    else { 
	        // Pressão Alta
	        sistolica = Math.floor(Math.random() * 81) + 140;  // Gera de 140 a 220
	        diastolica = Math.floor(Math.random() * 61) + 90;   // Gera de 90 a 150
	    }
	    
	    analisePressao();
	}
	
	public void analisePressao() {
		System.out.println(sistolica + " " + diastolica);
		
		if (sistolica >= 140 || diastolica >= 90) {
	        System.out.println("Status: HIPERTENSÃO");
	    } 
	    else if (sistolica < 90 || diastolica < 60) {
	        System.out.println("Status: Pressão BAIXA");
	    }
	    else if (sistolica > 120 || diastolica > 80) {
	        System.out.println("Status: ELEVADA / PRÉ-HIPERTENSÃO");
	    }
	    else {
	        System.out.println("Status: Pressão NORMAL");
	    }
	}
	
	public double getSistolica() {
		return this.sistolica;
	}
	public double getDiastolica() {
		return this.diastolica;
	}
	
}
