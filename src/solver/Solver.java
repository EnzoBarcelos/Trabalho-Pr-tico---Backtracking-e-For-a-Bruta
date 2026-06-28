package solver;

import modelo.Tabuleiro;

// Contrato comum das duas estrategias, para o benchmark trata-las do mesmo jeito.
// Contadores (para comparar de forma justa):
//   estados    -> cada simbolo colocado numa celula (cada no da arvore de busca).
//   validacoes -> cada chamada ao Validador.
//   podas      -> ramos cortados pela poda (so o Backtracking gera; Forca Bruta = 0).
public interface Solver {

    // Tenta resolver. Em caso de sucesso, 't' fica preenchido com a solucao.
    boolean resolver(Tabuleiro t);

    long getEstadosExplorados();
    long getValidacoes();
    long getPodas();

    // Estados explorados por nivel da recursao (a soma do vetor = getEstadosExplorados).
    long[] getEstadosPorNivel();

    void resetContadores();
    String getNome();
}
