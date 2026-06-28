import benchmark.Benchmark;
import io.ImpressoraTabuleiro;
import io.LeitorTabuleiro;
import modelo.Tabuleiro;
import solver.Backtracking;
import solver.ForcaBruta;
import solver.Solver;
import solver.Validador;

import java.io.IOException;
import java.util.Scanner;

// Menu de console: (1) resolver uma instancia mostrando inicial + final,
// (2) rodar o benchmark e gerar o CSV, (3) rodar os auto-testes, (0) sair.
public class Main {

    private static final String[] INSTANCIAS = {
            "entradas/teste_4x4.txt",
            "entradas/facil_6x6.txt",
            "entradas/medio_6x6.txt",
            "entradas/vazio_6x6.txt",
            "entradas/dificil_8x8.txt",
            "entradas/esparso_8x8.txt",
            "entradas/enorme_10x10.txt"
    };

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("==============================================");
            System.out.println("  Tango Solver - Forca Bruta e Backtracking");
            System.out.println("  FPAA - PUC Minas");
            System.out.println("==============================================");
            System.out.println("1 - Resolver uma instancia (inicial + final)");
            System.out.println("2 - Benchmark (gera CSV)");
            System.out.println("3 - Auto-testes");
            System.out.println("0 - Sair");
            System.out.print("Opcao: ");

            if (!sc.hasNextLine()) {       // fim da entrada (ex.: stdin canalizado)
                System.out.println("\nEntrada encerrada.");
                sc.close();
                return;
            }
            String entrada = sc.nextLine().trim();
            switch (entrada) {
                case "1":
                    menuResolver(sc);
                    break;
                case "2":
                    Benchmark.executar();
                    break;
                case "3":
                    rodarTestes();
                    break;
                case "0":
                    System.out.println("Encerrando...");
                    sc.close();
                    return;
                default:
                    System.out.println("Opcao invalida.");
            }
        }
    }

    private static void menuResolver(Scanner sc) {
        System.out.println();
        System.out.println("Instancias disponiveis:");
        for (int i = 0; i < INSTANCIAS.length; i++) {
            System.out.println("  " + (i + 1) + " - " + INSTANCIAS[i]);
        }
        System.out.print("Escolha (ou digite um caminho .txt): ");
        String esc = sc.nextLine().trim();

        String caminho;
        try {
            int idx = Integer.parseInt(esc);
            caminho = INSTANCIAS[idx - 1];
        } catch (Exception e) {
            caminho = esc; // tratou como caminho livre
        }

        try {
            Tabuleiro base = LeitorTabuleiro.ler(caminho);
            System.out.println();
            System.out.println(">>> Tabuleiro INICIAL (" + base.n + "x" + base.n + "):");
            ImpressoraTabuleiro.imprimir(base);

            resolverComStat(base, new ForcaBruta());
            resolverComStat(base, new Backtracking());
        } catch (IOException e) {
            System.out.println("Nao foi possivel ler '" + caminho + "': " + e.getMessage());
        }
    }

    private static void resolverComStat(Tabuleiro base, Solver solver) {
        int vazias = 0;
        for (int r = 0; r < base.n; r++)
            for (int c = 0; c < base.n; c++)
                if (!base.fixa[r][c]) vazias++;

        System.out.println();
        System.out.println("----- " + solver.getNome() + " -----");

        // Forca bruta em instancia grande pode nao terminar: avisa e pula.
        if (solver instanceof ForcaBruta && vazias > 25) {
            System.out.printf("(%d celulas vazias -> espaco de ~2^%d estados; Forca Bruta inviavel, pulando)%n",
                    vazias, vazias);
            return;
        }

        Tabuleiro t = base.clonar();
        long ini = System.nanoTime();
        boolean ok = solver.resolver(t);
        long fim = System.nanoTime();

        if (!ok) {
            System.out.println("Sem solucao encontrada.");
            return;
        }

        ImpressoraTabuleiro.imprimir(t);
        boolean valido = new Validador().tabuleiroCompletoValido(t);
        System.out.printf("Estados=%d  Validacoes=%d  Podas=%d  Tempo=%.3f ms  Valido=%s%n",
                solver.getEstadosExplorados(), solver.getValidacoes(), solver.getPodas(),
                (fim - ini) / 1_000_000.0, valido ? "sim" : "NAO");
    }

    private static void rodarTestes() {
        try {
            teste.Testes.main(new String[0]);
        } catch (AssertionError e) {
            System.out.println("FALHA nos testes: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro ao rodar testes: " + e.getMessage());
        }
    }
}
