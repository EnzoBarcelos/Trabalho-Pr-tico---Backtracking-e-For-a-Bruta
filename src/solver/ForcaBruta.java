package solver;

import modelo.Simbolo;
import modelo.Tabuleiro;

// Forca bruta: preenche as celulas livres testando Sol e depois Lua, SEM checar regra
// nenhuma durante a construcao. So valida quando o tabuleiro esta completo (na folha da
// recursao). No pior caso gera as 2^K combinacoes (K = celulas vazias). Nunca poda.
public class ForcaBruta implements Solver {

    private long estados;
    private long validacoes;
    private long[] estadosPorNivel;
    private final Validador validador = new Validador();
    private int[][] livres;

    @Override
    public boolean resolver(Tabuleiro t) {
        resetContadores();
        livres = t.celulasLivres();
        estadosPorNivel = new long[livres.length];
        return preencher(t, 0);
    }

    private boolean preencher(Tabuleiro t, int idx) {
        // Tabuleiro completo: so agora a forca bruta verifica as regras.
        if (idx == livres.length) {
            validacoes++;
            return validador.tabuleiroCompletoValido(t);
        }

        int r = livres[idx][0];
        int c = livres[idx][1];
        for (Simbolo s : new Simbolo[]{Simbolo.SOL, Simbolo.LUA}) {
            t.grade[r][c] = s;
            estados++;
            estadosPorNivel[idx]++;
            if (preencher(t, idx + 1)) return true;
        }
        t.grade[r][c] = Simbolo.VAZIO; // desfaz antes de voltar
        return false;
    }

    @Override public long getEstadosExplorados() { return estados; }
    @Override public long getValidacoes()        { return validacoes; }
    @Override public long getPodas()             { return 0; }
    @Override public long[] getEstadosPorNivel() { return estadosPorNivel; }
    @Override public void resetContadores()      { estados = 0; validacoes = 0; }
    @Override public String getNome()            { return "Forca Bruta"; }
}
