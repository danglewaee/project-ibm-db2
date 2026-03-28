const defaultFlows = [
    "create-order-draft",
    "reserve-stock-by-warehouse",
    "cancel-order-and-release-reservations",
    "ship-order-partially-or-fully",
    "count-stock-and-reconcile",
    "lookup-audit-trail-by-correlation-id"
];

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

function setHealthState(status) {
    const pill = document.getElementById("health-pill");
    const dot = document.getElementById("health-dot");
    const copy = document.getElementById("health-copy");
    const up = status === "UP";

    if (pill) {
        pill.textContent = status;
        pill.classList.remove("up", "down");
        pill.classList.add(up ? "up" : "down");
    }

    if (dot) {
        dot.classList.remove("up", "down");
        dot.classList.add(up ? "up" : "down");
    }

    if (copy) {
        copy.textContent = up ? "Live service is up" : "Live service is unavailable";
    }
}

function renderFlows(flows) {
    const list = document.getElementById("supported-flows");
    const count = document.getElementById("flow-count");
    if (!list) {
        return;
    }

    const items = flows.length > 0 ? flows : defaultFlows;
    list.innerHTML = "";

    items.forEach((flow) => {
        const li = document.createElement("li");
        li.textContent = flow;
        list.appendChild(li);
    });

    if (count) {
        count.textContent = `${items.length} flows`;
    }
}

async function loadHealth() {
    try {
        const response = await fetch("/actuator/health");
        if (!response.ok) {
            throw new Error(`Health endpoint returned ${response.status}`);
        }

        const payload = await response.json();
        setHealthState(payload.status || "UNKNOWN");
    } catch (error) {
        setHealthState("DOWN");
    }
}

async function loadSystemInfo() {
    try {
        const response = await fetch("/api/v1/system/info");
        if (!response.ok) {
            throw new Error(`System info endpoint returned ${response.status}`);
        }

        const payload = await response.json();
        setText("application-name", payload.application || "Unavailable");
        setText("runtime-mode", payload.runtimeMode || "Unavailable");
        setText("persistence-mode", payload.persistenceMode || "Unavailable");
        setText("ddl-path", payload.ddlPath || "Unavailable");
        renderFlows(payload.supportedFlows || []);
    } catch (error) {
        setText("application-name", "Unavailable");
        setText("runtime-mode", "Unavailable");
        setText("persistence-mode", "Unavailable");
        setText("ddl-path", "Unavailable");
        renderFlows([]);
    }
}

loadHealth();
loadSystemInfo();
