// ==========================================
//  1. 設定・定数
// ==========================================
const TOAST_TYPES = {
    success: 'bg-green-500 text-white',
    error: 'bg-red-500 text-white',
    warning: 'bg-yellow-400 text-black',
    info: 'bg-blue-500 text-white',
};

const BASE_TOAST_CLASSES = 'p-4 rounded-lg shadow-lg min-w-[250px] transition-all duration-500';

// ==========================================
//  2. トースト機能 (通知)
// ==========================================
function showToast(message, type, duration = 3000) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.innerText = message;
    toast.className = `${BASE_TOAST_CLASSES} ${TOAST_TYPES[type]} animate-toast-in`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.remove('animate-toast-in');
        toast.classList.add('animate-toast-out');
        setTimeout(() => {
            toast.remove();
        }, 500);
    }, duration);
}

// ==========================================
//  3. モーダル機能 (ポップアップ)
// ==========================================
function openModal(title, message, onConfirm) {
    const overlay = document.getElementById('modal-overlay');
    const content = document.getElementById('modal-content');
    const titleEl = document.getElementById('modal-title');
    const msgEl = document.getElementById('modal-message');
    const actionBtn = document.getElementById('modal-action-btn');

    if (!overlay || !content) return;

    titleEl.innerText = title;
    msgEl.innerText = message;

    const newBtn = actionBtn.cloneNode(true);
    actionBtn.parentNode.replaceChild(newBtn, actionBtn);

    newBtn.onclick = () => {
        if (onConfirm) onConfirm();
        closeModal();
    };

    overlay.classList.remove('hidden');
    content.classList.remove('modal-pop');
    void content.offsetWidth;
    content.classList.add('modal-pop');
}

function closeModal() {
    const overlay = document.getElementById('modal-overlay');
    if (overlay) {
        overlay.classList.add('hidden');
    }
}

// ==========================================
//  4. データ取得・表示機能 (メイン)
// ==========================================
async function fetchItems() {
    console.log("1. fetchItems が呼ばれたよ！");

    try {
        const response = await fetch('/api/items');
        console.log("2. 通信結果:", response.status);

        if (!response.ok) throw new Error(`HTTPエラー: ${response.status}`);

        const data = await response.json();
        console.log("3. データ受け取ったよ:", data);

        const listContainer = document.getElementById('item-list');
        if (!listContainer) return;

        listContainer.innerHTML = '';

        data.forEach(item => {
            const card = document.createElement('div');
            
            // 2列になったので padding を少し小さく (p-2) してスッキリさせた
            card.className = "border border-gray-200 rounded-xl shadow-sm bg-white hover:shadow-lg transition-all duration-300 overflow-hidden relative group flex flex-col";

            // ★ボタンのクラス解説
            // opacity-100 translate-y-0 : スマホでは「不透明」「元の位置」（＝常に見える）
            // md:opacity-0 md:translate-y-10 : PC(md)では「透明」「下に隠す」
            // md:group-hover:... : PCでホバーした時だけ表示
            
            card.innerHTML = `
                <div class="relative w-full h-32 md:h-48 overflow-hidden bg-gray-100">
                    <a href="/items/detail?id=${item.id}" class="block w-full h-full">
                        <img src="${item.imageUrl}" alt="${item.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105">
                    </a>
                    
                    <button onclick="openModal('カートに追加', '${item.name} をカートに入れますか？', () => showToast('追加しました！', 'success'))" 
                            class="absolute bottom-2 right-2 w-8 h-8 md:w-10 md:h-10 bg-green-500 rounded-full flex items-center justify-center shadow-lg z-10 
                                   transition-all duration-300 active:scale-95
                                   opacity-100 translate-y-0 
                                   md:opacity-0 md:translate-y-10 md:group-hover:translate-y-0 md:group-hover:opacity-100">
                        <span class="text-white text-lg md:text-xl font-bold">＋</span>
                    </button>
                </div>

                <div class="p-2 md:p-3 flex flex-col flex-grow justify-between">
                    <div>
                        <h3 class="font-bold text-gray-800 text-xs md:text-sm line-clamp-2 mb-1 leading-tight">${item.name}</h3>
                        <p class="text-[10px] md:text-xs text-gray-500 mb-1 truncate">出品: ${item.producer}</p>
                    </div>
                    <div class="flex justify-between items-end mt-1">
                        <p class="font-bold text-green-600 text-sm md:text-lg">¥${item.price}</p>
                    </div>
                </div>
            `;

            listContainer.appendChild(card);
        });

        console.log("5. 表示完了！");

    } catch (error) {
        console.error("エラー発生:", error);
        showToast('データの読み込みに失敗しました', 'error');
    }
}

// ==========================================
//  5. 初期化処理
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    const itemList = document.getElementById('item-list');
    if (itemList) {
        fetchItems();
    }
});