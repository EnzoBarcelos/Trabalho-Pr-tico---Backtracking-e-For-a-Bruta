import modelo.Restricao;
import modelo.Simbolo;
import modelo.Tabuleiro;
import solver.Validador;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Ferramenta auxiliar (fora da entrega) para gerar instancias: monta uma solucao valida,
// deriva algumas restricoes = / x dela e revela dicas ate a instancia ter solucao unica.
public class GeradorInstancias {

    static Validador val = new Validador();

    public static void main(String[] args) throws IOException {
        // instancias com solucao unica (verificada por contagem) - usadas nos exemplos
        gerar(4, 42, 2, "entradas/teste_4x4.txt", "4x4 didatico");
        gerar(6, 7,  6, "entradas/facil_6x6.txt", "6x6 facil");
        gerar(6, 99, 5, "entradas/medio_6x6.txt", "6x6 medio");
        gerar(8, 123, 8, "entradas/dificil_8x8.txt", "8x8 dificil");

        // instancias maiores/mais esparsas (por fracao de dicas, sem exigir unicidade)
        gerarPorFracao(6,  21, 4,  0.61, "entradas/vazio_6x6.txt",   "6x6 mais vazio");
        gerarPorFracao(8,  55, 8,  0.70, "entradas/esparso_8x8.txt", "8x8 esparso");
        gerarPorFracao(10, 77, 12, 0.50, "entradas/enorme_10x10.txt", "10x10 grande");

        System.out.println("Instancias geradas.");
    }

    // Revela uma FRACAO de celulas a partir de uma solucao valida, sem exigir unicidade.
    static void gerarPorFracao(int n, long seed, int numRestricoes, double fracVazias,
                               String caminho, String desc) throws IOException {
        Random rnd = new Random(seed);

        Simbolo[][] sol = new Simbolo[n][n];
        Tabuleiro v = new Tabuleiro(n);
        if (!preencheAleatorio(v, v.celulasLivres(), 0, rnd)) {
            throw new IllegalStateException("nao gerou solucao para n=" + n);
        }
        for (int r = 0; r < n; r++) sol[r] = v.grade[r].clone();

        Tabuleiro inst = new Tabuleiro(n);
        adicionaRestricoes(inst, sol, numRestricoes, rnd);

        // revela (1 - fracVazias) * n*n celulas como dicas
        List<int[]> celulas = new ArrayList<>();
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) celulas.add(new int[]{r, c});
        Collections.shuffle(celulas, rnd);
        int numDicas = (int) Math.round((1.0 - fracVazias) * n * n);
        for (int i = 0; i < numDicas; i++) {
            int r = celulas.get(i)[0], c = celulas.get(i)[1];
            inst.grade[r][c] = sol[r][c];
            inst.fixa[r][c] = true;
        }

