# Simulador-de-algoritmos-de-substitui-o-de-p-ginas

**Universidade de Fortaleza (Unifor)**  
Centro de Ciências Tecnológicas · Ciência da Computação · Sistemas Operacionais
Alunos: Gabriel Trajano 2410361
        Davi Lira Cysne 2410372

GitHub:

| Item | Informação |
|------|------------|
| **Repositório** | _(substituir pelo link público do GitHub após publicar)_ |
| **Dupla** | **Autor 1:** ___ · **Autor 2:** ___ |

---

## Objetivo

Simular o comportamento de **quatro** políticas de substituição de páginas em memória virtual: dada uma **sequência de referências** (inteiros) e um número de **quadros** (molduras), o programa calcula as **faltas de página** de cada algoritmo e exibe o passo a passo da memória, além de um **comparativo em gráfico**.

> Referência visual (inspiração da interface web): [SDPM — Simulador Didático de Paginação de Memória](https://sdpm-simulator.netlify.app/).

---

## Algoritmos implementados

Conforme o enunciado, foram escolhidos **quatro** entre os seis usualmente estudados:

| Método | Política | Observação |
|--------|----------|------------|
| 1 | **FIFO** | First In, First Out — remove a página há mais tempo na memória. |
| 2 | **LRU** | Least Recently Used — remove a menos recentemente usada. |
| 3 | **Relógio** | Clock / segunda chance — ponteiro circular e bit de referência. |
| 4 | **Ótimo** | MIN — usa o **futuro** da sequência; serve só como limite teórico. |

**NFU** e **Envelhecimento (Aging)** aparecem no material da disciplina, mas **não** estão neste repositório (escopo: quatro métodos).

---

## O que o projeto contém

1. **Backend em Java** (`src/`) — implementação de referência dos quatro algoritmos, com saída passo a passo (`Passo`, `ResultadoSimulacao`).
2. **Interface desktop (Swing)** — `SimuladorApp`: abas por algoritmo, tabela colorida e gráfico de barras em `Graphics2D`.
3. **Interface web** (`web/`) — HTML, CSS e JavaScript **sem build**; tema escuro estilo SDPM; gráfico com [Chart.js](https://www.chartjs.org/) (CDN). A lógica em `app.js` foi escrita para **coincidir** com o Java.
4. **Console** — `ExecucaoConsole` imprime apenas o total de faltas por método (útil para testes rápidos).

---

## Pré-requisitos

- **Java:** [JDK 17+](https://adoptium.net/) (`java` e `javac` no PATH).
- **Web:** navegador moderno; **Python 3** só para servir arquivos estáticos (`python3 -m http.server`). Não é obrigatório Node/npm.

---

## Compilar o Java

Na **raiz** do repositório:

```bash
cd /caminho/para/TrabalhoSistOperacional
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

No **Windows** (Prompt de Comando), na raiz do projeto:

```bat
mkdir out 2>nul
javac -encoding UTF-8 -d out src\br\unifor\sop\simulador\*.java
```

No **PowerShell** (compila todos os `.java` de uma vez):

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
$src = (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
javac -encoding UTF-8 -d out $src
```

---

## Como executar

### 1) Interface web (recomendado para apresentação)

```bash
cd web
python3 -m http.server 8080
```

Abra: **http://localhost:8080**

No macOS, opcionalmente:

```bash
open http://localhost:8080
```

**Uso:** informe **quadros** e **sequência** (inteiros separados por espaço); marque os algoritmos; **Simular**. Botões **Caso de teste** (sequência clássica, 3 quadros), **Gerar aleatório** e **Limpar**.

> Abrir `index.html` direto pelo sistema de arquivos (`file://`) pode funcionar, mas o servidor local evita limitações em alguns navegadores.

### 2) Swing (desktop)

Após compilar:

```bash
java -cp out br.unifor.sop.simulador.SimuladorApp
```

### 3) Terminal (sem GUI)

```bash
java -cp out br.unifor.sop.simulador.ExecucaoConsole "7 0 1 2 0 3 0 4 2 3" 3
```

- 1º argumento: sequência.  
- 2º argumento (opcional): número de quadros (padrão: **3**).

**Formato de saída (exemplo):**

```
Método 1 (FIFO) - X faltas de página
Método 2 (LRU) - X faltas de página
Método 3 (Relógio) - X faltas de página
Método 4 (Ótimo) - X faltas de página
```

---

## Resultado de referência (validação)

Para a sequência padrão  
`7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1` com **3 quadros**, Java e web devem reportar:

| Algoritmo | Faltas |
|-----------|--------|
| FIFO | 15 |
| LRU | 12 |
| Relógio | 14 |
| Ótimo | 9 |

---

## Estrutura de pastas

```
TrabalhoSistOperacional/
├── src/br/unifor/sop/simulador/
│   ├── AlgoritmoSubstituicao.java
│   ├── Passo.java
│   ├── ResultadoSimulacao.java
│   ├── Fifo.java
│   ├── Lru.java
│   ├── Relogio.java
│   ├── Otimo.java
│   ├── SimuladorApp.java      # GUI Swing
│   └── ExecucaoConsole.java   # CLI
├── web/
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── out/                       # gerado após javac (pode apagar e recompilar)
└── README.md
```

---

## Dificuldades e decisões (relato da dupla)

- **Relógio:** o ponteiro circular e o bit de referência exigem cuidado nos primeiros passos, quando ainda há quadros vazios (`-1`). Mantivemos o mesmo critério do código Java: slot vazio é tratado como candidato imediato à carga.
- **Ótimo:** a escolha da vítima depende da **próxima ocorrência** de cada página na sequência futura; é didático, mas não implementável como política real sem previsão do futuro.
- **Swing:** tabela dinâmica + `TableCellRenderer` para cores por célula (falta vs acerto).
- **Web:** replicar a lógica em JavaScript evitou backend e manteve o projeto simples para correção; o visual segue a linha do SDPM (escuro, stepper, ações claras).
