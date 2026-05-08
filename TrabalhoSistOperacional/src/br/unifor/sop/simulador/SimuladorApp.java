package br.unifor.sop.simulador;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface gráfica Swing com:
 *  - Entrada da sequência e número de quadros
 *  - Abas por algoritmo com tabela passo-a-passo colorida
 *  - Gráfico comparativo de barras
 */
public final class SimuladorApp extends JFrame {

    // ── Paleta de cores ────────────────────────────────────────────────────────
    private static final Color AZUL_ESCURO   = new Color(21,  101, 192);
    private static final Color AZUL_TEXTO    = new Color(187, 222, 251);
    private static final Color FUNDO_INPUT   = new Color(245, 245, 245);
    private static final Color CINZA_LINHA   = new Color(200, 200, 200);
    private static final Color CELL_HEADER   = new Color(236, 239, 241);

    // cores por algoritmo (aba + barra)
    private static final Color[] CORES_ALG = {
            new Color(21,  101, 192),   // FIFO  – azul
            new Color(46,  125,  50),   // LRU   – verde escuro
            new Color(230,  81,   0),   // Relógio – laranja
            new Color(106,  27, 154),   // Ótimo – roxo
    };

    // ── Componentes de entrada ─────────────────────────────────────────────────
    private final JTextField campoRefs = new JTextField(
            "7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1", 44);
    private final JSpinner spinnerK =
            new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));

    // ── Área principal ─────────────────────────────────────────────────────────
    private final JTabbedPane abas   = new JTabbedPane(JTabbedPane.TOP);
    private final GraficoBarra grafico = new GraficoBarra();

    private final AlgoritmoSubstituicao[] algoritmos = {
            new Fifo(), new Lru(), new Relogio(), new Otimo()
    };

    // ══════════════════════════════════════════════════════════════════════════
    private SimuladorApp() {
        super("Simulador de Substituição de Páginas — Unifor");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(montarTopo(),   BorderLayout.NORTH);
        add(montarCentro(), BorderLayout.CENTER);

        pack();
        setMinimumSize(new Dimension(860, 640));
        setLocationRelativeTo(null);
    }

    // ── Painel superior (título + campos de entrada) ───────────────────────────
    private JPanel montarTopo() {
        JPanel wrap = new JPanel(new BorderLayout());

        // faixa azul com título
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AZUL_ESCURO);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel titulo = new JLabel("Simulador de Substituição de Páginas");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);

        JLabel sub = new JLabel(
                "UNIFOR · Ciência da Computação · Sistemas Operacionais");
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        sub.setForeground(AZUL_TEXTO);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(sub);
        header.add(textos, BorderLayout.CENTER);
        wrap.add(header, BorderLayout.NORTH);

        // linha de inputs
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        inputs.setBackground(FUNDO_INPUT);
        inputs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CINZA_LINHA));

        inputs.add(rotulo("Sequência de páginas:"));
        inputs.add(campoRefs);

        inputs.add(rotulo("  Quadros:"));
        spinnerK.setPreferredSize(new Dimension(64, 28));
        inputs.add(spinnerK);

        JButton btnSimular = botao("▶  Simular", AZUL_ESCURO, Color.WHITE, true);
        btnSimular.addActionListener(e -> executar());
        inputs.add(btnSimular);

        JButton btnLimpar = botao("Limpar", new Color(117, 117, 117), Color.WHITE, false);
        btnLimpar.addActionListener(e -> {
            campoRefs.setText("");
            abas.removeAll();
            grafico.setDados(new LinkedHashMap<>());
        });
        inputs.add(btnLimpar);

        wrap.add(inputs, BorderLayout.CENTER);
        return wrap;
    }

    // ── Área central (abas + gráfico) ──────────────────────────────────────────
    private JPanel montarCentro() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

        abas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        p.add(abas, BorderLayout.CENTER);

        grafico.setPreferredSize(new Dimension(0, 210));
        grafico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 189, 189)),
                "Comparativo — faltas de página por algoritmo"));
        p.add(grafico, BorderLayout.SOUTH);

        return p;
    }

    // ── Execução da simulação ──────────────────────────────────────────────────
    private void executar() {
        int[] refs;
        try {
            refs = parsear(campoRefs.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Sequência inválida.\nUse apenas inteiros separados por espaço.",
                    "Erro de entrada", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (refs.length == 0) {
            JOptionPane.showMessageDialog(this, "Informe ao menos uma referência.",
                    "Entrada vazia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int k = (int) spinnerK.getValue();

        abas.removeAll();
        LinkedHashMap<String, Integer> dadosGrafico = new LinkedHashMap<>();

        for (int i = 0; i < algoritmos.length; i++) {
            ResultadoSimulacao res = algoritmos[i].simular(refs, k);
            dadosGrafico.put(res.nomeAlgoritmo, res.totalFaltas);
            abas.addTab(" " + res.nomeAlgoritmo + " ", montarAba(res, CORES_ALG[i]));
            abas.setBackgroundAt(i, CORES_ALG[i]);
            abas.setForegroundAt(i, Color.WHITE);
        }
        grafico.setDados(dadosGrafico);
    }

    // ── Construção de cada aba ─────────────────────────────────────────────────
    private JPanel montarAba(ResultadoSimulacao res, Color cor) {
        List<Passo> passos = res.passos;
        int n = passos.size();
        int k = passos.isEmpty() ? 0 : passos.get(0).molduras.length;
        int numLinhas = k + 2; // referência + k quadros + falta?

        // cabeçalhos de coluna: "", "1", "2", …, "n"
        String[] colunas = new String[n + 1];
        colunas[0] = "";
        for (int i = 0; i < n; i++) colunas[i + 1] = String.valueOf(i + 1);

        // dados
        Object[][] dados = new Object[numLinhas][n + 1];
        dados[0][0] = "Referência";
        for (int f = 0; f < k; f++) dados[f + 1][0] = "Quadro " + (f + 1);
        dados[numLinhas - 1][0] = "Falta?";

        for (int c = 0; c < n; c++) {
            Passo p = passos.get(c);
            dados[0][c + 1] = String.valueOf(p.referencia);
            for (int f = 0; f < k; f++) {
                dados[f + 1][c + 1] = p.molduras[f] < 0 ? "" : String.valueOf(p.molduras[f]);
            }
            dados[numLinhas - 1][c + 1] = p.falta ? "✗" : "✓";
        }

        DefaultTableModel modelo = new DefaultTableModel(dados, colunas) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(30);
        tabela.setShowGrid(true);
        tabela.setGridColor(new Color(220, 220, 220));
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        tabela.getTableHeader().setBackground(new Color(224, 224, 224));
        tabela.getTableHeader().setReorderingAllowed(false);
        // coluna de cabeçalho das linhas
        tabela.getColumnModel().getColumn(0).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(0).setMinWidth(95);
        for (int c = 1; c <= n; c++) {
            tabela.getColumnModel().getColumn(c).setPreferredWidth(40);
            tabela.getColumnModel().getColumn(c).setMinWidth(30);
        }
        tabela.setDefaultRenderer(Object.class, new CelulaRenderer(passos, k));

        // label colorida com total de faltas
        JLabel lblFaltas = new JLabel(
                "  " + res.nomeAlgoritmo + "   —   "
                        + res.totalFaltas + " faltas de página  ");
        lblFaltas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblFaltas.setForeground(Color.WHITE);
        lblFaltas.setOpaque(true);
        lblFaltas.setBackground(cor);
        lblFaltas.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));

        JLabel legenda = new JLabel(
                "   Vermelho = falta de página (✗)   |   "
                        + "Verde = página já na memória (✓)  ");
        legenda.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        legenda.setForeground(new Color(80, 80, 80));
        legenda.setOpaque(true);
        legenda.setBackground(new Color(245, 245, 245));
        legenda.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CINZA_LINHA));

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(lblFaltas, BorderLayout.NORTH);
        topo.add(legenda,   BorderLayout.SOUTH);

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ── Renderizador da tabela ─────────────────────────────────────────────────
    private static final class CelulaRenderer extends DefaultTableCellRenderer {

        private static final Color FUNDO_FALTA       = new Color(255, 205, 210);
        private static final Color FUNDO_HIT         = new Color(232, 245, 233);
        private static final Color REF_FALTA         = new Color(183,  28,  28);
        private static final Color REF_HIT           = new Color( 21,  101, 192);
        private static final Color INDICADOR_FALTA   = new Color(229,  57,  53);
        private static final Color INDICADOR_HIT     = new Color( 67, 160,  71);

        private final List<Passo> passos;
        private final int k;

        CelulaRenderer(List<Passo> passos, int k) {
            this.passos = passos;
            this.k = k;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(CENTER);
            setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

            // coluna de rótulos (col 0)
            if (col == 0) {
                setBackground(CELL_HEADER);
                setForeground(new Color(55, 71, 79));
                setFont(getFont().deriveFont(Font.BOLD, 12f));
                return this;
            }

            Passo p = passos.get(col - 1);
            int ultimaLinha = k + 1;

            if (row == 0) {
                // linha "Referência"
                setBackground(p.falta ? REF_FALTA : REF_HIT);
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(Font.BOLD, 13f));

            } else if (row == ultimaLinha) {
                // linha "Falta?"
                setBackground(p.falta ? INDICADOR_FALTA : INDICADOR_HIT);
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(Font.BOLD, 15f));

            } else {
                // linhas dos quadros de memória
                setBackground(p.falta ? FUNDO_FALTA : FUNDO_HIT);
                setForeground(new Color(33, 33, 33));
                setFont(getFont().deriveFont(Font.PLAIN, 12f));
            }
            return this;
        }
    }

    // ── Gráfico de barras comparativo ──────────────────────────────────────────
    private static final class GraficoBarra extends JPanel {

        private Map<String, Integer> dados = new LinkedHashMap<>();

        void setDados(Map<String, Integer> dados) {
            this.dados = new LinkedHashMap<>(dados);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);

            if (dados.isEmpty()) {
                g2.setColor(new Color(150, 150, 150));
                g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
                String msg = "Execute a simulação para ver o gráfico.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            int margE = 46, margB = 44, margT = 16;
            int areaW = w - margE - 16;
            int areaH = h - margB - margT;
            int maxVal = dados.values().stream().mapToInt(v -> v).max().orElse(1);
            int n = dados.size();
            int gap = 20;
            int barW = Math.max(30, (areaW - gap * (n + 1)) / n);

            // eixos
            g2.setColor(new Color(180, 180, 180));
            g2.drawLine(margE, margT, margE, margT + areaH);
            g2.drawLine(margE, margT + areaH, w - 16, margT + areaH);

            // grade horizontal
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            for (int m = 1; m <= 4; m++) {
                int val = maxVal * m / 4;
                if (val <= 0) continue;
                int y = margT + areaH - (int) ((double) val / maxVal * areaH);
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(margE, y, w - 16, y);
                g2.setColor(new Color(100, 100, 100));
                String s = String.valueOf(val);
                g2.drawString(s, margE - 4 - g2.getFontMetrics().stringWidth(s), y + 4);
            }

            // barras
            int i = 0;
            for (Map.Entry<String, Integer> e : dados.entrySet()) {
                Color cor = CORES_ALG[i % CORES_ALG.length];
                int x = margE + gap + i * (barW + gap);
                int barH = (int) ((double) e.getValue() / maxVal * areaH);
                barH = Math.max(barH, e.getValue() > 0 ? 4 : 0);
                int y = margT + areaH - barH;

                // sombra
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(x + 3, y + 3, barW, barH, 8, 8);

                // barra
                g2.setColor(cor);
                g2.fillRoundRect(x, y, barW, barH, 8, 8);
                g2.setColor(cor.darker());
                g2.drawRoundRect(x, y, barW, barH, 8, 8);

                // valor em cima
                g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
                g2.setColor(cor.darker());
                String val = String.valueOf(e.getValue());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 5);

                // rótulo embaixo
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2.setColor(new Color(60, 60, 60));
                String nome = e.getKey();
                fm = g2.getFontMetrics();
                g2.drawString(nome,
                        x + (barW - fm.stringWidth(nome)) / 2,
                        margT + areaH + 16);
                i++;
            }
            g2.dispose();
        }
    }

    // ── Utilitários ────────────────────────────────────────────────────────────
    static int[] parsear(String raw) {
        if (raw == null || raw.isBlank()) return new int[0];
        String[] partes = raw.trim().split("\\s+");
        int[] r = new int[partes.length];
        for (int i = 0; i < partes.length; i++) r[i] = Integer.parseInt(partes[i]);
        return r;
    }

    private static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return l;
    }

    private static JButton botao(String texto, Color bg, Color fg, boolean negrito) {
        JButton b = new JButton(texto);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, negrito ? Font.BOLD : Font.PLAIN, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Ponto de entrada ───────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new SimuladorApp().setVisible(true));
    }
}
