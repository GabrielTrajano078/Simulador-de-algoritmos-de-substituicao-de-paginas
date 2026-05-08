/**
 * Simulação alinhada às classes Java (Fifo, Lru, Relogio, Otimo).
 */

function indexOfFrame(frames, page) {
  for (let i = 0; i < frames.length; i++) {
    if (frames[i] === page) return i;
  }
  return -1;
}

function cloneFrames(frames) {
  return frames.slice();
}

function simulateFifo(referencias, quadros) {
  const passos = [];
  if (quadros <= 0 || !referencias.length) {
    return { nome: "FIFO", passos, totalFaltas: 0 };
  }
  const frames = Array(quadros).fill(-1);
  const insertTime = Array(quadros).fill(0);
  let tempo = 0;
  let faltas = 0;

  for (const pagina of referencias) {
    let slot = indexOfFrame(frames, pagina);
    const falta = slot < 0;
    if (falta) {
      faltas++;
      const livre = frames.indexOf(-1);
      if (livre >= 0) {
        frames[livre] = pagina;
        insertTime[livre] = tempo++;
      } else {
        let minSlot = 0;
        for (let i = 1; i < quadros; i++) {
          if (insertTime[i] < insertTime[minSlot]) minSlot = i;
        }
        frames[minSlot] = pagina;
        insertTime[minSlot] = tempo++;
      }
    }
    passos.push({ referencia: pagina, molduras: cloneFrames(frames), falta });
  }
  return { nome: "FIFO", passos, totalFaltas: faltas };
}

function simulateLru(referencias, quadros) {
  const passos = [];
  if (quadros <= 0 || !referencias.length) {
    return { nome: "LRU", passos, totalFaltas: 0 };
  }
  const frames = Array(quadros).fill(-1);
  const lastUse = Array(quadros).fill(0);
  let tempo = 0;
  let faltas = 0;

  for (const pagina of referencias) {
    let slot = indexOfFrame(frames, pagina);
    const falta = slot < 0;
    if (falta) {
      faltas++;
      const livre = frames.indexOf(-1);
      if (livre >= 0) {
        frames[livre] = pagina;
        lastUse[livre] = tempo++;
      } else {
        let minSlot = 0;
        for (let i = 1; i < quadros; i++) {
          if (lastUse[i] < lastUse[minSlot]) minSlot = i;
        }
        frames[minSlot] = pagina;
        lastUse[minSlot] = tempo++;
      }
    } else {
      lastUse[slot] = tempo++;
    }
    passos.push({ referencia: pagina, molduras: cloneFrames(frames), falta });
  }
  return { nome: "LRU", passos, totalFaltas: faltas };
}

function simulateRelogio(referencias, quadros) {
  const passos = [];
  if (quadros <= 0 || !referencias.length) {
    return { nome: "Relógio", passos, totalFaltas: 0 };
  }
  const frames = Array(quadros).fill(-1);
  const refBit = Array(quadros).fill(false);
  let ponteiro = 0;
  let faltas = 0;

  for (const pagina of referencias) {
    const slot = indexOfFrame(frames, pagina);
    const falta = slot < 0;
    if (falta) {
      faltas++;
      while (frames[ponteiro] !== -1 && refBit[ponteiro]) {
        refBit[ponteiro] = false;
        ponteiro = (ponteiro + 1) % quadros;
      }
      frames[ponteiro] = pagina;
      refBit[ponteiro] = true;
      ponteiro = (ponteiro + 1) % quadros;
    } else {
      refBit[slot] = true;
    }
    passos.push({ referencia: pagina, molduras: cloneFrames(frames), falta });
  }
  return { nome: "Relógio", passos, totalFaltas: faltas };
}

function proximaOcorrencia(ref, pagina, from) {
  const INF = Number.MAX_SAFE_INTEGER;
  for (let j = from; j < ref.length; j++) {
    if (ref[j] === pagina) return j;
  }
  return INF;
}

