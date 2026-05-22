import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BatalhaNavalTest {

    @Test
    void coordenadaValidaDentroDoTabuleiro() {
        Coordenada c = new Coordenada(0, 0);
        assertTrue(c.estaDentroDoTabuleiro(10));
    }

    @Test
    void coordenadaInvalidaForaDoTabuleiro() {
        Coordenada c = new Coordenada(10, 0);
        assertFalse(c.estaDentroDoTabuleiro(10));
    }

    @Test
    void parseCoordenadaValida() {
        Coordenada c = BatalhaNavalUI.parseCoordenada("A1", 10);
        assertNotNull(c);
        assertEquals(0, c.getX());
        assertEquals(0, c.getY());
    }

    @Test
    void parseCoordenadaJ10Valida() {
        Coordenada c = BatalhaNavalUI.parseCoordenada("J10", 10);
        assertNotNull(c);
        assertEquals(9, c.getX());
        assertEquals(9, c.getY());
    }

    @Test
    void parseCoordenadaInvalidaLetraForaDoRange() {
        Coordenada c = BatalhaNavalUI.parseCoordenada("K1", 10);
        assertNull(c);
    }

    @Test
    void parseCoordenadaInvalidaNumeroForaDoRange() {
        Coordenada c = BatalhaNavalUI.parseCoordenada("A11", 10);
        assertNull(c);
    }

    @Test
    void posicionamentoNavioValido() {
        char[][] board = new char[10][10];
        BatalhaNavalUI.fill(board, '.');
        assertTrue(BatalhaNavalUI.podePosicionarNavio(board, 0, 0, 3, true, 10));
    }

    @Test
    void posicionamentoNavioForaDoTabuleiro() {
        char[][] board = new char[10][10];
        BatalhaNavalUI.fill(board, '.');
        assertFalse(BatalhaNavalUI.podePosicionarNavio(board, 9, 0, 3, true, 10));
    }

    @Test
    void posicionamentoNavioEncostando() {
        char[][] board = new char[10][10];
        int[][] ids = new int[10][10];
        BatalhaNavalUI.fill(board, '.');
        BatalhaNavalUI.fillInt(ids, -1);
        BatalhaNavalUI.colocarNavio(board, ids, 0, 0, 3, true, 0);
        assertFalse(BatalhaNavalUI.podePosicionarNavio(board, 0, 1, 3, true, 10));
    }

    @Test
    void tiroNaAgua() {
        char[][] ships = new char[10][10];
        int[][] ids = new int[10][10];
        int[] hp = {3};
        BatalhaNavalUI.fill(ships, '.');
        BatalhaNavalUI.fillInt(ids, -1);
        int resultado = BatalhaNavalUI.applyShot(ships, ids, hp, 5, 5);
        assertEquals(0, resultado);
    }

    @Test
    void tiroAcerto() {
        char[][] ships = new char[10][10];
        int[][] ids = new int[10][10];
        int[] hp = {3};
        BatalhaNavalUI.fill(ships, '.');
        BatalhaNavalUI.fillInt(ids, -1);
        BatalhaNavalUI.colocarNavio(ships, ids, 0, 0, 3, true, 0);
        int resultado = BatalhaNavalUI.applyShot(ships, ids, hp, 0, 0);
        assertEquals(1, resultado);
    }

    @Test
    void navioAfundado() {
        char[][] ships = new char[10][10];
        int[][] ids = new int[10][10];
        int[] hp = {1};
        BatalhaNavalUI.fill(ships, '.');
        BatalhaNavalUI.fillInt(ids, -1);
        BatalhaNavalUI.colocarNavio(ships, ids, 0, 0, 1, true, 0);
        int resultado = BatalhaNavalUI.applyShot(ships, ids, hp, 0, 0);
        assertEquals(2, resultado);
    }

    @Test
    void fimDeJogoQuandoFrotaAfundada() {
        int[] hp = {0, 0, 0, 0, 0};
        assertTrue(BatalhaNavalUI.frotaAfundada(hp));
    }

    @Test
    void jogoNaoAcabouComNaviosVivos() {
        int[] hp = {3, 0, 0, 0, 0};
        assertFalse(BatalhaNavalUI.frotaAfundada(hp));
    }

    @Test
    void validadorFrotaCorreta() {
        ValidadorDeFrotas v = new ValidadorDeFrotas();
        assertTrue(v.validarTamanhos(new int[]{5, 4, 3, 3, 2}));
    }

    @Test
    void validadorFrotaErrada() {
        ValidadorDeFrotas v = new ValidadorDeFrotas();
        assertFalse(v.validarTamanhos(new int[]{5, 4, 3, 3, 3}));
    }

    @Test
    void navioRecebeAcertoEAfunda() {
        Navio n = new Navio("Teste", 2);
        n.receberAcerto();
        assertFalse(n.estaAfundado());
        n.receberAcerto();
        assertTrue(n.estaAfundado());
    }

    @Test
    void coordenadaFormatada() {
        Coordenada c = new Coordenada(0, 0);
        assertEquals("A1", c.formatar());
    }

    @Test
    void tabuleiroCoordenadaDentro() {
        Tabuleiro t = new Tabuleiro(10);
        assertTrue(t.coordenadaEstaDentro(new Coordenada(5, 5)));
    }

    @Test
    void tabuleiroCoordenadaFora() {
        Tabuleiro t = new Tabuleiro(10);
        assertFalse(t.coordenadaEstaDentro(new Coordenada(10, 10)));
    }

    @Test
    void jogoAlternarTurno() {
        Jogo jogo = new Jogo(new HumanPlayer("Jogador"), new CpuPlayer("CPU"));
        assertTrue(jogo.isTurnoDoHumano());
        jogo.alternarTurno();
        assertFalse(jogo.isTurnoDoHumano());
    }
}
