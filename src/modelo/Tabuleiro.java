package modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Estado do tabuleiro durante a busca.
//   grade[r][c]      -> simbolo atual da celula (VAZIO enquanto nao foi preenchida).
//   fixa[r][c]       -> true se a celula veio como dica no arquivo; a busca nunca a altera.
//   restricaoH[r][c] -> ligacao entre (r,c) e (r,c+1)  (dimensao n x (n-1)).
//   restricaoV[r][c] -> ligacao entre (r,c) e (r+1,c)  (dimensao (n-1) x n).
public class Tabuleiro {

    public final int n;
    public final Simbolo[][] grade;
    public final boolean[][] fixa;
    public final Restricao[][] restricaoH;
    public final Restricao[][] restricaoV;

    public Tabuleiro(int n) {
        this.n = n;
        this.grade = new Simbolo[n][n];
        this.fixa = new boolean[n][n];
        this.restricaoH = new Restricao[n][n > 0 ? n - 1 : 0];
        this.restricaoV = new Restricao[n > 0 ? n - 1 : 0][n];

        for (int r = 0; r < n; r++) Arrays.fill(grade[r], Simbolo.VAZIO);
        for (Restricao[] linha : restricaoH) Arrays.fill(linha, Restricao.NENHUMA);
        for (Restricao[] linha : restricaoV) Arrays.fill(linha, Restricao.NENHUMA);
    }

    public boolean vazia(int r, int c) {
        return grade[r][c] == Simbolo.VAZIO;
    }

    // Coordenadas {linha, coluna} das celulas livres, em ordem de leitura. E por elas que
    // a busca anda; as dicas fixas ficam de fora. Os dois solvers usam este metodo.
    public int[][] celulasLivres() {
        List<int[]> livres = new ArrayList<>();
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (!fixa[r][c]) livres.add(new int[]{r, c});
        return livres.toArray(new int[0][]);
    }

    // Copia profunda: cada solver roda sobre o seu proprio tabuleiro.
    public Tabuleiro clonar() {
        Tabuleiro t = new Tabuleiro(n);
        for (int r = 0; r < n; r++) {
            System.arraycopy(grade[r], 0, t.grade[r], 0, n);
            System.arraycopy(fixa[r], 0, t.fixa[r], 0, n);
        }
        for (int r = 0; r < restricaoH.length; r++)
            System.arraycopy(restricaoH[r], 0, t.restricaoH[r], 0, restricaoH[r].length);
        for (int r = 0; r < restricaoV.length; r++)
            System.arraycopy(restricaoV[r], 0, t.restricaoV[r], 0, restricaoV[r].length);
        return t;
    }
}
