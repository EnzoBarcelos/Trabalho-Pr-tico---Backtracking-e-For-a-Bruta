package solver;

import modelo.Simbolo;
import modelo.Tabuleiro;

// Backtracking: a mesma busca da forca bruta, mas valida logo apos colocar cada simbolo.
// Se a celula recem-preenchida ja viola uma regra, o ramo inteiro e descartado (poda) e
// nem descemos a recursao. E essa poda que evita a explosao combinatoria.
public class Backtracking implements Solver {

    private long estados;
    private long validacoes;
    private long podas;
    private long[] estadosPorNivel;
    private final Validador validador = new Validador();
    private int[][] livres;

    @Override
    public boolean resolver(Tabuleiro t) {
        resetContadores();
        // A busca so mexe nas celulas livres, entao as dicas fixas nunca passam pela
        // validacao incremental. Se as proprias dicas ja se contradizem, nao ha solucao.
        if (!validador.dicasValidas(t)) return false;
        livres = t.celulasLivres();
        estadosPorNivel = new long[livres.length];
        return preencher(t, 0);
    }

    private boolean preencher(Tabuleiro t, int idx) {
        // Caso base: todas as livres preenchidas. Cada passo ja passou pela validacao e
        // nenhum simbolo passou de n/2 na linha/coluna; logo, ao completar a linha sobra
        // exatamente n/2 de cada um. Chegar aqui significa solucao valida.
        if (idx == livres.length) return true;

        int r = livres[idx][0];
        int c = livres[idx][1];
        for (Simbolo s : new Simbolo[]{Simbolo.SOL, Simbolo.LUA}) {
            t.grade[r][c] = s;
            estados++;
            estadosPorNivel[idx]++;
            validacoes++;
            if (validador.movimentoValido(t, r, c)) {
                if (preencher(t, idx + 1)) return true;
            } else {
                podas++; // regra violada: corta o ramo sem descer a recursao
            }
        }
        t.grade[r][c] = Simbolo.VAZIO; // backtrack: desfaz e volta
        return false;
    }

    @Override public long getEstadosExplorados() { return estados; }
    @Override public long getValidacoes()        { return validacoes; }
    @Override public long getPodas()             { return podas; }
    @Override public long[] getEstadosPorNivel() { return estadosPorNivel; }
    @Override public void resetContadores()      { estados = 0; validacoes = 0; podas = 0; }
    @Override public String getNome()            { return "Backtracking"; }
}
