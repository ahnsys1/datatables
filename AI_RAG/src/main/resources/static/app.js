const conversation = document.querySelector("#conversation");
const welcome = document.querySelector("#welcome");
const form = document.querySelector("#chat-form");
const input = document.querySelector("#message-input");
const sendButton = document.querySelector("#send-button");
const fileInput = document.querySelector("#file-input");
const dropZone = document.querySelector("#drop-zone");
const uploadList = document.querySelector("#upload-list");
const documentCount = document.querySelector("#document-count");
const library = document.querySelector("#library");
const scrim = document.querySelector("#scrim");
const chatScrollbar = document.querySelector("#chat-scrollbar");
const chatScrollbarThumb = chatScrollbar.querySelector("span");

let conversationId = crypto.randomUUID();
let messagesContainer;
let uploadedDocuments = 0;
let scrollbarUpdatePending = false;

lucide.createIcons();

function updateChatScrollbar() {
    if (scrollbarUpdatePending) return;
    scrollbarUpdatePending = true;
    requestAnimationFrame(() => {
        scrollbarUpdatePending = false;
        updateChatScrollbarNow();
    });
}

function updateChatScrollbarNow() {
    const trackHeight = chatScrollbar.clientHeight;
    const thumbHeight = Math.max(18, trackHeight * conversation.clientHeight / conversation.scrollHeight);
    const maxThumbTop = Math.max(0, trackHeight - thumbHeight);
    const maxScrollTop = Math.max(1, conversation.scrollHeight - conversation.clientHeight);
    chatScrollbarThumb.style.height = `${Math.min(trackHeight, thumbHeight)}px`;
    chatScrollbarThumb.style.transform = `translateY(${maxThumbTop * conversation.scrollTop / maxScrollTop}px)`;
}

conversation.addEventListener("scroll", updateChatScrollbar);
new ResizeObserver(updateChatScrollbar).observe(conversation);
new MutationObserver(updateChatScrollbar).observe(conversation, { childList: true, subtree: true, characterData: true });
chatScrollbar.addEventListener("click", (event) => {
    if (event.target === chatScrollbarThumb) return;
    const thumbCenter = chatScrollbarThumb.clientHeight / 2;
    const thumbTop = event.clientY - chatScrollbar.getBoundingClientRect().top - thumbCenter;
    const availableTrack = chatScrollbar.clientHeight - chatScrollbarThumb.clientHeight;
    const ratio = Math.max(0, Math.min(1, thumbTop / Math.max(1, availableTrack)));
    conversation.scrollTo({
        top: ratio * (conversation.scrollHeight - conversation.clientHeight),
        behavior: "instant"
    });
    updateChatScrollbarNow();
});
chatScrollbarThumb.addEventListener("pointerdown", (event) => {
    const startY = event.clientY;
    const startScrollTop = conversation.scrollTop;
    const availableTrack = chatScrollbar.clientHeight - chatScrollbarThumb.clientHeight;
    const availableScroll = conversation.scrollHeight - conversation.clientHeight;
    chatScrollbarThumb.setPointerCapture(event.pointerId);

    const moveThumb = (moveEvent) => {
        if (availableTrack > 0) {
            conversation.scrollTo({
                top: startScrollTop + (moveEvent.clientY - startY) * availableScroll / availableTrack,
                behavior: "instant"
            });
            updateChatScrollbarNow();
        }
    };
    chatScrollbarThumb.addEventListener("pointermove", moveThumb);
    chatScrollbarThumb.addEventListener("pointerup", () => {
        chatScrollbarThumb.removeEventListener("pointermove", moveThumb);
    }, { once: true });
});

function ensureMessagesContainer() {
    if (!messagesContainer) {
        welcome.remove();
        messagesContainer = document.createElement("div");
        messagesContainer.className = "messages";
        conversation.append(messagesContainer);
    }
    return messagesContainer;
}

