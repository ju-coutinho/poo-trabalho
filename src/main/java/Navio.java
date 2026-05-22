public class Navio {
    private String nome;
    private int tamanho;
    private int vida;

    public Navio(String nome, int tamanho) {
        this.nome = nome;
        this.tamanho = tamanho;
        this.vida = tamanho;
    }

    public String getNome() {
        return nome;
    }

    public int getTamanho() {
        return tamanho;
    }

    public int getVida() {
        return vida;
    }

    public void receberAcerto() {
        if (vida > 0) {
            vida--;
        }
    }

    public boolean estaAfundado() {
        return vida == 0;
    }
}