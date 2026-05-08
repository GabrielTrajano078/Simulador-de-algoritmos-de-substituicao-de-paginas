package br.unifor.sop.simulador;

/**
 * Contrato comum para todos os algoritmos de substituição de páginas.
 */
public interface AlgoritmoSubstituicao {

    String getNome();

    /**
     * Executa a simulação e retorna o resultado completo passo a passo.
     */
    ResultadoSimulacao simular(int[] referencias, int quadros);

    /**
     * Atalho: retorna apenas o total de faltas.
     */
    default int contarFaltas(int[] referencias, int quadros) {
        return simular(referencias, quadros).totalFaltas;
    }
}
