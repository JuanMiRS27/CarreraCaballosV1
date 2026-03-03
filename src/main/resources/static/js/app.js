const form = document.getElementById("game-form");
const stepBtn = document.getElementById("step-btn");
const autoBtn = document.getElementById("auto-btn");
const stopBtn = document.getElementById("stop-btn");
const statusEl = document.getElementById("status");
const trackEl = document.getElementById("track");
const lastCardEl = document.getElementById("last-card");
const historyEl = document.getElementById("history");

let gameId = null;
let autoTimer = null;
let currentState = null;

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const distance = Number(document.getElementById("distance").value);
    const horses = [...document.querySelectorAll('input[type="checkbox"]:checked')].map((cb) => cb.value);

    if (horses.length < 2 || horses.length > 4) {
        setStatus("Debes elegir entre 2 y 4 caballos.");
        return;
    }

    stopAuto();

    try {
        const response = await fetch("/api/games", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ distance, horses })
        });
        const data = await response.json();

        if (!response.ok) {
            setStatus(data.message || "No se pudo crear la carrera.");
            return;
        }

        gameId = data.id;
        currentState = data;
        renderState(data);
        stepBtn.disabled = false;
        autoBtn.disabled = false;
        stopBtn.disabled = true;
        setStatus(`Carrera iniciada. Turno ${data.turn}.`);
    } catch (error) {
        setStatus("Error de conexion con el servidor.");
    }
});

stepBtn.addEventListener("click", async () => {
    await advanceTurn();
});

autoBtn.addEventListener("click", () => {
    if (!gameId || autoTimer) {
        return;
    }
    stepBtn.disabled = true;
    autoBtn.disabled = true;
    stopBtn.disabled = false;
    autoTimer = setInterval(async () => {
        const finished = await advanceTurn();
        if (finished) {
            stopAuto();
        }
    }, 650);
});

stopBtn.addEventListener("click", () => {
    stopAuto();
    if (gameId && currentState?.winner == null) {
        stepBtn.disabled = false;
        autoBtn.disabled = false;
    }
});

async function advanceTurn() {
    if (!gameId) {
        return false;
    }

    try {
        const response = await fetch(`/api/games/${gameId}/step`, { method: "POST" });
        const data = await response.json();
        if (!response.ok) {
            setStatus(data.message || "No se pudo avanzar el turno.");
            return false;
        }

        currentState = data;
        renderState(data);

        if (data.winner) {
            setStatus(`Ganador: ${displaySuit(data.winner)} en el turno ${data.turn}.`);
            stepBtn.disabled = true;
            autoBtn.disabled = true;
            stopBtn.disabled = true;
            return true;
        }

        setStatus(`Turno ${data.turn} completado.`);
        return false;
    } catch (error) {
        setStatus("Error de conexion con el servidor.");
        return false;
    }
}

function renderState(state) {
    renderTrack(state);
    renderLastCard(state.lastTurn);
    renderHistory(state.history);
}

function renderTrack(state) {
    trackEl.innerHTML = "";
    for (const horse of state.horses) {
        const pos = state.positions[horse] ?? 0;
        const lane = document.createElement("div");
        lane.className = "lane";

        const label = document.createElement("div");
        label.className = "lane-label";
        label.textContent = `${displaySuit(horse)} - Posicion ${pos}/${state.distance}`;
        lane.appendChild(label);

        const grid = document.createElement("div");
        grid.className = "lane-grid";
        grid.style.gridTemplateColumns = `repeat(${state.distance}, 1fr)`;

        for (let i = 1; i <= state.distance; i += 1) {
            const cell = document.createElement("div");
            cell.className = "cell";
            if (i === state.distance) {
                cell.classList.add("finish");
            }
            if (i <= pos) {
                cell.classList.add("active");
            }
            grid.appendChild(cell);
        }
        lane.appendChild(grid);
        trackEl.appendChild(lane);
    }
}

function renderLastCard(lastTurn) {
    if (!lastTurn) {
        lastCardEl.textContent = "-";
        return;
    }
    let text = `${lastTurn.card.value} de ${displaySuit(lastTurn.card.suit)}`;
    if (lastTurn.penaltyTriggered && lastTurn.penaltyCard) {
        text += ` | Penalizacion: ${lastTurn.penaltyCard.value} de ${displaySuit(lastTurn.penaltyCard.suit)}`;
    }
    lastCardEl.textContent = text;
}

function renderHistory(history) {
    historyEl.innerHTML = "";
    const recent = [...history].slice(-10).reverse();
    for (const item of recent) {
        const li = document.createElement("li");
        let text;
        if (item.horseMoved) {
            text = `T${item.turn}: ${item.card.value} de ${displaySuit(item.card.suit)} mueve ${displaySuit(item.movedHorse)} a ${item.newPosition}`;
        } else {
            text = `T${item.turn}: ${item.card.value} de ${displaySuit(item.card.suit)} no mueve caballo`;
        }

        if (item.penaltyTriggered && item.penaltyCard) {
            if (item.penaltyApplied) {
                text += ` | Penalizacion: ${item.penaltyCard.value} de ${displaySuit(item.penaltyCard.suit)} hace retroceder a ${displaySuit(item.penalizedHorse)} a ${item.penalizedNewPosition}`;
            } else {
                text += ` | Penalizacion: ${item.penaltyCard.value} de ${displaySuit(item.penaltyCard.suit)} sin efecto`;
            }
        }
        li.textContent = text;
        historyEl.appendChild(li);
    }
}

function stopAuto() {
    if (autoTimer) {
        clearInterval(autoTimer);
        autoTimer = null;
    }
    stopBtn.disabled = true;
}

function setStatus(message) {
    statusEl.textContent = message;
}

function displaySuit(suit) {
    switch (suit) {
        case "ORO":
            return "Oro";
        case "ESPADA":
            return "Espada";
        case "BASTOS":
            return "Bastos";
        case "COPA":
            return "Copa";
        default:
            return suit;
    }
}
