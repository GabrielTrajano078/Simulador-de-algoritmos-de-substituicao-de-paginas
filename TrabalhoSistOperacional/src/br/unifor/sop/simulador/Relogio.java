package br.unifor.sop.simulador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Relógio / Segunda Chance (Clock): lista circular com bit de referência.
 * Slots vazios (-1) possuem bit=false e são substituídos imediatamente.
 */
public class Relogio implements AlgoritmoSubstituicao {

    @Override
    public String getNome() {
        return "Relógio";
    }

    @Override
    public ResultadoSimulacao simular(int[] referencias, int quadros) {
        if (quadros <= 0 || referencias == null || referencias.length == 0) {
            return new ResultadoSimulacao(getNome(), new ArrayList<>(), 0);
        }

        int[] frames = new int[quadros];
        Arrays.fill(frames, -1);
        boolean[] refBit = new boolean[quadros]; // false por padrão

        int ponteiro = 0;
        int faltas = 0;
        List<Passo> passos = new ArrayList<>();

        for (int pagina : referencias) {
            int slot = indexOf(frames, pagina);
            boolean falta = slot < 0;

            if (falta) {
                faltas++;
                // Avança o relógio até encontrar slot com refBit=false ou vazio (-1)
                while (frames[ponteiro] != -1 && refBit[ponteiro]) {
                    refBit[ponteiro] = false;
                    ponteiro = (ponteiro + 1) % quadros;
                }
                frames[ponteiro] = pagina;
                refBit[ponteiro] = true;
                ponteiro = (ponteiro + 1) % quadros;
            } else {
                refBit[slot] = true;
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
