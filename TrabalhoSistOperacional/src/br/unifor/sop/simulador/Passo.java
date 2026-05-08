package br.unifor.sop.simulador;

/**
 * Representa o estado da memória após processar uma referência.
 */
public final class Passo {

    /** Número da página referenciada neste passo. */
    public final int referencia;

    /** Estado dos quadros após este passo; -1 = quadro vazio. */
    public final int[] molduras;

    /** true se ocorreu falta de página (page fault). */
    public final boolean falta;

    public Passo(int referencia, int[] molduras, boolean falta) {
        this.referencia = referencia;
        this.molduras = molduras.clone();
        this.falta = falta;
    }
}
