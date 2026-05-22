# Batalha Naval

Projeto de refatoracao de POO 2026-1.

## Como compilar e rodar

Requer Java 17+ e Maven instalados.

```
mvn compile
mvn exec:java -Dexec.mainClass=Main
```

Ou gerar o jar e rodar:

```
mvn package
java -jar target/batalha-naval.jar
```

## Como rodar os testes

```
mvn test
```

## Modos de execucao

Configure o arquivo `game.properties` na raiz do projeto:

- `game.mode=PLAY` — jogo normal (padrao)
- `game.mode=LIST` — lista partidas salvas no banco
- `game.mode=REPLAY` — reproduz uma partida salva

## Banco de dados

Por padrao o banco esta desabilitado. Para ativar, edite o `game.properties`:

```
db.enabled=true
db.auto_migrate=true
db.sqlite.file=data/batalha_naval.db
```

O arquivo do banco e criado automaticamente na pasta `data/`.

## Dependencias

Gerenciadas pelo Maven:
- sqlite-jdbc 3.45.1.0
- JUnit Jupiter 5.10.2
