package benchmark;

import io.LeitorTabuleiro;
import modelo.Tabuleiro;
import solver.Backtracking;
import solver.ForcaBruta;
import solver.Solver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.function.Supplier;

// Roda as duas estrategias sobre as instancias e mede estados, validacoes, podas e tempo
// (media +/- desvio sobre algumas repeticoes, com um warm-up descartado para o JIT).
// Grava benchmark_tango.csv. Acima de LIMITE_FORCA_BRUTA celulas vazias a forca bruta nao
// roda (2^vazias e grande demais): registra so o limite teorico.
public class Benchmark {

    private static final int LIMITE_FORCA_BRUTA = 25; // acima disso 2^vazias e grande demais
    private static final int REPETICOES = 5;

    private static final String[] INSTANCIAS = {
            "entradas/teste_4x4.txt",
            "entradas/facil_6x6.txt",
            "entradas/medio_6x6.txt",
            "entradas/vazio_6x6.txt",
            "entradas/dificil_8x8.txt",
            "entradas/esparso_8x8.txt",
            "entradas/enorme_10x10.txt"
    };

    // instancia onde as duas estrategias rodam; usada para o perfil por nivel da recursao
    private static final String INSTANCIA_PERFIL = "entradas/medio_6x6.txt";

    public static void executar() {
        StringBuilder csv = new StringBuilder();
        csv.append("instancia,n,vazias,solver,estados,validacoes,podas,tempo_ms_media,tempo_ms_std,resolveu\n");

        System.out.println();
        System.out.printf("%-20s %3s %6s %-13s %15s %12s %12s %16s%n",
                "instancia", "n", "vazias", "solver", "estados", "validacoes", "podas", "tempo_ms (m+-s)");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (String caminho : INSTANCIAS) {
            try {
                Tabuleiro base = LeitorTabuleiro.ler(caminho);
                int vazias = contaVazias(base);
                String nome = nomeArquivo(caminho);

                medirEReportar(csv, nome, base, vazias, Backtracking::new);

                if (vazias <= LIMITE_FORCA_BRUTA) {
                    medirEReportar(csv, nome, base, vazias, ForcaBruta::new);
                } else {
                    double limite = Math.pow(2, vazias);
                    System.out.printf("%-20s %3d %6d %-13s %15s %12s %12s %16s%n",
                            nome, base.n, vazias, "Forca Bruta",
                            String.format(Locale.US, "~2^%d", vazias), "-", "-", "(pulado)");
                    csv.append(String.format(Locale.US, "%s,%d,%d,%s,%.0f,%d,%d,%s,%s,%s%n",
                            nome, base.n, vazias, "Forca Bruta", limite, 0L, 0L, "NA", "NA", "pulado"));
                }
            } catch (IOException e) {
                System.out.println("Erro lendo " + caminho + ": " + e.getMessage());
            }
        }

        gravar("benchmark_tango.csv", csv.toString());
        System.out.println();
        System.out.println("CSV gravado em benchmark_tango.csv  (media +/- desvio sobre " + REPETICOES + " repeticoes)");

        perfilarNivel(INSTANCIA_PERFIL);
    }

    // Grava, por nivel da recursao, quantos estados cada estrategia explorou. Mostra o
    // formato da arvore: a forca bruta cresce por nivel; o backtracking fica achatado.
    private static void perfilarNivel(String caminho) {
        try {
            Tabuleiro base = LeitorTabuleiro.ler(caminho);

            Solver fb = new ForcaBruta();
            fb.resolver(base.clonar());
            Solver bt = new Backtracking();
            bt.resolver(base.clonar());

            long[] nfb = fb.getEstadosPorNivel();
            long[] nbt = bt.getEstadosPorNivel();
            int len = Math.max(nfb.length, nbt.length);

            StringBuilder sb = new StringBuilder("nivel,forca_bruta,backtracking\n");
            for (int i = 0; i < len; i++) {
                long vf = i < nfb.length ? nfb[i] : 0;
                long vb = i < nbt.length ? nbt[i] : 0;
                sb.append(i).append(",").append(vf).append(",").append(vb).append("\n");
            }
            gravar("perfil_nivel.csv", sb.toString());
            System.out.println("Perfil por nivel gravado em perfil_nivel.csv (instancia "
                    + nomeArquivo(caminho) + ")");
        } catch (IOException e) {
            System.out.println("Erro no perfil por nivel: " + e.getMessage());
        }
    }

    private static void medirEReportar(StringBuilder csv, String nome, Tabuleiro base,
                                       int vazias, Supplier<Solver> fabrica) {
        // warm-up (descartado)
        Solver aquecer = fabrica.get();
        aquecer.resolver(base.clonar());

        double[] tempos = new double[REPETICOES];
        long estados = 0, validacoes = 0, podas = 0;
        boolean ok = false;

        for (int i = 0; i < REPETICOES; i++) {
            Solver solver = fabrica.get();
            Tabuleiro t = base.clonar();
            long ini = System.nanoTime();
            ok = solver.resolver(t);
            long fim = System.nanoTime();
            tempos[i] = (fim - ini) / 1_000_000.0;
            estados = solver.getEstadosExplorados();
            validacoes = solver.getValidacoes();
            podas = solver.getPodas();
        }

        double media = media(tempos);
        double std = desvioPadrao(tempos, media);
        String nomeSolver = aquecer.getNome();

        System.out.printf("%-20s %3d %6d %-13s %15d %12d %12d %8.3f +- %5.3f%n",
                nome, base.n, vazias, nomeSolver, estados, validacoes, podas, media, std);

        csv.append(String.format(Locale.US, "%s,%d,%d,%s,%d,%d,%d,%.4f,%.4f,%s%n",
                nome, base.n, vazias, nomeSolver, estados, validacoes, podas, media, std, ok ? "sim" : "nao"));
    }

    private static double media(double[] v) {
        double s = 0;
        for (double x : v) s += x;
        return s / v.length;
    }

    private static double desvioPadrao(double[] v, double media) {
        if (v.length < 2) return 0;
        double s = 0;
        for (double x : v) s += (x - media) * (x - media);
        return Math.sqrt(s / (v.length - 1)); // amostral (n-1)
    }

    private static int contaVazias(Tabuleiro t) {
        int v = 0;
        for (int r = 0; r < t.n; r++)
            for (int c = 0; c < t.n; c++)
                if (!t.fixa[r][c]) v++;
        return v;
    }

    private static String nomeArquivo(String caminho) {
        int barra = Math.max(caminho.lastIndexOf('/'), caminho.lastIndexOf('\\'));
        return caminho.substring(barra + 1);
    }

    private static void gravar(String caminho, String conteudo) {
        try (FileWriter w = new FileWriter(caminho)) {
            w.write(conteudo);
        } catch (IOException e) {
            System.out.println("Erro ao gravar CSV: " + e.getMessage());
        }
    }
}
