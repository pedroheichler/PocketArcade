# Pocket Arcade · APP05

Central de Desafios para Android, feita em **Java + XML** com **View Binding**, para o trabalho avaliativo de
*Desenvolvimento de Aplicações para Dispositivos Móveis* (Prof. Fernando Gonçalves Abadia).

São **seis Activities** e quatro desafios, com visual "neon arcade" próprio:

| Tela | Activity | O que faz |
|------|----------|-----------|
| 1 · Apresentação | `ApresentacaoActivity` | Dados do integrante, logotipo, apelido validado (mín. 3 caracteres), botão animado com `ViewPropertyAnimator` |
| 2 · Central de Desafios | `CentralActivity` | Menu rolável (`NestedScrollView` → um único `LinearLayout`), saudação com o apelido, 4 painéis com entrada em cascata (animação XML) |
| 3 · Scanner de Mensagens | `ScannerActivity` | Caracteres sem espaços, palavras, vogais, texto invertido e palíndromo |
| 4 · Arena | `ArenaActivity` | Pedra, Papel e Tesoura melhor de três, animações e 3 efeitos sonoros |
| 5 · Cofre do Código Secreto | `CofreActivity` | 4 dígitos diferentes, 8 tentativas, pistas verde/amarelo/cinza (estilo TERMO), tempo com `System.currentTimeMillis()` |
| 6 · Randomizador | `RandomizadorActivity` | `RecyclerView` com cadastro, exclusão por clique longo, lista/grade e sorteio por categoria |

## Antes de entregar: troque seus dados

Em [`app/src/main/res/values/strings.xml`](app/src/main/res/values/strings.xml), altere:

- `integrante1_nome`, `integrante1_matricula`, `integrante1_curso`
- `modalidade` (ex.: "Trabalho em dupla", se for o caso)

## Como rodar

Abra a pasta no Android Studio e use **Run ▶**, ou pela linha de comando:

```bash
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # 23 testes JUnit da lógica dos jogos
./gradlew installDebug         # instala no emulador/celular conectado
```

Requisitos: JDK 17+, Android SDK 36. O caminho do projeto tem acentos ("Programação"), por isso o
`gradle.properties` usa `android.overridePathCheck=true`.

## Onde cada requisito está

**Navegação, extras e pilha** — [`Navegacao.java`](app/src/main/java/br/com/pocketarcade/Navegacao.java)

- Chaves dos extras em constantes (`EXTRA_APELIDO`, `EXTRA_ULTIMO_MODULO`) e validação no destino (`lerApelido`).
  A Central fecha com `finish()` se o apelido faltar.
- A seta de cada desafio chama `finish()`: volta para a Central que já está na pilha.
- **Encerrar** usa `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP` e a Tela 1 recebe o retorno em
  `onNewIntent()`, mostrando o último módulo usado.
- Todas as Activities registram `Log.d()` em `onCreate`, `onResume`, `onStop` e `onDestroy`.

**MediaPlayer** — `ArenaActivity`, `CofreActivity` e `RandomizadorActivity`

- Players criados uma vez em `onCreate()` e reutilizados (`seekTo(0)` + `start()`), nunca dentro de listeners.
- `onPause()` faz `pause()` (sem `stop()`); `onDestroy()` faz `release()`.
- Sons em `res/raw`: `som_vitoria`, `som_derrota`, `som_empate`, `som_sorteio`.

**Animações**

- XML em `res/anim`: `painel_entrada` + `layout_cascata` (Central), `resultado_entrada` (Scanner e aviso da Tela 1),
  `tremer` (entradas inválidas e derrota no Cofre).
- `ViewPropertyAnimator`: botão de entrar, escolhas e resultado da Arena (300–700 ms), dígitos do Cofre, dado do sorteio.

**RecyclerView** — pacote [`randomizador`](app/src/main/java/br/com/pocketarcade/randomizador)

- Modelo `ItemSorteio`, `ItemSorteioAdapter` com `ItemViewHolder` (guarda o binding do item).
- `notifyItemInserted()` + `smoothScrollToPosition()` ao cadastrar; `notifyItemRemoved()` no clique longo.
- Alterna `LinearLayoutManager` e `GridLayoutManager`.

**Lógica pura e testada** — pacote [`logica`](app/src/main/java/br/com/pocketarcade/logica), testes em
[`app/src/test`](app/src/test/java/br/com/pocketarcade/logica): `AnalisadorTexto`, `PartidaArena`, `CodigoSecreto`.

**Layouts** — `ConstraintLayout` na Tela 1 e nos painéis da Central; `LinearLayout` e `NestedScrollView` nas demais.
A Tela 1 tem uma `NestedScrollView` por fora do `ConstraintLayout` para caber em telas pequenas com o teclado aberto.

**Recursos** — textos em `strings.xml`/`arrays.xml`, cores em `colors.xml`, dimensões em `dimens.xml`, durações em
`integers.xml`, `contentDescription` em todas as imagens e `inputType` adequado em cada campo.

**Não usado:** `findViewById`, `android:onClick`, banco de dados, internet (o app não pede a permissão),
`Handler`, `Thread`, `Runnable` e Jetpack Compose. Os fins de animação usam `AnimatorListenerAdapter`.

## Roteiro sugerido para o vídeo

1. No Logcat, filtre por `tag:PocketArcade` para mostrar o ciclo de vida durante todo o vídeo.
2. Tela 1: tente entrar vazio e com 2 letras (erros), depois entre com um apelido válido.
3. Central: role até o fim; abra cada desafio e volte pela seta (`finish()`) e pelo botão Voltar do sistema.
4. Scanner: "a b" (erro), depois "A sacada da casa" (palíndromo) e Limpar.
5. Arena: jogue até alguém fazer 2 pontos, mostre um empate, o som e Nova partida.
6. Cofre: teste "1123" (erro de repetição); o código sorteado aparece no Logcat para facilitar a demonstração.
7. Randomizador: adicione um item, segure para excluir, alterne lista/grade e sorteie em duas categorias.
8. Toque em **Encerrar** dentro de um desafio: a Tela 1 volta mostrando o último módulo, e o Logcat mostra
   `onNewIntent()` e o `onDestroy()` das telas fechadas.

## Créditos

- **Ícones e logotipo:** desenhados em XML (VectorDrawable) especialmente para este projeto, em `res/drawable`.
- **Efeitos sonoros:** sintetizados pelo script [`tools/gerar_sons.py`](tools/gerar_sons.py) (ondas quadradas no
  estilo 8 bits). Para gerar de novo: `python tools/gerar_sons.py`.
- Nenhuma imagem ou som de terceiros foi usado.