        escrever(caminho, inst, desc);
        int vazias = n * n - numDicas;
        System.out.println(caminho + " -> n=" + n + " dicas=" + numDicas + " vazias=" + vazias);
    }

    static void adicionaRestricoes(Tabuleiro inst, Simbolo[][] sol, int numRestricoes, Random rnd) {
        int n = inst.n;
        List<int[]> todos = new ArrayList<>();
        for (int r = 0; r < n; r++) for (int c = 0; c < n - 1; c++) todos.add(new int[]{0, r, c});
        for (int r = 0; r < n - 1; r++) for (int c = 0; c < n; c++) todos.add(new int[]{1, r, c});
        Collections.shuffle(todos, rnd);
        int posto = 0;
        for (int[] x : todos) {
            if (posto >= numRestricoes) break;
            int tipo = x[0], r = x[1], c = x[2];
            Simbolo a = sol[r][c];
            Simbolo b = (tipo == 0) ? sol[r][c + 1] : sol[r + 1][c];
            Restricao rest = (a == b) ? Restricao.IGUAL : Restricao.OPOSTO;
            if (tipo == 0) inst.restricaoH[r][c] = rest; else inst.restricaoV[r][c] = rest;
            posto++;
        }
    }

    static void gerar(int n, long seed, int numRestricoes, String caminho, String desc) throws IOException {
        Random rnd = new Random(seed);

        // 1) solucao completa
        Simbolo[][] sol = new Simbolo[n][n];
        Tabuleiro vazio = new Tabuleiro(n);
        if (!preencheAleatorio(vazio, vazio.celulasLivres(), 0, rnd)) {
            throw new IllegalStateException("nao gerou solucao para n=" + n);
        }
        for (int r = 0; r < n; r++) sol[r] = vazio.grade[r].clone();

        // 2) restricoes derivadas da solucao
        Tabuleiro inst = new Tabuleiro(n);
        List<int[]> candidatosH = new ArrayList<>();
        for (int r = 0; r < n; r++) for (int c = 0; c < n - 1; c++) candidatosH.add(new int[]{0, r, c});
        List<int[]> candidatosV = new ArrayList<>();
        for (int r = 0; r < n - 1; r++) for (int c = 0; c < n; c++) candidatosV.add(new int[]{1, r, c});
        List<int[]> todos = new ArrayList<>();
        todos.addAll(candidatosH);
        todos.addAll(candidatosV);
        Collections.shuffle(todos, rnd);
        int posto = 0;
        for (int[] x : todos) {
            if (posto >= numRestricoes) break;
            int tipo = x[0], r = x[1], c = x[2];
            Simbolo a, b;
            if (tipo == 0) { a = sol[r][c]; b = sol[r][c + 1]; }
            else           { a = sol[r][c]; b = sol[r + 1][c]; }
            Restricao rest = (a == b) ? Restricao.IGUAL : Restricao.OPOSTO;
            if (tipo == 0) inst.restricaoH[r][c] = rest; else inst.restricaoV[r][c] = rest;
            posto++;
        }

        // 3) revela dicas ate solucao unica
        List<int[]> celulas = new ArrayList<>();
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) celulas.add(new int[]{r, c});
        Collections.shuffle(celulas, rnd);

        while (true) {
            int sols = contaSolucoes(inst, 2);
            if (sols == 1) break;
            // revela proxima dica
            boolean revelou = false;
            for (int[] cel : celulas) {
                int r = cel[0], c = cel[1];
                if (!inst.fixa[r][c]) {
                    inst.grade[r][c] = sol[r][c];
                    inst.fixa[r][c] = true;
                    revelou = true;
                    break;
                }
            }
            if (!revelou) break; // todas reveladas (sera unica)
        }

        escrever(caminho, inst, desc);
        int vazias = 0;
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) if (!inst.fixa[r][c]) vazias++;
        System.out.println(caminho + " -> n=" + n + " dicas=" + (n * n - vazias) + " vazias=" + vazias);
    }

    // backtracking que conta solucoes ate um limite (para checar unicidade)
    static int contaSolucoes(Tabuleiro base, int limite) {
        Tabuleiro t = base.clonar();
        return conta(t, t.celulasLivres(), 0, limite);
    }

    static int conta(Tabuleiro t, int[][] livres, int idx, int limite) {
        if (idx == livres.length) {
            return val.tabuleiroCompletoValido(t) ? 1 : 0;
        }
        int r = livres[idx][0], c = livres[idx][1];
        int total = 0;
        for (Simbolo s : new Simbolo[]{Simbolo.SOL, Simbolo.LUA}) {
            t.grade[r][c] = s;
            if (val.movimentoValido(t, r, c)) {
                total += conta(t, livres, idx + 1, limite);
                if (total >= limite) { t.grade[r][c] = Simbolo.VAZIO; return total; }
            }
        }
        t.grade[r][c] = Simbolo.VAZIO;
        return total;
    }

    static boolean preencheAleatorio(Tabuleiro t, int[][] livres, int idx, Random rnd) {
        if (idx == livres.length) return val.tabuleiroCompletoValido(t);
        int r = livres[idx][0], c = livres[idx][1];
        Simbolo[] ordem = rnd.nextBoolean()
                ? new Simbolo[]{Simbolo.SOL, Simbolo.LUA}
                : new Simbolo[]{Simbolo.LUA, Simbolo.SOL};
        for (Simbolo s : ordem) {
            t.grade[r][c] = s;
            if (val.movimentoValido(t, r, c) && preencheAleatorio(t, livres, idx + 1, rnd)) return true;
        }
        t.grade[r][c] = Simbolo.VAZIO;
        return false;
    }

    static void escrever(String caminho, Tabuleiro t, String desc) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Instancia de Tango - ").append(desc).append("\n");
        sb.append("# S=Sol  L=Lua  .=vazia ; restricoes: H/V linha coluna (=/x)\n");
        sb.append(t.n).append("\n");
        for (int r = 0; r < t.n; r++) {
            StringBuilder linha = new StringBuilder();
            for (int c = 0; c < t.n; c++) linha.append(t.grade[r][c].c);
            sb.append(linha).append("\n");
        }
        for (int r = 0; r < t.n; r++)
            for (int c = 0; c < t.n - 1; c++)
                if (t.restricaoH[r][c] != Restricao.NENHUMA)
                    sb.append("H ").append(r).append(" ").append(c).append(" ")
                      .append(t.restricaoH[r][c] == Restricao.IGUAL ? "=" : "x").append("\n");
        for (int r = 0; r < t.n - 1; r++)
            for (int c = 0; c < t.n; c++)
                if (t.restricaoV[r][c] != Restricao.NENHUMA)
                    sb.append("V ").append(r).append(" ").append(c).append(" ")
                      .append(t.restricaoV[r][c] == Restricao.IGUAL ? "=" : "x").append("\n");
        try (FileWriter w = new FileWriter(caminho)) {
            w.write(sb.toString());
        }
    }
}
