import java.util.*;

public class BatalhaNavalUI {

    private GameConfig config;
    private PartidaRepository repo;
    private Scanner sc;

    public BatalhaNavalUI(GameConfig config, PartidaRepository repo) {
        this.config = config;
        this.repo = repo;
        this.sc = new Scanner(System.in);
    }

    public void iniciar() {
        String modo = config.getGameMode();

        if ("LIST".equals(modo)) {
            if (repo != null) {
                repo.listarPartidas();
            } else {
                System.out.println("Banco desabilitado. Ative db.enabled=true no game.properties.");
            }
            return;
        }

        if ("REPLAY".equals(modo)) {
            if (repo != null) {
                repo.listarPartidas();
                System.out.print("ID da partida para replay: ");
                long id = Long.parseLong(sc.nextLine().trim());
                repo.replay(id, config.getReplayDelayMs());
            } else {
                System.out.println("Banco desabilitado. Ative db.enabled=true no game.properties.");
            }
            return;
        }

        jogar();
    }

    private void jogar() {
        System.out.println("=== BATALHA NAVAL ===");

        long seedUsada;
        Random rng;
        long seedConfig = config.getSeed();
        if (seedConfig == -1) {
            rng = new Random();
            seedUsada = rng.nextLong();
            rng = new Random(seedUsada);
        } else {
            seedUsada = seedConfig;
            rng = new Random(seedUsada);
        }

        int tamanho = config.getTamanhoTabuleiro();
        int[] tamanhosNavios = config.getTamanhosNavios();
        String[] nomesNavios = config.getNomesNavios();

        ValidadorDeFrotas validador = new ValidadorDeFrotas();
        if (!validador.validarTamanhos(tamanhosNavios)) {
            System.out.println("Configuracao de frota invalida no game.properties.");
            return;
        }

        char[][] ownShips = new char[tamanho][tamanho];
        char[][] ownShots = new char[tamanho][tamanho];
        char[][] cpuShips = new char[tamanho][tamanho];
        char[][] cpuShots = new char[tamanho][tamanho];

        fill(ownShips, '.');
        fill(ownShots, '.');
        fill(cpuShips, '.');
        fill(cpuShots, '.');

        int[][] ownShipId = new int[tamanho][tamanho];
        int[][] cpuShipId = new int[tamanho][tamanho];
        fillInt(ownShipId, -1);
        fillInt(cpuShipId, -1);

        int[] ownHp = Arrays.copyOf(tamanhosNavios, tamanhosNavios.length);
        int[] cpuHp = Arrays.copyOf(tamanhosNavios, tamanhosNavios.length);

        System.out.println();
        System.out.println("Posicionamento da sua frota.");
        System.out.println("Coordenadas: A-J e 1-10. Ex: A1, J10.");
        System.out.print("Deseja posicionar manualmente? (s/N): ");
        String manual = sc.nextLine().trim().toLowerCase(Locale.ROOT);

        if (manual.equals("s") || manual.equals("sim")) {
            for (int sid = 0; sid < tamanhosNavios.length; sid++) {
                boolean placed = false;
                while (!placed) {
                    System.out.println();
                    imprimirTabuleiro("SEU TABULEIRO", ownShips, true, tamanho);
                    System.out.println("Posicione: " + nomesNavios[sid] + " (tamanho " + tamanhosNavios[sid] + ")");
                    System.out.print("Coordenada inicial (ex A1): ");
                    String c = sc.nextLine().trim();
                    Coordenada coord = parseCoordenada(c, tamanho);
                    if (coord == null) {
                        System.out.println("Coordenada invalida.");
                        continue;
                    }
                    System.out.print("Direcao (H horizontal, V vertical): ");
                    String dir = sc.nextLine().trim().toUpperCase(Locale.ROOT);
                    boolean horiz = dir.equals("H");
                    boolean vert = dir.equals("V");
                    if (!horiz && !vert) {
                        System.out.println("Direcao invalida.");
                        continue;
                    }
                    if (!podePosicionarNavio(ownShips, coord.getX(), coord.getY(), tamanhosNavios[sid], horiz, tamanho)) {
                        System.out.println("Posicao invalida: nao cabe, colide ou encosta em outro navio.");
                        continue;
                    }
                    colocarNavio(ownShips, ownShipId, coord.getX(), coord.getY(), tamanhosNavios[sid], horiz, sid);
                    placed = true;
                }
            }
        } else {
            for (int sid = 0; sid < tamanhosNavios.length; sid++) {
                boolean ok = false;
                int tries = 0;
                while (!ok && tries < 2000) {
                    tries++;
                    boolean horiz = rng.nextInt(2) == 0;
                    int x = rng.nextInt(tamanho);
                    int y = rng.nextInt(tamanho);
                    if (podePosicionarNavio(ownShips, x, y, tamanhosNavios[sid], horiz, tamanho)) {
                        colocarNavio(ownShips, ownShipId, x, y, tamanhosNavios[sid], horiz, sid);
                        ok = true;
                    }
                }
                if (!ok) {
                    System.out.println("Falha ao posicionar automaticamente.");
                    return;
                }
            }
            System.out.println("Frota posicionada automaticamente.");
        }

        for (int sid = 0; sid < tamanhosNavios.length; sid++) {
            boolean ok = false;
            int tries = 0;
            while (!ok && tries < 5000) {
                tries++;
                boolean horiz = rng.nextInt(2) == 0;
                int x = rng.nextInt(tamanho);
                int y = rng.nextInt(tamanho);
                if (podePosicionarNavio(cpuShips, x, y, tamanhosNavios[sid], horiz, tamanho)) {
                    colocarNavio(cpuShips, cpuShipId, x, y, tamanhosNavios[sid], horiz, sid);
                    ok = true;
                }
            }
            if (!ok) {
                System.out.println("Falha ao posicionar CPU.");
                return;
            }
        }

        String inicio = java.time.LocalDateTime.now().toString();
        List<String[]> jogadas = new ArrayList<>();
        int turno = 0;
        boolean playerTurn = true;
        ArrayDeque<int[]> cpuTargets = new ArrayDeque<>();
        boolean[][] cpuTried = new boolean[tamanho][tamanho];
        String vencedor = "";

        while (true) {
            System.out.println();
            imprimirDoisTabuleiros(ownShips, ownShots, tamanho, config.isShowLegend());
            System.out.println("Navios restantes  |  Voce: " + contarVivos(ownHp) + "  |  CPU: " + contarVivos(cpuHp));

            if (frotaAfundada(cpuHp)) {
                System.out.println("VITORIA! Voce afundou toda a frota inimiga.");
                vencedor = "Jogador";
                break;
            }
            if (frotaAfundada(ownHp)) {
                System.out.println("DERROTA. Sua frota foi afundada.");
                vencedor = "CPU";
                break;
            }

            turno++;

            if (playerTurn) {
                System.out.println();
                System.out.println("Seu turno.");
                System.out.println("Acoes: 1) Atirar  2) Ver log  3) Ajuda");
                System.out.print("> ");
                String opt = sc.nextLine().trim();

                if ("2".equals(opt)) {
                    System.out.println("Ultimas jogadas:");
                    int inicio2 = Math.max(0, jogadas.size() - 10);
                    for (int i = inicio2; i < jogadas.size(); i++) {
                        String[] j = jogadas.get(i);
                        System.out.println("  " + j[0] + " -> " + j[1] + ": " + j[2]);
                    }
                    turno--;
                    continue;
                }
                if ("3".equals(opt)) {
                    System.out.println("Legenda: S=navio proprio, X=acerto, o=agua, .=vazio ou desconhecido");
                    turno--;
                    continue;
                }

                int sx = -1, sy = -1;
                while (true) {
                    System.out.print("Coordenada para atirar (ex B7): ");
                    String tiro = sc.nextLine().trim();
                    Coordenada coord = parseCoordenada(tiro, tamanho);
                    if (coord == null) {
                        System.out.println("Invalida.");
                        continue;
                    }
                    sx = coord.getX();
                    sy = coord.getY();
                    if (ownShots[sy][sx] != '.') {
                        System.out.println("Voce ja atirou ai.");
                        continue;
                    }
                    break;
                }

                int result = applyShot(cpuShips, cpuShipId, cpuHp, sx, sy);
                String coordStr = formatarCoordenada(sx, sy);
                String resultStr;

                if (result == 0) {
                    ownShots[sy][sx] = 'o';
                    resultStr = "AGUA";
                    System.out.println("AGUA.");
                } else {
                    ownShots[sy][sx] = 'X';
                    if (result == 2) {
                        resultStr = "AFUNDOU";
                        System.out.println("AFUNDOU o navio inimigo: " + nomesNavios[cpuShipId[sy][sx]]);
                    } else {
                        resultStr = "ACERTO";
                        System.out.println("ACERTO.");
                    }
                }

                jogadas.add(new String[]{"Jogador", coordStr, resultStr});

                boolean extraShot = config.isHitGrantsExtraShot() && result != 0;
                if (!extraShot) playerTurn = false;

            } else {
                System.out.println("Turno da CPU.");
                int tx = -1, ty = -1;

                while (!cpuTargets.isEmpty()) {
                    int[] t = cpuTargets.removeFirst();
                    int px = t[0], py = t[1];
                    if (px < 0 || px >= tamanho || py < 0 || py >= tamanho) continue;
                    if (cpuTried[py][px]) continue;
                    tx = px;
                    ty = py;
                    break;
                }

                if (tx == -1) {
                    int tries = 0;
                    while (tries < 5000) {
                        tries++;
                        int px = rng.nextInt(tamanho);
                        int py = rng.nextInt(tamanho);
                        if (cpuTried[py][px]) continue;
                        if (config.isCpuUseParityPreference()) {
                            if ((px + py) % 2 == 0 || rng.nextInt(100) < 25) {
                                tx = px;
                                ty = py;
                                break;
                            }
                        } else {
                            tx = px;
                            ty = py;
                            break;
                        }
                    }
                    if (tx == -1) {
                        outer:
                        for (int py = 0; py < tamanho; py++)
                            for (int px = 0; px < tamanho; px++)
                                if (!cpuTried[py][px]) {
                                    tx = px;
                                    ty = py;
                                    break outer;
                                }
                    }
                }

                cpuTried[ty][tx] = true;
                int result = applyShot(ownShips, ownShipId, ownHp, tx, ty);
                String coordStr = formatarCoordenada(tx, ty);
                String resultStr;

                if (result == 0) {
                    cpuShots[ty][tx] = 'o';
                    resultStr = "AGUA";
                    System.out.println("CPU errou em " + coordStr);
                } else {
                    cpuShots[ty][tx] = 'X';
                    if (result == 2) {
                        resultStr = "AFUNDOU";
                        System.out.println("CPU AFUNDOU seu navio: " + nomesNavios[ownShipId[ty][tx]]);
                        if (rng.nextInt(100) < 60) cpuTargets.clear();
                    } else {
                        resultStr = "ACERTO";
                        System.out.println("CPU acertou em " + coordStr);
                        cpuTargets.addLast(new int[]{tx + 1, ty});
                        cpuTargets.addLast(new int[]{tx - 1, ty});
                        cpuTargets.addLast(new int[]{tx, ty + 1});
                        cpuTargets.addLast(new int[]{tx, ty - 1});
                    }
                }

                jogadas.add(new String[]{"CPU", coordStr, resultStr});
                playerTurn = true;
            }
        }

        if (repo != null) {
            String fim = java.time.LocalDateTime.now().toString();
            long partidaId = repo.salvarPartida(inicio, fim, vencedor, String.valueOf(seedUsada));
            repo.salvarJogador(partidaId, "Jogador", "humano");
            repo.salvarJogador(partidaId, "CPU", "cpu");
            int t = 0;
            for (String[] j : jogadas) {
                t++;
                repo.salvarJogada(partidaId, t, j[0], j[1], j[2]);
            }
            System.out.println("Partida salva com ID " + partidaId + ".");
        }

        System.out.println();
        System.out.print("Mostrar log completo? (s/N): ");
        String show = sc.nextLine().trim().toLowerCase(Locale.ROOT);
        if (show.equals("s") || show.equals("sim")) {
            for (int i = 0; i < jogadas.size(); i++) {
                String[] j = jogadas.get(i);
                System.out.println((i + 1) + ") " + j[0] + " -> " + j[1] + ": " + j[2]);
            }
        }

        sc.close();
    }

