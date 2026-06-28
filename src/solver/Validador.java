package solver;

import modelo.Restricao;
import modelo.Simbolo;
import modelo.Tabuleiro;

// Concentra as 5 regras do Tango. Nao sabe nada sobre recursao: so responde "isto e valido?".
// Tres usos:
//   tabuleiroCompletoValido(t) -> forca bruta, no tabuleiro inteiramente preenchido.
//   movimentoValido(t,r,c)     -> backtracking, checa so o que a celula (r,c) pode ter violado.
//   dicasValidas(t)            -> confere se as dicas iniciais ja respeitam as regras.
//
// Regras: (1) toda celula preenchida; (2) nunca 3 iguais seguidos; (3) cada linha/coluna
// com n/2 de cada simbolo; (4) '=' liga iguais; (5) 'x' liga opostos.
public class Validador {

    // Valida um tabuleiro TOTALMENTE preenchido contra as 5 regras.
    public boolean tabuleiroCompletoValido(Tabuleiro t) {
        int n = t.n;
        int meta = n / 2;

        // regra 1 (completo) + regra 3 (equilibrio exato por linha e por coluna)
        for (int r = 0; r < n; r++) {
            int solLinha = 0, luaLinha = 0, solCol = 0, luaCol = 0;
            for (int c = 0; c < n; c++) {
                if (t.grade[r][c] == Simbolo.VAZIO) return false;
                if (t.grade[r][c] == Simbolo.SOL) solLinha++; else luaLinha++;
                if (t.grade[c][r] == Simbolo.SOL) solCol++;  else luaCol++;
            }
            if (solLinha != meta || luaLinha != meta) return false;
            if (solCol  != meta || luaCol  != meta) return false;
        }

        // regras 2, 4 e 5: a checagem local de cada celula ja cobre
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (!movimentoValido(t, r, c)) return false;
        return true;
    }

    // Confere todas as celulas ja preenchidas. Usado antes da busca para validar as dicas:
    // o backtracking so olha as celulas livres, entao uma violacao so entre dicas escaparia.
    public boolean dicasValidas(Tabuleiro t) {
        for (int r = 0; r < t.n; r++)
            for (int c = 0; c < t.n; c++)
                if (!movimentoValido(t, r, c)) return false;
        return true;
    }

    // Verifica so as regras que a celula (r,c) pode ter violado. Celulas VAZIO contam como
    // indefinidas e nao violam nada. E a poda do backtracking.
    public boolean movimentoValido(Tabuleiro t, int r, int c) {
        Simbolo s = t.grade[r][c];
        if (s == Simbolo.VAZIO) return true;

        int meta = t.n / 2;

        // regra 2: tres iguais seguidos, olhando as duas direcoes
        if (tresIguais(t, r, c, 0, 1)) return false; // horizontal
        if (tresIguais(t, r, c, 1, 0)) return false; // vertical

        // regra 3 parcial: nunca passar de n/2 de um simbolo. Como so adicionamos simbolos,
        // se nunca passamos de n/2 a linha/coluna completa fica com n/2 de cada (invariante).
        if (contaNaLinha(t, r, s) > meta) return false;
        if (contaNaColuna(t, c, s) > meta) return false;

        // regras 4 e 5: ligacoes = / x com vizinhos ja definidos
        if (!restricoesOk(t, r, c)) return false;

        return true;
    }

    // (r,c) participa de alguma trinca de iguais na direcao (dr,dc)? Olha as 3 janelas de
    // tamanho 3 que contem (r,c).
    private boolean tresIguais(Tabuleiro t, int r, int c, int dr, int dc) {
        Simbolo s = t.grade[r][c];
        for (int k = -2; k <= 0; k++) {
            int r0 = r + k * dr, c0 = c + k * dc;
            int r1 = r0 + dr,    c1 = c0 + dc;
            int r2 = r1 + dr,    c2 = c1 + dc;
            if (dentro(t, r0, c0) && dentro(t, r1, c1) && dentro(t, r2, c2)
                    && t.grade[r0][c0] == s && t.grade[r1][c1] == s && t.grade[r2][c2] == s) {
                return true;
            }
        }
        return false;
    }

    private int contaNaLinha(Tabuleiro t, int r, Simbolo s) {
        int cont = 0;
        for (int c = 0; c < t.n; c++) if (t.grade[r][c] == s) cont++;
        return cont;
    }

    private int contaNaColuna(Tabuleiro t, int c, Simbolo s) {
        int cont = 0;
        for (int r = 0; r < t.n; r++) if (t.grade[r][c] == s) cont++;
        return cont;
    }

    private boolean restricoesOk(Tabuleiro t, int r, int c) {
        Simbolo s = t.grade[r][c];
        if (c > 0       && !parOk(s, t.grade[r][c - 1], t.restricaoH[r][c - 1])) return false; // esquerda
        if (c < t.n - 1 && !parOk(s, t.grade[r][c + 1], t.restricaoH[r][c]))     return false; // direita
        if (r > 0       && !parOk(s, t.grade[r - 1][c], t.restricaoV[r - 1][c])) return false; // acima
        if (r < t.n - 1 && !parOk(s, t.grade[r + 1][c], t.restricaoV[r][c]))     return false; // abaixo
        return true;
    }

    // Vizinho vazio nunca viola: a checagem volta quando ele for preenchido.
    private boolean parOk(Simbolo s, Simbolo viz, Restricao rest) {
        if (rest == Restricao.NENHUMA || viz == Simbolo.VAZIO) return true;
        if (rest == Restricao.IGUAL) return s == viz;
        return s != viz; // OPOSTO
    }

    private boolean dentro(Tabuleiro t, int r, int c) {
        return r >= 0 && r < t.n && c >= 0 && c < t.n;
    }
}
