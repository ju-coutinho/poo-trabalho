import java.util.Arrays;

public class ValidadorDeFrotas {
    public boolean validarTamanhos(int[] tamanhosNavios) {
        if (tamanhosNavios == null || tamanhosNavios.length != 5) {
            return false;
        }

        int[] esperados = new int[] {2, 3, 3, 4, 5};
        int[] recebidos = Arrays.copyOf(tamanhosNavios, tamanhosNavios.length);

        Arrays.sort(recebidos);

        return Arrays.equals(esperados, recebidos);
    }
}