# Trabalho Prático 2 — Backtracking e Força Bruta

Resolvedor do quebra-cabeça lógico **Tango** (um Problema de Satisfação de
Restrições) implementado em **Java puro**, comparando duas estratégias de busca:

- **Força Bruta** — gera todas as combinações e só valida o tabuleiro completo.
- **Backtracking** — valida a cada jogada e **poda** ramos inválidos cedo.

O projeto mede e compara as duas estratégias (estados explorados, validações,
podas e tempo) sobre sete instâncias e gera gráficos para a documentação.

> Disciplina: **Fundamentos de Projeto e Análise de Algoritmos (FPAA)** — PUC Minas.

---

## 📋 Índice

- [Início rápido (TL;DR)](#-início-rápido-tldr)
- [Pré-requisitos](#-pré-requisitos)
- [Como compilar](#-como-compilar)
- [Como executar](#-como-executar)
- [Exemplo de saída real](#-exemplo-de-saída-real)
- [Resultados do benchmark](#-resultados-do-benchmark)
- [As regras do Tango](#-as-regras-do-tango)
- [Formato dos arquivos de entrada](#-formato-dos-arquivos-de-entrada)
- [Instâncias de teste](#-instâncias-de-teste)
- [Reproduzir os gráficos](#-reproduzir-os-gráficos)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Decisões de projeto](#-decisões-de-projeto)
- [Solução de problemas](#-solução-de-problemas)

---

## ⚡ Início rápido (TL;DR)

Abra o terminal **na pasta raiz do projeto** (a que contém `src/` e `entradas/`)
e rode os **dois comandos** abaixo. Eles são idênticos no Windows
(PowerShell ou CMD), Linux e macOS:

```bash
javac -d out -sourcepath src src/Main.java
java -cp out Main
```

O primeiro compila tudo; o segundo abre o menu. No menu, digite **3** para rodar
os auto-testes (deve mostrar `18/18 testes passaram`) ou **2** para gerar os CSVs
do benchmark. Pronto.

> ⚠️ **Rode sempre a partir da raiz do projeto.** Os caminhos das instâncias
> (`entradas/...`) são relativos a ela; de outra pasta o programa não acha os arquivos.

---

## 🔧 Pré-requisitos

| Ferramenta | Versão | Para quê | Como verificar |
|---|---|---|---|
| **JDK** (Java) | 8 ou superior (testado no **JDK 24**) | compilar e executar | `java -version` e `javac -version` |
| Python 3 + `matplotlib` + `numpy` | qualquer 3.x | **opcional**, só para regerar os gráficos | `python --version` |

- O programa principal é **100% Java**, sem bibliotecas externas nem framework de
  teste — só precisa do JDK.
- Se `javac` não for reconhecido, o JDK não está instalado **ou** não está no
  `PATH`. Baixe em [adoptium.net](https://adoptium.net) (Temurin) e reabra o terminal.
- Python é necessário **apenas** se você quiser regerar os `.png`. Os gráficos já
  vêm prontos no repositório; rodar o trabalho não exige Python.

---

## 🛠 Como compilar

**Forma recomendada** (um comando, funciona em qualquer sistema/terminal):

```bash
javac -d out -sourcepath src src/Main.java
```

O `-sourcepath src` faz o compilador resolver e compilar automaticamente todas as
classes referenciadas a partir de `Main.java`. Os `.class` vão para a pasta `out/`.

<details>
<summary>Alternativa: listar os pacotes explicitamente</summary>

Funciona igual (em qualquer shell do Windows, Linux ou macOS):

```bash
javac -d out src/Main.java src/modelo/*.java src/io/*.java src/solver/*.java src/benchmark/*.java src/teste/*.java
```
</details>

---

## ▶ Como executar

```bash
java -cp out Main
```

Abre um menu de console com quatro opções:

| Opção | O que faz |
|:---:|---|
| **1** | **Resolver uma instância** — mostra o tabuleiro inicial (com dicas e restrições) e o tabuleiro final resolvido por **Força Bruta** e por **Backtracking**, com os contadores (estados, validações, podas, tempo). |
| **2** | **Benchmark** — roda as duas estratégias sobre as 7 instâncias e grava `benchmark_tango.csv` e `perfil_nivel.csv`. |
| **3** | **Auto-testes** — roda a suíte de testes (corretude das duas estratégias, concordância entre elas e rejeição de cada uma das 5 regras). Deve terminar em `18/18 testes passaram`. |
| **0** | Sair. |

Na opção 1 você pode escolher uma das 7 instâncias pelo número **ou** digitar o
caminho de um arquivo `.txt` próprio.

> 💡 **Por onde começar a avaliar:** rode a opção **3** (prova que tudo está
> correto: 18/18) e depois a opção **1 → 1** (vê na prática a diferença entre as
> estratégias num caso pequeno).

---

## 📺 Exemplo de saída real

Saída da opção **1** com a instância **`teste_4x4`** (capturada da execução real).
Repare na diferença gritante de **estados explorados**: Força Bruta = **408**,
Backtracking = **12**, ambos chegando à **mesma** solução válida.

```
>>> Tabuleiro INICIAL (4x4):
 S   .   S   L
 x
 .   . x L   S
 .   .   .   L
 L   S   .   .

----- Forca Bruta -----
 S   L   S   L
 x
 L   S x L   S
 S   L   S   L
 L   S   L   S
Estados=408  Validacoes=203  Podas=0  Tempo=0,141 ms  Valido=sim

----- Backtracking -----
 S   L   S   L
 x
 L   S x L   S
 S   L   S   L
 L   S   L   S
Estados=12  Validacoes=12  Podas=4  Tempo=0,035 ms  Valido=sim
```

Como ler o tabuleiro: `S` = Sol, `L` = Lua, `.` = célula vazia. Os símbolos `=` e
`x` **entre** as células são as restrições de igualdade e oposição.

---

## 📊 Resultados do benchmark

Valores de **estados** e **podas** são **determinísticos** (reproduzem exatamente
o `benchmark_tango.csv` versionado); os tempos variam um pouco a cada execução.
`K` = número de células vazias = expoente do espaço de busca `2^K`.

| Instância | n | K | BT estados | BT podas | FB estados | FB explora ~N× mais |
|---|:---:|:---:|---:|---:|---:|---:|
| `teste_4x4`    | 4  | 8  | 12  | 4   | 408 | 34× |
| `facil_6x6`    | 6  | 10 | 15  | 5   | 689 | 46× |
| `medio_6x6`    | 6  | 20 | 31  | 10  | 683.347 | ~22.000× |
| `vazio_6x6`    | 6  | 22 | 32  | 9   | 427.862 | ~13.000× |
| `dificil_8x8`  | 8  | 35 | 254 | 118 | 2³⁵ ≈ 3,4×10¹⁰ *(inviável)* | — |
| `esparso_8x8`  | 8  | 45 | 298 | 138 | 2⁴⁵ ≈ 3,5×10¹³ *(inviável)* | — |
| `enorme_10x10` | 10 | 50 | 151 | 64  | 2⁵⁰ ≈ 1,1×10¹⁵ *(inviável)* | — |

**Leitura dos resultados:**

- O **Backtracking** resolve todas as instâncias explorando de dezenas a poucas
  centenas de estados — graças às podas, que cortam ramos inválidos cedo.
- A **Força Bruta** cresce de forma explosiva (`2^K`) e se torna **inviável**
  a partir do 8×8: nessas instâncias o programa **pula** a execução e registra
  apenas o limite teórico `2^K` (acima de 25 células vazias, `2^K` é grande demais).
- Tanto Força Bruta quanto Backtracking **param na primeira solução encontrada** —
  por isso o nº de estados da FB não cresce monotonicamente com `K` (a solução
  pode aparecer "mais cedo" na varredura, como em `vazio_6x6`).

Esses números embasam os cinco gráficos usados na documentação técnica
(`artigo.tex`): `grafico_estados.png`, `grafico_tempo.png`,
`grafico_estados_por_instancia.png`, `grafico_speedup.png` e
`grafico_perfil_nivel.png`.

---

## 🧩 As regras do Tango

Grade `N × N` (com **N par**), cada célula contendo Sol (`S`) ou Lua (`L`):

1. **Preenchimento:** toda célula recebe um símbolo.
2. **Sem trincas:** não pode haver três símbolos iguais consecutivos (na horizontal
   ou na vertical).
3. **Equilíbrio:** cada linha e cada coluna têm exatamente `N/2` de cada símbolo.
4. **Igualdade (`=`):** células ligadas por `=` têm o **mesmo** símbolo.
5. **Oposição (`x`):** células ligadas por `x` têm símbolos **opostos**.

As dicas (células já preenchidas no arquivo) e as restrições `=`/`x` são fixas e
nunca são alteradas pela busca.

---

## 📄 Formato dos arquivos de entrada

Arquivo de texto simples (veja exemplos em `entradas/`):

```
6                <- N: tamanho da grade (par e positivo)
SSLL..           <- N linhas da grade: S=Sol, L=Lua, .=vazia
.SLSLL
L..S.S
SLSLSL
L.LSLS
L.S..S
H 0 4 x          <- restrição horizontal entre (0,4) e (0,5): símbolos opostos
V 0 1 =          <- restrição vertical entre (0,1) e (1,1): símbolos iguais
```

- `H r c <sinal>` → liga `(r,c)` a `(r,c+1)` (vizinho à direita).
- `V r c <sinal>` → liga `(r,c)` a `(r+1,c)` (vizinho abaixo).
- `<sinal>` é `=` (igualdade) ou `x` (oposição).
- Coordenadas começam em **0**.
- Linhas em branco e linhas iniciadas por `#` são ignoradas (comentários).

---

## 🗂 Instâncias de teste

As sete instâncias em `entradas/` cobrem um espectro crescente de dificuldade,
para evidenciar onde a Força Bruta deixa de ser viável:

| Arquivo | Grade | Células vazias (K) | Observação |
|---|:---:|:---:|---|
| `teste_4x4.txt`    | 4×4   | 8  | menor caso; as duas estratégias rodam rápido |
| `facil_6x6.txt`    | 6×6   | 10 | ambas rodam |
| `medio_6x6.txt`    | 6×6   | 20 | FB já leva ~7 ms (centenas de milhares de estados) |
| `vazio_6x6.txt`    | 6×6   | 22 | quase sem dicas |
| `dificil_8x8.txt`  | 8×8   | 35 | FB inviável; só Backtracking |
| `esparso_8x8.txt`  | 8×8   | 45 | FB inviável; só Backtracking |
| `enorme_10x10.txt` | 10×10 | 50 | maior caso; FB inviável |

---

## 📈 Reproduzir os gráficos

Os `.png` já vêm prontos. Para regerá-los a partir dos dados:

```bash
# 1) gere os CSVs rodando o benchmark (opção 2 no menu)
java -cp out Main         # escolha 2, depois 0

# 2) gere os gráficos a partir dos CSVs
python gerar_graficos.py
```

Isso recria os cinco `.png` usados em `artigo.tex`. Requer
`pip install matplotlib numpy`.

---

## 📁 Estrutura do projeto

```
.
├── src/
│   ├── Main.java                 menu de console (ponto de entrada)
│   ├── modelo/                   estruturas de dados
│   │   ├── Simbolo.java          enum Sol / Lua / Vazio
│   │   ├── Restricao.java        enum Nenhuma / Igual / Oposto
│   │   └── Tabuleiro.java        grade + dicas + restrições + clonagem
│   ├── io/
│   │   ├── LeitorTabuleiro.java  lê e valida o arquivo de entrada
│   │   └── ImpressoraTabuleiro.java  desenha o tabuleiro em ASCII
│   ├── solver/
│   │   ├── Solver.java           interface comum (contadores)
│   │   ├── Validador.java        as 5 regras isoladas (as podas)
│   │   ├── ForcaBruta.java       valida só no tabuleiro completo
│   │   └── Backtracking.java     valida a cada jogada e poda
│   ├── benchmark/
│   │   └── Benchmark.java        mede e grava os CSVs
│   └── teste/
│       └── Testes.java           18 auto-testes (sem framework externo)
├── entradas/                     7 instâncias de teste (.txt)
├── tools/
│   └── GeradorInstancias.java    utilitário auxiliar (fora da entrega)
├── gerar_graficos.py             gera os 5 gráficos a partir dos CSVs
├── benchmark_tango.csv           resultados do benchmark (gerado)
├── perfil_nivel.csv              estados por nível da recursão (gerado)
├── grafico_*.png                 gráficos da documentação (gerados)
├── artigo.tex                    documentação técnica (template SBC)
└── README.md                     este arquivo
```

---

## 🏗 Decisões de projeto

- **Validador isolado.** Toda a lógica das 5 regras (e, portanto, das podas) fica
  na classe `Validador`, separada da mecânica de recursão dos solvers — conforme
  pede o enunciado. Os solvers só decidem *quando* validar:
  - **Força Bruta** chama o validador **uma vez**, no tabuleiro completo (folha da
    recursão). Nunca poda.
  - **Backtracking** chama o validador **após cada jogada**; se a célula recém-
    preenchida viola alguma regra, o ramo inteiro é descartado (poda) sem descer
    mais fundo na recursão. É essa poda que evita a explosão combinatória.
- **Contadores comparáveis.** Ambos contam *estados* (cada símbolo colocado =
  um nó da árvore de busca), *validações* e *podas*, permitindo uma comparação
  justa entre as estratégias.
- **Medição honesta.** O benchmark descarta uma execução de *warm-up* (para o JIT)
  e reporta média ± desvio-padrão amostral sobre 5 repetições.
- **Sem dependências externas.** Java puro; os auto-testes não usam JUnit nem
  qualquer biblioteca — bastam `javac`/`java`.

---

## 🩹 Solução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| `'javac' não é reconhecido` / `command not found` | JDK ausente ou fora do `PATH` | Instale o JDK (ex.: [Temurin](https://adoptium.net)) e reabra o terminal |
| `Nao foi possivel ler 'entradas/...'` | Rodando de fora da raiz do projeto | `cd` para a pasta que contém `src/` e `entradas/` e rode de novo |
| `Erro: não foi possível encontrar ou carregar a classe principal Main` | Faltou compilar, ou `-cp` errado | Rode o `javac` primeiro e use exatamente `java -cp out Main` |
| Força Bruta "trava" numa instância grande | Esperado: `2^K` é gigante | A FB é **pulada** automaticamente acima de 25 células vazias — use o Backtracking |
| `python: command not found` ao gerar gráficos | Python não instalado | É **opcional**; os `.png` já vêm prontos. Para regerar: instale Python 3 + `matplotlib` + `numpy` |

---

> **Documentação técnica completa** (metodologia, análise de complexidade e
> discussão dos resultados) em `artigo.tex` — compile no [Overleaf](https://overleaf.com)
> usando o template SBC incluído (`sbc-template.sty`).
