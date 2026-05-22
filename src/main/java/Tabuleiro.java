public class Tabuleiro {
    private int tamanho;

    public Tabuleiro(int tamanho) {
        this.tamanho = tamanho;
    }

    public int getTamanho() {
        return tamanho;
    }

    public boolean coordenadaEstaDentro(Coordenada coordenada) {
        if (coordenada == null) {
            return false;
        }

        return coordenada.estaDentroDoTabuleiro(tamanho);
    }
}