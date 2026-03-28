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

function renderFlows(flows) {
    const list = document.getElementById("supported-flows");
    const count = document.getElementById("flow-count");
    if (!list || !count) {
        return;
    }

    list.innerHTML = "";
    const items = flows.length > 0 ? flows : defaultFlows;
    count.textContent = `${items.length} loaded`;

    items.forEach((flow) => {
        const li = document.createElement("li");
        li.textContent = flow;
        list.appendChild(li);
    });
}

async function loadHealth() {
    const pill = document.getElementById("health-pill");
    try {
        const response = await fetch("/actuator/health");
        if (!response.ok) {
            throw new Error(`Health endpoint returned ${response.status}`);
        }

        const payload = await response.json();
        const status = payload.status || "UNKNOWN";
        pill.textContent = status;
        pill.classList.add(status === "UP" ? "up" : "down");
    } catch (error) {
        pill.textContent = "Unavailable";
        pill.classList.add("down");
    }
}

async function loadSystemInfo() {
    try {
        const response = await fetch("/api/v1/system/info");
        if (!response.ok) {
            throw new Error(`System info endpoint returned ${response.status}`);
        }

        const payload = await response.json();
        setText("application-name", payload.application || "Unknown");
        setText("runtime-mode", payload.runtimeMode || "Unknown");
        setText("persistence-mode", payload.persistenceMode || "Unknown");
        setText("ddl-path", payload.ddlPath || "Unknown");
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