function addMessage(role, text, sources = []) {
    const message = document.createElement("article");
    message.className = `message ${role}`;

    const avatar = document.createElement("div");
    avatar.className = "message-avatar";
    avatar.setAttribute("aria-hidden", "true");
    avatar.innerHTML = `<i data-lucide="${role === "assistant" ? "sparkles" : "user-round"}"></i>`;

    const content = document.createElement("div");
    content.className = "message-content";
    content.textContent = text;

    if (sources.length) {
        const sourceList = document.createElement("div");
        sourceList.className = "sources";
        sources.forEach((sourceData) => {
            const sourceName = typeof sourceData === "string" ? sourceData : sourceData.fileName;
            const documentId = typeof sourceData === "string" ? "" : sourceData.documentId;
            const source = document.createElement(documentId ? "a" : "span");
            source.className = "source";
            if (documentId) {
                source.href = `/api/documents/${encodeURIComponent(documentId)}/content`;
                source.target = "_blank";
                source.rel = "noopener";
                source.title = `Otevřít původní PDF: ${sourceName}`;
            }
            source.innerHTML = '<i data-lucide="file-text" aria-hidden="true"></i>';
            const label = document.createElement("span");
            label.textContent = sourceName;
            source.append(label);
            sourceList.append(source);
        });
        content.append(sourceList);
    }

    message.append(avatar, content);
    ensureMessagesContainer().append(message);
    lucide.createIcons({ nodes: [message] });
    conversation.scrollTo({ top: conversation.scrollHeight, behavior: "instant" });
    updateChatScrollbarNow();
    new ResizeObserver(updateChatScrollbar).observe(messagesContainer);
    return message;
}

function addThinking() {
    const message = addMessage("assistant", "");
    const content = message.querySelector(".message-content");
    content.innerHTML = '<div class="thinking" aria-label="Model připravuje odpověď"><span></span><span></span><span></span></div>';
    return message;
}

async function askModel(message) {
    addMessage("user", message);
    const thinking = addThinking();
    sendButton.disabled = true;

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ conversationId, message })
        });
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(data.message || "Model nyní neodpovídá.");
        }
        thinking.remove();
        addMessage("assistant", data.answer, data.sources);
    } catch (error) {
        thinking.remove();
        addMessage("assistant", `Nepodařilo se získat odpověď: ${error.message}`);
    } finally {
        sendButton.disabled = false;
        input.focus();
    }
}

form.addEventListener("submit", (event) => {
    event.preventDefault();
    const message = input.value.trim();
    if (!message || sendButton.disabled) return;
    input.value = "";
    input.style.height = "auto";
    askModel(message);
});

input.addEventListener("input", () => {
    input.style.height = "auto";
    input.style.height = `${Math.min(input.scrollHeight, 150)}px`;
});

input.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        form.requestSubmit();
    }
});

document.querySelectorAll("[data-question]").forEach((button) => {
    button.addEventListener("click", () => askModel(button.dataset.question));
});

function startIndexJob(file, onUploadProgress) {
    return new Promise((resolve, reject) => {
        const request = new XMLHttpRequest();
        request.open("POST", "/api/documents");
        request.responseType = "json";
        request.timeout = 120000;

        request.upload.addEventListener("progress", (event) => {
            if (event.lengthComputable) {
                onUploadProgress(Math.round(event.loaded * 100 / event.total));
            }
        });
        request.addEventListener("load", () => {
            const response = request.response || {};
            if (request.status >= 200 && request.status < 300 && response.jobId) {
                resolve(response);
            } else {
                reject(new Error(response.message || "Nahrání selhalo."));
            }
        });
        request.addEventListener("error", () => reject(new Error("Nahrání selhalo.")));
        request.addEventListener("timeout", () => reject(new Error("Nahrávání překročilo časový limit.")));

        const body = new FormData();
        body.append("file", file);
        request.send(body);
    });
}

function formatDuration(totalSeconds) {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor(totalSeconds % 3600 / 60);
    const seconds = totalSeconds % 60;
    return hours > 0
        ? `${hours}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`
        : `${minutes}:${String(seconds).padStart(2, "0")}`;
}

