package io;

import modelo.Restricao;
import modelo.Simbolo;
import modelo.Tabuleiro;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Le um tabuleiro de um arquivo texto. Formato:
//   linha 1 ........ N (tamanho da grade, par e positivo).
//   N linhas ........ a grade: cada celula 'S' (Sol), 'L' (Lua) ou '.' (vazia); espacos ignorados.
//   demais linhas ... restricoes (opcionais):
//                       H r c <sym>  -> entre (r,c) e (r,c+1)
//                       V r c <sym>  -> entre (r,c) e (r+1,c)   com <sym> = '=' ou 'x'
//   linhas em branco e linhas com '#' sao comentarios e sao ignoradas.
public class LeitorTabuleiro {

    public static Tabuleiro ler(String caminho) throws IOException {
        List<String> linhas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String l;
            while ((l = br.readLine()) != null) {
                String t = l.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                linhas.add(t);
            }
        }

        if (linhas.isEmpty()) throw new IOException("Arquivo de tabuleiro vazio: " + caminho);

        int n;
        try {
            n = Integer.parseInt(linhas.get(0).trim());
        } catch (NumberFormatException e) {
            throw new IOException("A primeira linha deveria ser o tamanho N: " + linhas.get(0));
        }
        // A regra do equilibrio (n/2 de cada simbolo) so faz sentido com N par e positivo.
        if (n <= 0 || n % 2 != 0)
            throw new IOException("N deve ser par e positivo, mas veio: " + n);
        if (linhas.size() < 1 + n)
            throw new IOException("Esperava " + n + " linhas de grade, encontrei " + (linhas.size() - 1));

        Tabuleiro tab = new Tabuleiro(n);

        // grade inicial
        for (int r = 0; r < n; r++) {
            String linha = linhas.get(1 + r).replace(" ", "");
            if (linha.length() < n)
                throw new IOException("Linha " + r + " da grade tem menos de " + n + " celulas: " + linha);
            for (int c = 0; c < n; c++) {
                Simbolo s = Simbolo.deChar(linha.charAt(c));
                tab.grade[r][c] = s;
                tab.fixa[r][c] = (s != Simbolo.VAZIO); // celula preenchida no arquivo = dica
            }
        }

        // restricoes (o que sobrar depois da grade)
        for (int i = 1 + n; i < linhas.size(); i++) {
            String[] p = linhas.get(i).split("\\s+");
            if (p.length < 4) continue;
            try {
                char tipo = Character.toUpperCase(p[0].charAt(0));
                int r = Integer.parseInt(p[1]);
                int c = Integer.parseInt(p[2]);
                Restricao rest = Restricao.deChar(p[3].charAt(0));
                if (tipo == 'H' && r >= 0 && r < n && c >= 0 && c < n - 1) {
                    tab.restricaoH[r][c] = rest;
                } else if (tipo == 'V' && r >= 0 && r < n - 1 && c >= 0 && c < n) {
                    tab.restricaoV[r][c] = rest;
                } else {
                    throw new IOException("Restricao invalida ou fora dos limites: " + linhas.get(i));
                }
            } catch (NumberFormatException e) {
                throw new IOException("Restricao mal formada (esperava 'H/V linha coluna sinal'): " + linhas.get(i));
            }
        }

        return tab;
    }
}
