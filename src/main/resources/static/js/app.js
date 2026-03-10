const registerForm = document.getElementById("register-form");
const loginForm = document.getElementById("login-form");
const storeForm = document.getElementById("store-form");
const gameForm = document.getElementById("game-form");
const stepBtn = document.getElementById("step-btn");
const autoBtn = document.getElementById("auto-btn");
const stopBtn = document.getElementById("stop-btn");
const statusEl = document.getElementById("status");
const trackEl = document.getElementById("track");
const lastCardEl = document.getElementById("last-card");
const historyEl = document.getElementById("history");
const recentGamesEl = document.getElementById("recent-games");
const summaryEl = document.getElementById("player-summary");
const playerNameEl = document.getElementById("player-name");
const playerEmailEl = document.getElementById("player-email");
const playerGroupEl = document.getElementById("player-group");
const playerPointsEl = document.getElementById("player-points");

let userId = Number(localStorage.getItem("raceUserId")) || null;
let gameId = null;
let autoTimer = null;
let currentState = null;

registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await authenticate("/api/auth/register", {
        name: document.getElementById("register-name").value,
        email: document.getElementById("register-email").value,
        password: document.getElementById("register-password").value
    });
});

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await authenticate("/api/auth/login", {
        email: document.getElementById("login-email").value,
        password: document.getElementById("login-password").value
    });
});

storeForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!requireLogin()) {
        return;
    }

    const packageCount = Number(document.getElementById("package-count").value);
    try {
        const response = await fetch("/api/store/purchase", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId, packageCount })
        });
        const data = await response.json();
        if (!response.ok) {
            setStatus(data.message || "No se pudo completar la compra.");
            return;
        }
        updatePlayerBalance(data.newBalance);
        setStatus(`Compra exitosa: ${data.purchasedPoints} puntos por ${formatCop(data.amountCop)}.`);
        await loadDashboard();
    } catch (error) {
        setStatus("Error de conexion con el servidor.");
    }
});

gameForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!requireLogin()) {
        return;
    }

    const distance = Number(document.getElementById("distance").value);
    const betPoints = Number(document.getElementById("bet-points").value);
    const selectedHorse = document.getElementById("selected-horse").value;
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
            body: JSON.stringify({ userId, distance, horses, selectedHorse, betPoints })
        });
        const data = await response.json();
        if (!response.ok) {
            setStatus(data.message || "No se pudo crear la carrera.");
            return;
        }

        gameId = data.id;
        currentState = data;
        renderState(data);
        updatePlayerBalance(data.playerPointsBalance);
        stepBtn.disabled = false;
        autoBtn.disabled = false;
        stopBtn.disabled = true;
        setStatus(`Carrera creada para ${data.userName}. Apuesta: ${data.betPoints} puntos a ${displaySuit(data.selectedHorse)}.`);
        await loadDashboard();
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

async function authenticate(url, payload) {
    try {
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) {
            setStatus(data.message || "No fue posible autenticar el usuario.");
            return;
        }

        userId = data.userId;
        localStorage.setItem("raceUserId", String(userId));
        applyAuthData(data);
        setStatus(`Sesion iniciada. Grupo asignado: ${data.group.code}.`);
        await loadDashboard();
    } catch (error) {
        setStatus("Error de conexion con el servidor.");
    }
}

async function loadDashboard() {
    if (!userId) {
        return;
    }

    try {
        const response = await fetch(`/api/auth/users/${userId}`);
        const data = await response.json();
        if (!response.ok) {
            setStatus(data.message || "No fue posible cargar el tablero del usuario.");
            return;
        }
        playerNameEl.textContent = data.name;
        playerEmailEl.textContent = data.email;
        playerGroupEl.textContent = `${data.group.code} (${data.group.memberCount}/${data.group.maxMembers})`;
        updatePlayerBalance(data.pointsBalance);
        renderRecentGames(data.recentGames);
        summaryEl.classList.remove("hidden");
    } catch (error) {
        setStatus("Error de conexion con el servidor.");
    }
}

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
        updatePlayerBalance(data.playerPointsBalance);

        if (data.winner) {
            const payoutMessage = data.payoutPoints > 0
                ? ` Ganaste ${data.payoutPoints} puntos.`
                : " No hubo premio en esta apuesta.";
            setStatus(`Ganador: ${displaySuit(data.winner)} en el turno ${data.turn}.${payoutMessage}`);
            stepBtn.disabled = true;
            autoBtn.disabled = true;
            stopBtn.disabled = true;
            await loadDashboard();
            return true;
        }

        setStatus(`Turno ${data.turn} completado.`);
        return false;
    } catch (error) {
        setStatus("Error de conexion con el servidor.");
        return false;
    }
}

function applyAuthData(data) {
    playerNameEl.textContent = data.name;
    playerEmailEl.textContent = data.email;
    playerGroupEl.textContent = `${data.group.code} (${data.group.memberCount}/${data.group.maxMembers})`;
    updatePlayerBalance(data.pointsBalance);
    summaryEl.classList.remove("hidden");
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
        const selectedTag = horse === state.selectedHorse ? " | Apuesta" : "";
        label.textContent = `${displaySuit(horse)} - Posicion ${pos}/${state.distance}${selectedTag}`;
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

function renderRecentGames(games) {
    recentGamesEl.innerHTML = "";
    if (!games || games.length === 0) {
        const li = document.createElement("li");
        li.textContent = "Aun no hay apuestas registradas.";
        recentGamesEl.appendChild(li);
        return;
    }

    for (const item of games) {
        const li = document.createElement("li");
        const winner = item.winnerHorse ? displaySuit(item.winnerHorse) : "Pendiente";
        li.textContent = `${new Date(item.createdAt).toLocaleString()} | Apostaste ${item.betPoints} a ${displaySuit(item.selectedHorse)} | Ganador: ${winner} | Premio: ${item.payoutPoints}`;
        recentGamesEl.appendChild(li);
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

function updatePlayerBalance(balance) {
    playerPointsEl.textContent = balance;
}

function requireLogin() {
    if (userId) {
        return true;
    }
    setStatus("Debes registrarte o iniciar sesion antes de jugar.");
    return false;
}

function formatCop(value) {
    return new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 }).format(value);
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

if (userId) {
    loadDashboard();
}