    static void fill(char[][] b, char c) {
        for (char[] row : b) Arrays.fill(row, c);
    }

    static void fillInt(int[][] b, int v) {
        for (int[] row : b) Arrays.fill(row, v);
    }

    static Coordenada parseCoordenada(String texto, int tamanho) {
        if (texto == null) return null;
        String s = texto.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (s.length() < 2 || s.length() > 3) return null;
        char col = s.charAt(0);
        int x = col - 'A';
        if (x < 0 || x >= tamanho) return null;
        int linha;
        try {
            linha = Integer.parseInt(s.substring(1));
        } catch (Exception e) {
            return null;
        }
        if (linha < 1 || linha > tamanho) return null;
        return new Coordenada(x, linha - 1);
    }

    static String formatarCoordenada(int x, int y) {
        return "" + (char) ('A' + x) + (y + 1);
    }

    static boolean podePosicionarNavio(char[][] board, int x, int y, int len, boolean horiz, int tamanho) {
        if (x < 0 || x >= tamanho || y < 0 || y >= tamanho) return false;
        if (horiz) {
            if (x + len > tamanho) return false;
            for (int i = 0; i < len; i++) {
                if (board[y][x + i] != '.') return false;
                if (temNavioEncostando(board, x + i, y, tamanho)) return false;
            }
        } else {
            if (y + len > tamanho) return false;
            for (int i = 0; i < len; i++) {
                if (board[y + i][x] != '.') return false;
                if (temNavioEncostando(board, x, y + i, tamanho)) return false;
            }
        }
        return true;
    }