function escolherVitima(frames, ref, atual) {
  const INF = Number.MAX_SAFE_INTEGER;
  let melhorSlot = 0;
  let melhorProximo = -1;
  for (let i = 0; i < frames.length; i++) {
    const prox = proximaOcorrencia(ref, frames[i], atual + 1);
    if (prox === INF) return i;
    if (prox > melhorProximo) {
      melhorProximo = prox;
      melhorSlot = i;
    }
  }
  return melhorSlot;
}

function simulateOtimo(referencias, quadros) {
  const passos = [];
  if (quadros <= 0 || !referencias.length) {
    return { nome: "Ótimo", passos, totalFaltas: 0 };
  }
  const frames = Array(quadros).fill(-1);
  let faltas = 0;

  for (let i = 0; i < referencias.length; i++) {
    const pagina = referencias[i];
    const slot = indexOfFrame(frames, pagina);
    const falta = slot < 0;
    if (falta) {
      faltas++;
      const livre = frames.indexOf(-1);
      if (livre >= 0) {
        frames[livre] = pagina;
      } else {
        const vitima = escolherVitima(frames, referencias, i);
        frames[vitima] = pagina;
      }
    }
    passos.push({ referencia: pagina, molduras: cloneFrames(frames), falta });
  }
  return { nome: "Ótimo", passos, totalFaltas: faltas };
}

const SIMULATORS = {
  fifo: simulateFifo,
  lru: simulateLru,
  relogio: simulateRelogio,
  otimo: simulateOtimo,
};

function parseReferencias(text) {
  const trimmed = text.trim();
  if (!trimmed) return [];
  return trimmed.split(/\s+/).map((s) => {
    const n = parseInt(s, 10);
    if (Number.isNaN(n)) throw new Error(`Valor inválido: "${s}"`);
    return n;
  });
}

function renderStepTable(result) {
  const { passos } = result;
  if (!passos.length) return "<p class='field-hint'>Sem dados.</p>";
  const k = passos[0].molduras.length;
  const n = passos.length;
  let html = '<div class="table-scroll"><table class="step-table"><thead><tr>';
  html += '<th class="row-label"></th>';
  for (let c = 0; c < n; c++) {
    html += `<th>${c + 1}</th>`;
  }
  html += "</tr></thead><tbody>";

  html += "<tr><td class='row-label'>Referência</td>";
  for (let c = 0; c < n; c++) {
    const p = passos[c];
    const cls = p.falta ? "ref-miss" : "ref-hit";
    html += `<td class="${cls}">${p.referencia}</td>`;
  }
  html += "</tr>";

  for (let f = 0; f < k; f++) {
    html += `<tr><td class='row-label'>Quadro ${f + 1}</td>`;
    for (let c = 0; c < n; c++) {
      const p = passos[c];
      const v = p.molduras[f];
      const cls = p.falta ? "cell-miss" : "cell-hit";
      html += `<td class="${cls}">${v < 0 ? "—" : v}</td>`;
    }
    html += "</tr>";
  }

  html += "<tr><td class='row-label'>Falta?</td>";
  for (let c = 0; c < n; c++) {
    const p = passos[c];
    const cls = p.falta ? "ref-miss" : "ref-hit";
    html += `<td class="${cls}">${p.falta ? "✗" : "✓"}</td>`;
  }
  html += "</tr></tbody></table></div>";
  return html;
}

let chartInstance = null;

function updateChart(results) {
  const ctx = document.getElementById("compareChart");
  if (!ctx) return;
  const labels = results.map((r) => r.nome);
  const data = results.map((r) => r.totalFaltas);
  const colors = [
    "rgba(34, 197, 94, 0.75)",
    "rgba(59, 130, 246, 0.75)",
    "rgba(249, 115, 22, 0.75)",
    "rgba(168, 85, 247, 0.75)",
  ];

  if (chartInstance) chartInstance.destroy();
  chartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "Faltas de página",
          data,
          backgroundColor: labels.map((_, i) => colors[i % colors.length]),
          borderColor: labels.map((_, i) => colors[i % colors.length].replace("0.75", "1")),
          borderWidth: 1,
          borderRadius: 6,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        title: {
          display: true,
          text: "Comparativo de faltas por algoritmo",
          color: "#8b949e",
          font: { size: 14, family: "'DM Sans', sans-serif" },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: { color: "#8b949e", stepSize: 1 },
          grid: { color: "rgba(42, 51, 64, 0.8)" },
        },
        x: {
          ticks: { color: "#e8eaed" },
          grid: { display: false },
        },
      },
    },
  });
}

