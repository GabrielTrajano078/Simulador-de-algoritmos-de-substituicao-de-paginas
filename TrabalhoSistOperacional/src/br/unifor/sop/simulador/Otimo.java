package br.unifor.sop.simulador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ótimo (MIN): substitui a página cujo próximo uso é o mais distante no futuro.
 * Algoritmo teórico — requer conhecimento da sequência futura completa.
 */
public class Otimo implements AlgoritmoSubstituicao {

    @Override
    public String getNome() {
        return "Ótimo";
    }

    @Override
    public ResultadoSimulacao simular(int[] referencias, int quadros) {
        if (quadros <= 0 || referencias == null || referencias.length == 0) {
            return new ResultadoSimulacao(getNome(), new ArrayList<>(), 0);
        }

        int[] frames = new int[quadros];
        Arrays.fill(frames, -1);
        int faltas = 0;
        List<Passo> passos = new ArrayList<>();

        for (int i = 0; i < referencias.length; i++) {
            int pagina = referencias[i];
            int slot = indexOf(frames, pagina);
            boolean falta = slot < 0;

            if (falta) {
                faltas++;
                int livre = indexOf(frames, -1);
                if (livre >= 0) {
                    frames[livre] = pagina;
                } else {
                    int vitima = escolherVitima(frames, referencias, i);
                    frames[vitima] = pagina;
                }
            }
            passos.add(new Passo(pagina, frames, falta));
        }
        return new ResultadoSimulacao(getNome(), passos, faltas);
    }

    /** Retorna o índice do quadro cuja página tem o próximo uso mais distante (ou nunca). */
    private static int escolherVitima(int[] frames, int[] ref, int atual) {
        int melhorSlot = 0;
        int melhorProximo = -1;
        for (int i = 0; i < frames.length; i++) {
            int prox = proximaOcorrencia(ref, frames[i], atual + 1);
            if (prox == Integer.MAX_VALUE) return i; // nunca mais usada → vítima imediata
            if (prox > melhorProximo) {
                melhorProximo = prox;
                melhorSlot = i;
            }
        }
        return melhorSlot;
    }

    private static int proximaOcorrencia(int[] ref, int pagina, int from) {
        for (int j = from; j < ref.length; j++) {
            if (ref[j] == pagina) return j;
        }
        return Integer.MAX_VALUE;
    }

    private static int indexOf(int[] frames, int pagina) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == pagina) return i;
        }
        return -1;
    }
}
