package mediator;

public class ResultadoAnalise {

    private String status;
    private String prioridade;

    public ResultadoAnalise(String status, String prioridade) {
        this.status = status;
        this.prioridade = prioridade;
    }

    public String getStatus() {
        return status;
    }

    public String getPrioridade() {
        return prioridade;
    }
}