function formatIndexStatus(status) {
    const elapsed = formatDuration(status.elapsedSeconds || 0);
    const estimatedEnd = status.estimatedCompletionAt
        ? new Date(status.estimatedCompletionAt).toLocaleTimeString("cs-CZ", { hour: "2-digit", minute: "2-digit" })
        : "počítám…";
    return `${status.message} ${status.progress} % · Uplynulo ${elapsed} · Odhad konce ${estimatedEnd}`;
}

async function uploadFile(file) {
    const item = document.createElement("div");
    item.className = "upload-item";
    item.innerHTML = '<div class="upload-icon"><i data-lucide="file-text"></i></div><div class="upload-details"><strong></strong><span>Nahrávám PDF…</span><div class="index-progress"><span></span></div></div><div class="upload-state loading"></div>';
    item.querySelector("strong").textContent = file.name;
    uploadList.prepend(item);
    lucide.createIcons({ nodes: [item] });

    try {
        const started = await startIndexJob(file, (progress) => {
            item.querySelector(".upload-details > span").textContent = `Nahrávám PDF… ${progress} %`;
            item.querySelector(".index-progress span").style.width = `${progress}%`;
        });
        item.querySelector(".upload-details > span").textContent = "Čeká na indexaci… 0 %";
        item.querySelector(".index-progress span").style.width = "0%";

        let completed;
        while (true) {
            const statusResponse = await fetch(`/api/documents/${encodeURIComponent(started.jobId)}/status`, {
                cache: "no-store"
            });
            if (!statusResponse.ok) throw new Error("Stav indexace není dostupný.");
            const status = await statusResponse.json();
            item.querySelector(".upload-details > span").textContent = formatIndexStatus(status);
            item.querySelector(".index-progress span").style.width = `${status.progress}%`;

            if (status.state === "complete") {
                completed = status;
                break;
            }
            if (status.state === "error") throw new Error(status.message || "Indexace selhala.");
            await new Promise((resolve) => setTimeout(resolve, 1000));
        }
        item.querySelector(".upload-details > span").textContent =
            `${completed.chunks} částí připraveno · Indexace trvala ${formatDuration(completed.elapsedSeconds || 0)}`;
        item.querySelector(".index-progress span").style.width = "100%";
        item.querySelector(".upload-state").outerHTML = '<i class="upload-state" data-lucide="circle-check"></i>';
        uploadedDocuments += 1;
        documentCount.textContent = uploadedDocuments;
    } catch (error) {
        item.classList.add("failed");
        item.querySelector(".upload-details > span").textContent = error.message;
        item.querySelector(".upload-state").outerHTML = '<i class="upload-state" data-lucide="circle-x"></i>';
    }
    lucide.createIcons({ nodes: [item] });
}

fileInput.addEventListener("change", () => {
    [...fileInput.files].forEach(uploadFile);
    fileInput.value = "";
});

["dragenter", "dragover"].forEach((eventName) => dropZone.addEventListener(eventName, (event) => {
    event.preventDefault();
    dropZone.classList.add("dragging");
}));

["dragleave", "drop"].forEach((eventName) => dropZone.addEventListener(eventName, (event) => {
    event.preventDefault();
    dropZone.classList.remove("dragging");
}));

dropZone.addEventListener("drop", (event) => {
    [...event.dataTransfer.files].filter((file) => file.type === "application/pdf" || file.name.toLowerCase().endsWith(".pdf")).forEach(uploadFile);
});

document.querySelector("#new-chat").addEventListener("click", async () => {
    await fetch(`/api/chat?conversationId=${encodeURIComponent(conversationId)}`, { method: "DELETE" }).catch(() => {});
    conversationId = crypto.randomUUID();
    if (messagesContainer) {
        messagesContainer.remove();
        messagesContainer = undefined;
        conversation.append(welcome);
    }
});

function toggleLibrary(open) {
    library.classList.toggle("open", open);
    scrim.classList.toggle("open", open);
}

document.querySelector("#menu-button").addEventListener("click", () => toggleLibrary(true));
scrim.addEventListener("click", () => toggleLibrary(false));