package br.unifor.sop.simulador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LRU (Least Recently Used): substitui a página usada há mais tempo.
 */
public class Lru implements AlgoritmoSubstituicao {

    @Override
    public String getNome() {
        return "LRU";
    }

    @Override
    public ResultadoSimulacao simular(int[] referencias, int quadros) {
        if (quadros <= 0 || referencias == null || referencias.length == 0) {
            return new ResultadoSimulacao(getNome(), new ArrayList<>(), 0);
        }

        int[] frames = new int[quadros];
        Arrays.fill(frames, -1);

        int[] lastUse = new int[quadros];
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
                    lastUse[livre] = tempo++;
                } else {
                    int minSlot = 0;
                    for (int i = 1; i < quadros; i++) {
                        if (lastUse[i] < lastUse[minSlot]) minSlot = i;
                    }
                    frames[minSlot] = pagina;
                    lastUse[minSlot] = tempo++;
                }
            } else {
                lastUse[slot] = tempo++;
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
