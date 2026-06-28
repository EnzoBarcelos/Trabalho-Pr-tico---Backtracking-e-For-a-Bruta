package io;

import modelo.Restricao;
import modelo.Tabuleiro;

// Desenha o tabuleiro no terminal em ASCII. As restricoes aparecem entre as celulas:
// '=' (igualdade) e 'x' (oposicao). Celula vazia = '.'. Exemplo:
//
//    S = S   L   L
//            x
//    L   S   S = L
public class ImpressoraTabuleiro {

    public static void imprimir(Tabuleiro t) {
        System.out.print(formatar(t));
    }

    public static String formatar(Tabuleiro t) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < t.n; r++) {
            // linha das celulas + restricoes horizontais
            for (int c = 0; c < t.n; c++) {
                sb.append(' ').append(t.grade[r][c].c).append(' ');
                if (c < t.n - 1) sb.append(simbolo(t.restricaoH[r][c]));
            }
            sb.append('\n');

            // linha das restricoes verticais (entre a linha r e r+1)
            if (r < t.n - 1) {
                for (int c = 0; c < t.n; c++) {
                    sb.append(' ').append(simbolo(t.restricaoV[r][c])).append(' ');
                    if (c < t.n - 1) sb.append(' ');
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static char simbolo(Restricao rest) {
        if (rest == Restricao.IGUAL) return '=';
        if (rest == Restricao.OPOSTO) return 'x';
        return ' ';
    }
}