function setStep(activeIndex) {
  document.querySelectorAll(".step").forEach((el, i) => {
    el.classList.remove("active", "done");
    if (i < activeIndex) el.classList.add("done");
    if (i === activeIndex) el.classList.add("active");
  });
}

function showError(msg) {
  const b = document.getElementById("error-banner");
  b.textContent = msg;
  b.classList.add("visible");
}

function hideError() {
  document.getElementById("error-banner").classList.remove("visible");
}

function getSelectedAlgos() {
  const boxes = document.querySelectorAll('.algo-chip input[type="checkbox"]:checked');
  return Array.from(boxes).map((cb) => cb.value);
}

function runSimulation() {
  hideError();
  const seqText = document.getElementById("input-seq").value;
  const k = parseInt(document.getElementById("input-frames").value, 10);
  let refs;
  try {
    refs = parseReferencias(seqText);
  } catch (e) {
    showError(e.message || "Sequência inválida.");
    return;
  }
  if (!refs.length) {
    showError("Informe ao menos um número na sequência de páginas.");
    return;
  }
  if (Number.isNaN(k) || k < 1) {
    showError("Número de quadros deve ser um inteiro ≥ 1.");
    return;
  }
  const selected = getSelectedAlgos();
  if (!selected.length) {
    showError("Selecione ao menos um algoritmo.");
    return;
  }

  setStep(1);
  const results = selected.map((key) => SIMULATORS[key](refs, k));

  window.setTimeout(() => {
    setStep(2);

    const section = document.getElementById("results-section");
    const container = document.getElementById("algo-results");
    container.innerHTML = "";

    results.forEach((res) => {
      const div = document.createElement("div");
      div.className = "results-card";
      div.innerHTML = `
        <h2>
          ${res.nome}
          <span class="badge-faltas">${res.totalFaltas} faltas</span>
        </h2>
        ${renderStepTable(res)}
      `;
      container.appendChild(div);
    });

    section.classList.add("visible");
    section.scrollIntoView({ behavior: "smooth", block: "start" });
    window.requestAnimationFrame(() => updateChart(results));
  }, 280);
}

function loadTestCase() {
  document.getElementById("input-seq").value =
    "7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1";
  document.getElementById("input-frames").value = "3";
  document.querySelectorAll('.algo-chip input[type="checkbox"]').forEach((cb) => {
    cb.checked = true;
  });
  hideError();
}

function randomData() {
  const len = 12 + Math.floor(Math.random() * 14);
  const maxPage = 5 + Math.floor(Math.random() * 6);
  const arr = [];
  for (let i = 0; i < len; i++) {
    arr.push(Math.floor(Math.random() * (maxPage + 1)));
  }
  document.getElementById("input-seq").value = arr.join(" ");
  document.getElementById("input-frames").value = String(2 + Math.floor(Math.random() * 4));
  hideError();
}

function clearForm() {
  document.getElementById("input-seq").value = "";
  document.getElementById("input-frames").value = "3";
  document.getElementById("results-section").classList.remove("visible");
  document.getElementById("algo-results").innerHTML = "";
  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
  setStep(0);
  hideError();
}

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("btn-simulate").addEventListener("click", runSimulation);
  document.getElementById("btn-test").addEventListener("click", loadTestCase);
  document.getElementById("btn-random").addEventListener("click", randomData);
  document.getElementById("btn-clear").addEventListener("click", clearForm);
  loadTestCase();
});
