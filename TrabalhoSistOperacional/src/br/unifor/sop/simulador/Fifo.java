package br.unifor.sop.simulador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FIFO (First In, First Out): substitui a página mais antiga na memória.
 */
public class Fifo implements AlgoritmoSubstituicao {

    @Override
    public String getNome() {
        return "FIFO";
    }

    @Override
    public ResultadoSimulacao simular(int[] referencias, int quadros) {
        if (quadros <= 0 || referencias == null || referencias.length == 0) {
            return new ResultadoSimulacao(getNome(), new ArrayList<>(), 0);
        }

        int[] frames = new int[quadros];
        Arrays.fill(frames, -1);

        int[] insertTime = new int[quadros];
        int tempo = 0;
        int faltas = 0;
        List<Passo> passos = new ArrayList<>();

        for (int pagina : referencias) {
            int slot = indexOf(frames, pagina);
            boolean falta = slot < 0;

            if (falta) {
                faltas++;
                int livre = indexOf(frames, -1);
                if (livre >= 0) {
                    frames[livre] = pagina;
                    insertTime[livre] = tempo++;
                } else {
                    int minSlot = 0;
                    for (int i = 1; i < quadros; i++) {
                        if (insertTime[i] < insertTime[minSlot]) minSlot = i;
                    }
                    frames[minSlot] = pagina;
                    insertTime[minSlot] = tempo++;
                }
            }
            passos.add(new Passo(pagina, frames, falta));
        }
        return new ResultadoSimulacao(getNome(), passos, faltas);
    }

    private static int indexOf(int[] frames, int pagina) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == pagina) return i;
        }
        return -1;
    }
}
