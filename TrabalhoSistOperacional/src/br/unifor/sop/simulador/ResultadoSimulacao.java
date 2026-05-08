package br.unifor.sop.simulador;

import java.util.Collections;
import java.util.List;

/**
 * Resultado completo de uma simulação: lista de passos + total de faltas.
 */
public final class ResultadoSimulacao {

    public final String nomeAlgoritmo;
    public final List<Passo> passos;
    public final int totalFaltas;

    public ResultadoSimulacao(String nomeAlgoritmo, List<Passo> passos, int totalFaltas) {
        this.nomeAlgoritmo = nomeAlgoritmo;
        this.passos = Collections.unmodifiableList(passos);
        this.totalFaltas = totalFaltas;
    }
}
