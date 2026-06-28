package teste;

import io.LeitorTabuleiro;
import modelo.Restricao;
import modelo.Simbolo;
import modelo.Tabuleiro;
import solver.Backtracking;
import solver.ForcaBruta;
import solver.Solver;
import solver.Validador;

// Auto-testes sem framework externo: cada check() imprime [OK]/[FALHA] e no fim lanca
// AssertionError se algo falhou. Cobre as duas estrategias, a concordancia entre elas, a
// poda, e a rejeicao de cada uma das 5 regras pelo Validador.
public class Testes {

    static int passou = 0;
    static int total = 0;

    static void check(boolean cond, String nome) {
        total++;
        if (cond) {
            passou++;
            System.out.println("[OK]    " + nome);
        } else {
            System.out.println("[FALHA] " + nome);
        }
    }

    public static void main(String[] args) throws Exception {
        Validador val = new Validador();

        // --- Estrategias resolvem e concordam (4x4) ---
        Tabuleiro base4 = LeitorTabuleiro.ler("entradas/teste_4x4.txt");

        ForcaBruta fb = new ForcaBruta();
        Tabuleiro tFb = base4.clonar();
        boolean okFb = fb.resolver(tFb);

        Backtracking bt = new Backtracking();
        Tabuleiro tBt = base4.clonar();
        boolean okBt = bt.resolver(tBt);

        check(okFb, "Forca Bruta resolve o 4x4");
        check(okBt, "Backtracking resolve o 4x4");
        check(val.tabuleiroCompletoValido(tFb), "solucao da Forca Bruta e valida (5 regras)");
        check(val.tabuleiroCompletoValido(tBt), "solucao do Backtracking e valida (5 regras)");
        check(mesmaGrade(tFb, tBt), "as duas estrategias chegam a MESMA solucao");
        check(bt.getEstadosExplorados() <= fb.getEstadosExplorados(),
                "Backtracking explora <= estados que a Forca Bruta");
        check(bt.getPodas() > 0, "Backtracking de fato poda ramos");
        check(fb.getPodas() == 0, "Forca Bruta nao poda (por definicao)");
        check(somaVetor(fb.getEstadosPorNivel()) == fb.getEstadosExplorados(),
                "FB: soma dos estados por nivel = total de estados");
        check(somaVetor(bt.getEstadosPorNivel()) == bt.getEstadosExplorados(),
                "BT: soma dos estados por nivel = total de estados");

        // --- 6x6 resolve e e valido ---
        Tabuleiro t6 = LeitorTabuleiro.ler("entradas/facil_6x6.txt");
        Backtracking bt6 = new Backtracking();
        boolean ok6 = bt6.resolver(t6);
        check(ok6 && val.tabuleiroCompletoValido(t6), "Backtracking resolve o 6x6 facil");

        // --- 8x8 resolve (so backtracking) ---
        Tabuleiro t8 = LeitorTabuleiro.ler("entradas/dificil_8x8.txt");
        Backtracking bt8 = new Backtracking();
        boolean ok8 = bt8.resolver(t8);
        check(ok8 && val.tabuleiroCompletoValido(t8), "Backtracking resolve o 8x8 dificil");

        // --- Validador rejeita violacao de cada regra ---
        check(!val.tabuleiroCompletoValido(comVazia()), "regra 1: rejeita tabuleiro incompleto");
        check(regra2Violada(val), "regra 2: rejeita 3 iguais seguidos (via movimentoValido)");
        check(!val.tabuleiroCompletoValido(comDesequilibrio()), "regra 3: rejeita linha desequilibrada");
        check(!val.tabuleiroCompletoValido(comIgualdadeViolada()), "regra 4: rejeita '=' violado");
        check(!val.tabuleiroCompletoValido(comOposicaoViolada()), "regra 5: rejeita 'x' violado");

        // --- Validador aceita um tabuleiro 4x4 correto ---
        check(val.tabuleiroCompletoValido(boardValido4()), "aceita um 4x4 totalmente correto");

        System.out.println();
        System.out.println(passou + "/" + total + " testes passaram.");
        if (passou != total) {
            throw new AssertionError("Ha testes falhando.");
        }
        System.out.println("TODOS OS TESTES PASSARAM.");
    }

    // ----- helpers de comparacao -----

    static long somaVetor(long[] v) {
        long s = 0;
        if (v != null) for (long x : v) s += x;
        return s;
    }

    static boolean mesmaGrade(Tabuleiro a, Tabuleiro b) {
        for (int r = 0; r < a.n; r++)
            for (int c = 0; c < a.n; c++)
                if (a.grade[r][c] != b.grade[r][c]) return false;
        return true;
    }

    // ----- tabuleiros sinteticos para testar o Validador -----

    // 4x4 valido conhecido (linhas e colunas com 2 e 2, sem 3 iguais).
    static Tabuleiro boardValido4() {
        return board(new String[]{
                "SLSL",
                "LSLS",
                "SLLS",
                "LSSL"
        });
    }

    static Tabuleiro comVazia() {
        Tabuleiro t = boardValido4();
        t.grade[0][0] = Simbolo.VAZIO;
        return t;
    }

    // Regra 2 isolada num 6x6: tres Sois seguidos NAO estouram o equilibrio (meta=3),
    // entao o unico motivo da rejeicao e a adjacencia. Testamos pelo movimentoValido.
    static boolean regra2Violada(Validador val) {
        Tabuleiro t = new Tabuleiro(6);
        t.grade[0][0] = Simbolo.SOL;
        t.grade[0][1] = Simbolo.SOL;
        t.grade[0][2] = Simbolo.SOL;
        return !val.movimentoValido(t, 0, 2);
    }

    static Tabuleiro comDesequilibrio() {
        // primeira linha L S L L: 1 Sol e 3 Luas (desequilibrada) e SEM trinca, para isolar a regra 3.
        return board(new String[]{
                "LSLL",
                "SLSS",
                "SLLS",
                "LSSL"
        });
    }

    static Tabuleiro comIgualdadeViolada() {
        Tabuleiro t = boardValido4();
        // (0,0)=S e (0,1)=L ; impor '=' entre eles viola
        t.restricaoH[0][0] = Restricao.IGUAL;
        return t;
    }

    static Tabuleiro comOposicaoViolada() {
        Tabuleiro t = boardValido4();
        // (2,1)=L e (2,2)=L sao iguais; impor 'x' entre eles viola
        t.restricaoH[2][1] = Restricao.OPOSTO;
        return t;
    }

    static Tabuleiro board(String[] linhas) {
        int n = linhas.length;
        Tabuleiro t = new Tabuleiro(n);
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                t.grade[r][c] = Simbolo.deChar(linhas[r].charAt(c));
        return t;
    }
}
