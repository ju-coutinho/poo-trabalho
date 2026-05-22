public class Coordenada {
    private int x;
    private int y;

    public Coordenada(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean estaDentroDoTabuleiro(int tamanhoTabuleiro) {
        return x >= 0 && x < tamanhoTabuleiro && y >= 0 && y < tamanhoTabuleiro;
    }

    public String formatar() {
        char coluna = (char) ('A' + x);
        int linha = y + 1;
        return "" + coluna + linha;
    }
}