    static boolean temNavioEncostando(char[][] board, int x, int y, int tamanho) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (nx < 0 || nx >= tamanho || ny < 0 || ny >= tamanho) continue;
                if (board[ny][nx] == 'S') return true;
            }
        }
        return false;
    }

    static void colocarNavio(char[][] board, int[][] shipId, int x, int y, int len, boolean horiz, int sid) {
        if (horiz) {
            for (int i = 0; i < len; i++) {
                board[y][x + i] = 'S';
                shipId[y][x + i] = sid;
            }
        } else {
            for (int i = 0; i < len; i++) {
                board[y + i][x] = 'S';
                shipId[y + i][x] = sid;
            }
        }
    }

    static int applyShot(char[][] ships, int[][] shipId, int[] hp, int x, int y) {
        if (ships[y][x] != 'S') return 0;
        ships[y][x] = 'X';
        int sid = shipId[y][x];
        if (sid >= 0) {
            hp[sid]--;
            if (hp[sid] == 0) return 2;
        }
        return 1;
    }

    static int contarVivos(int[] hp) {
        int c = 0;
        for (int v : hp) if (v > 0) c++;
        return c;
    }

    static boolean frotaAfundada(int[] hp) {
        for (int v : hp) if (v > 0) return false;
        return true;
    }

    static void imprimirDoisTabuleiros(char[][] ownShips, char[][] ownShots, int tamanho, boolean legenda) {
        StringBuilder cab = new StringBuilder("    ");
        for (int x = 0; x < tamanho; x++) cab.append((char) ('A' + x)).append(' ');
        String cabStr = cab.toString();

        System.out.printf("%-34s | %s%n", "SEU TABULEIRO", "TIROS NO INIMIGO");
        System.out.printf("%-34s | %s%n", cabStr, cabStr);

        for (int y = 0; y < tamanho; y++) {
            StringBuilder esq = new StringBuilder(String.format("%2d  ", y + 1));
            StringBuilder dir = new StringBuilder(String.format("%2d  ", y + 1));
            for (int x = 0; x < tamanho; x++) esq.append(ownShips[y][x]).append(' ');
            for (int x = 0; x < tamanho; x++) dir.append(ownShots[y][x]).append(' ');
            System.out.println(esq + "  |  " + dir);
        }

        if (legenda) System.out.println("Legenda: S=navio, X=acerto, o=agua, .=vazio");
    }

    static void imprimirTabuleiro(String titulo, char[][] b, boolean mostrarNavios, int tamanho) {
        System.out.println(titulo);
        StringBuilder cab = new StringBuilder("    ");
        for (int x = 0; x < tamanho; x++) cab.append((char) ('A' + x)).append(' ');
        System.out.println(cab);
        for (int y = 0; y < tamanho; y++) {
            StringBuilder row = new StringBuilder(String.format("%2d  ", y + 1));
            for (int x = 0; x < tamanho; x++) {
                char c = b[y][x];
                if (!mostrarNavios && c == 'S') c = '.';
                row.append(c).append(' ');
            }
            System.out.println(row);
        }
    }
}
