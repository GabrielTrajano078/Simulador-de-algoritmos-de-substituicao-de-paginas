package br.unifor.sop.simulador;

/**
 * Execução via terminal (sem interface gráfica) — útil para testes rápidos.
 *
 * Uso:
 *   java -cp out br.unifor.sop.simulador.ExecucaoConsole "7 0 1 2 0 3" 3
 *
 * Sem argumentos usa a sequência de exemplo com 3 quadros.
 */
public final class ExecucaoConsole {

    public static void main(String[] args) {
        String linha = args.length > 0
                ? args[0]
                : "7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1";
        int k = args.length > 1 ? Integer.parseInt(args[1]) : 3;

        int[] refs = SimuladorApp.parsear(linha);
        AlgoritmoSubstituicao[] algs = {
                new Fifo(), new Lru(), new Relogio(), new Otimo()
        };

        System.out.printf("Molduras=%d | referências=%d%n%n", k, refs.length);
        int m = 1;
        for (AlgoritmoSubstituicao a : algs) {
            int f = a.contarFaltas(refs, k);
            System.out.printf("Método %d (%s) - %d faltas de página%n", m++, a.getNome(), f);
        }
    }
}
