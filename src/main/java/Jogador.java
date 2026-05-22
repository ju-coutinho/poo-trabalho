public class Jogador {
    private String nome;
    private boolean cpu;

    public Jogador(String nome, boolean cpu) {
        this.nome = nome;
        this.cpu = cpu;
    }

    public String getNome() {
        return nome;
    }

    public boolean isCpu() {
        return cpu;
    }
}