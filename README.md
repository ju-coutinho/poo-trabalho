# Batalha Naval

Projeto de refatoracao de POO 2026-1.

## Como compilar e rodar

Requer Java 17+ e Maven instalados.

```bash
mvn compile
mvn exec:java -Dexec.mainClass=Main
```

Ou gerar o jar e rodar:

```bash
mvn package
java -jar target/batalha-naval.jar
```

## Como rodar os testes

Os testes automatizados foram feitos com JUnit 5 e ficam em `src/test/java`.

Para executar:

```bash
mvn test
```

A suíte de testes cobre:

- coordenadas válidas e inválidas;
- posicionamento de navios, incluindo limites do tabuleiro e colisão/encostar em outro navio;
- aplicação de tiros, incluindo água, acerto e afundamento;
- verificação de fim de jogo quando toda a frota é afundada;
- validação dos tamanhos da frota;
- alternância de turno entre jogador humano e CPU.

Ao final da execução, o Maven deve mostrar `BUILD SUCCESS`.

## Modos de execucao

Configure o arquivo `game.properties` na raiz do projeto:

- `game.mode=PLAY` — jogo normal (padrao)
- `game.mode=LIST` — lista partidas salvas no banco
- `game.mode=REPLAY` — reproduz uma partida salva

## Banco de dados

O projeto usa SQLite via JDBC para salvar o histórico das partidas.

As configurações ficam no arquivo `game.properties`:

```properties
db.enabled=true
db.auto_migrate=true
db.sqlite.file=data/batalha_naval.db
```

Quando `db.enabled=true`, ao final da partida o sistema salva:

- dados da partida: início, fim, vencedor e seed;
- jogadores: nome e tipo, humano ou CPU;
- jogadas: turno, jogador, coordenada e resultado.

O arquivo do banco é criado automaticamente na pasta `data/` quando `db.auto_migrate=true`.

Para listar partidas salvas, configure:

```properties
game.mode=LIST
```

Para reproduzir uma partida salva, configure:

```properties
game.mode=REPLAY
```

Depois execute o programa normalmente.

## Dependencias

Gerenciadas pelo Maven:
- sqlite-jdbc 3.45.1.0
- JUnit Jupiter 5.10.2
