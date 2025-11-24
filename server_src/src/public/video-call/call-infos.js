let startTime = null;
let timerInterval = null;
let elapsedSeconds = 0; 

function startCallTimer() {
    if (timerInterval) return;

    startTime = Date.now();
    const timerElement = document.getElementById("timer");
    
    if (!timerElement) {
        console.error("Elemento timer não encontrado!");
        return;
    }

    console.log("Iniciando cronômetro...");
    timerInterval = setInterval(() => {
        const now = Date.now();
        elapsedSeconds = (now - startTime) / 1000; 
        updateUI(elapsedSeconds);
    }, 1000); 
}

function stopCallTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }
    
    const timerElement = document.getElementById("timer");
    if (timerElement) timerElement.textContent = "00:00";
    
    return parseFloat(elapsedSeconds.toFixed(2));
}

function updateUI(seconds) {
    const timerElement = document.getElementById("timer");
    if (timerElement) {
        timerElement.textContent = formatTime(seconds);
    }
}

function formatTime(sec) {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60).toString().padStart(2, "0");
    return `${m}:${s}`;
}