public class Jogo {
    private Jogador jogadorHumano;
    private Jogador jogadorCpu;
    private boolean turnoDoHumano;

    public Jogo(Jogador jogadorHumano, Jogador jogadorCpu) {
        this.jogadorHumano = jogadorHumano;
        this.jogadorCpu = jogadorCpu;
        this.turnoDoHumano = true;
    }

    public Jogador getJogadorHumano() {
        return jogadorHumano;
    }

    public Jogador getJogadorCpu() {
        return jogadorCpu;
    }

    public boolean isTurnoDoHumano() {
        return turnoDoHumano;
    }

    public void alternarTurno() {
        turnoDoHumano = !turnoDoHumano;
    }
}