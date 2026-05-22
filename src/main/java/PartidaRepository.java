import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidaRepository {
    private String dbFile;

    public PartidaRepository(String dbFile) {
        this.dbFile = dbFile;
        new java.io.File(dbFile).getParentFile().mkdirs();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    public void criarTabelas() {
        String sqlPartidas = "CREATE TABLE IF NOT EXISTS partidas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "inicio TEXT," +
                "fim TEXT," +
                "vencedor TEXT," +
                "seed TEXT)";

        String sqlJogadores = "CREATE TABLE IF NOT EXISTS jogadores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "partida_id INTEGER," +
                "nome TEXT," +
                "tipo TEXT)";

        String sqlJogadas = "CREATE TABLE IF NOT EXISTS jogadas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "partida_id INTEGER," +
                "turno INTEGER," +
                "jogador TEXT," +
                "coordenada TEXT," +
                "resultado TEXT)";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlPartidas);
            stmt.execute(sqlJogadores);
            stmt.execute(sqlJogadas);
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    public long salvarPartida(String inicio, String fim, String vencedor, String seed) {
        String sql = "INSERT INTO partidas (inicio, fim, vencedor, seed) VALUES (?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inicio);
            ps.setString(2, fim);
            ps.setString(3, vencedor);
            ps.setString(4, seed);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.out.println("Erro ao salvar partida: " + e.getMessage());
        }
        return -1;
    }

    public void salvarJogador(long partidaId, String nome, String tipo) {
        String sql = "INSERT INTO jogadores (partida_id, nome, tipo) VALUES (?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, partidaId);
            ps.setString(2, nome);
            ps.setString(3, tipo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar jogador: " + e.getMessage());
        }
    }

    public void salvarJogada(long partidaId, int turno, String jogador, String coordenada, String resultado) {
        String sql = "INSERT INTO jogadas (partida_id, turno, jogador, coordenada, resultado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, partidaId);
            ps.setInt(2, turno);
            ps.setString(3, jogador);
            ps.setString(4, coordenada);
            ps.setString(5, resultado);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao salvar jogada: " + e.getMessage());
        }
    }

    public void listarPartidas() {
        String sql = "SELECT id, inicio, fim, vencedor FROM partidas ORDER BY id DESC";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("=== HISTORICO DE PARTIDAS ===");
            boolean tem = false;
            while (rs.next()) {
                tem = true;
                System.out.println("ID: " + rs.getLong("id") +
                        " | Inicio: " + rs.getString("inicio") +
                        " | Fim: " + rs.getString("fim") +
                        " | Vencedor: " + rs.getString("vencedor"));
            }
            if (!tem) System.out.println("Nenhuma partida salva.");
        } catch (SQLException e) {
            System.out.println("Erro ao listar partidas: " + e.getMessage());
        }
    }

    public void replay(long partidaId, int delayMs) {
        String sqlPartida = "SELECT * FROM partidas WHERE id = ?";
        String sqlJogadas = "SELECT * FROM jogadas WHERE partida_id = ? ORDER BY turno, id";

        try (Connection conn = conectar()) {
            PreparedStatement psP = conn.prepareStatement(sqlPartida);
            psP.setLong(1, partidaId);
            ResultSet rsP = psP.executeQuery();

            if (!rsP.next()) {
                System.out.println("Partida nao encontrada.");
                return;
            }

            System.out.println("=== REPLAY DA PARTIDA " + partidaId + " ===");
            System.out.println("Inicio: " + rsP.getString("inicio"));
            System.out.println("Vencedor: " + rsP.getString("vencedor"));
            System.out.println();

            PreparedStatement psJ = conn.prepareStatement(sqlJogadas);
            psJ.setLong(1, partidaId);
            ResultSet rsJ = psJ.executeQuery();

            java.util.Scanner sc = new java.util.Scanner(System.in);
            while (rsJ.next()) {
                System.out.println("Turno " + rsJ.getInt("turno") +
                        " | " + rsJ.getString("jogador") +
                        " -> " + rsJ.getString("coordenada") +
                        " : " + rsJ.getString("resultado"));

                if (delayMs > 0) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    System.out.print("[Enter para continuar]");
                    sc.nextLine();
                }
            }
            System.out.println("=== FIM DO REPLAY ===");
        } catch (SQLException e) {
            System.out.println("Erro no replay: " + e.getMessage());
        }
    }

    public List<Long> listarIds() {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT id FROM partidas ORDER BY id DESC";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) ids.add(rs.getLong("id"));
        } catch (SQLException e) {
            System.out.println("Erro ao listar ids: " + e.getMessage());
        }
        return ids;
    }
}
