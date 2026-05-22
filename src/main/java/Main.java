public class Main {
    public static void main(String[] args) {
        GameConfig config = new GameConfig();
        config.carregar("game.properties");

        PartidaRepository repo = null;
        if (config.isDbEnabled()) {
            repo = new PartidaRepository(config.getDbFile());
            if (config.isAutoMigrate()) {
                repo.criarTabelas();
            }
        }

        BatalhaNavalUI ui = new BatalhaNavalUI(config, repo);
        ui.iniciar();
    }
}
