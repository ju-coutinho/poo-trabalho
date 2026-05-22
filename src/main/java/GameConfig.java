import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GameConfig {
    private Properties props = new Properties();

    public void carregar(String caminho) {
        try (FileInputStream fis = new FileInputStream(caminho)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Aviso: nao foi possivel ler " + caminho + ". Usando valores padrao.");
        }
    }

    public int getTamanhoTabuleiro() {
        return Integer.parseInt(props.getProperty("board.size", "10"));
    }

    public int[] getTamanhosNavios() {
        String[] partes = props.getProperty("fleet.sizes", "5,4,3,3,2").split(",");
        int[] tamanhos = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            tamanhos[i] = Integer.parseInt(partes[i].trim());
        }
        return tamanhos;
    }

    public String[] getNomesNavios() {
        String[] nomes = props.getProperty("fleet.names", "Porta-avioes,Encouracado,Cruzador,Submarino,Destroyer").split(",");
        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = nomes[i].trim();
        }
        return nomes;
    }

    public String getAdjacencyRule() {
        return props.getProperty("fleet.adjacency_rule", "ORTHO_DIAG");
    }

    public boolean isHitGrantsExtraShot() {
        return Boolean.parseBoolean(props.getProperty("rules.hit_grants_extra_shot", "false"));
    }

    public int getMaxExtraShots() {
        return Integer.parseInt(props.getProperty("rules.max_extra_shots", "3"));
    }

    public String getCpuStrategy() {
        return props.getProperty("cpu.strategy", "HUNT");
    }

    public boolean isCpuUseParityPreference() {
        return Boolean.parseBoolean(props.getProperty("cpu.use_parity_preference", "true"));
    }

    public boolean isShowOwnShips() {
        return Boolean.parseBoolean(props.getProperty("ui.show_own_ships", "true"));
    }

    public boolean isShowLegend() {
        return Boolean.parseBoolean(props.getProperty("ui.show_legend", "true"));
    }

    public int getReplayDelayMs() {
        return Integer.parseInt(props.getProperty("ui.replay_delay_ms", "250"));
    }

    public boolean isDbEnabled() {
        return Boolean.parseBoolean(props.getProperty("db.enabled", "false"));
    }

    public String getDbFile() {
        return props.getProperty("db.sqlite.file", "data/batalha_naval.db");
    }

    public boolean isAutoMigrate() {
        return Boolean.parseBoolean(props.getProperty("db.auto_migrate", "true"));
    }

    public boolean isSaveInitialFleet() {
        return Boolean.parseBoolean(props.getProperty("db.save_initial_fleet", "true"));
    }

    public long getSeed() {
        String s = props.getProperty("game.seed", "").trim();
        if (s.isEmpty()) return -1;
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return s.hashCode();
        }
    }

    public String getGameMode() {
        return props.getProperty("game.mode", "PLAY").trim().toUpperCase();
    }

    public String getGroupId() {
        return props.getProperty("group.id", "G00");
    }
}
