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
    // コンテナ取得（なければ何もしない）
    const container = document.getElementById('toast-container');
    if (!container) return;

    // トースト作成
    const toast = document.createElement('div');
    toast.innerText = message;
    // アニメーションクラス (animate-toast-in) は tailwind.config.cjs で定義済みと想定
    toast.className = `${BASE_TOAST_CLASSES} ${TOAST_TYPES[type]} animate-toast-in`;

    container.appendChild(toast);

    // 指定時間後に消す
    setTimeout(() => {
        // 消えるアニメーション (animate-toast-out) に切り替え
        toast.classList.remove('animate-toast-in');
        toast.classList.add('animate-toast-out');

        // アニメーション完了後にDOMから削除
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

    // テキストセット
    titleEl.innerText = title;
    msgEl.innerText = message;

    // ボタンのイベントをリセット（連打防止のためのクローン）
    const newBtn = actionBtn.cloneNode(true);
    actionBtn.parentNode.replaceChild(newBtn, actionBtn);

    newBtn.onclick = () => {
        if (onConfirm) onConfirm();
        closeModal();
    };

    // 表示処理
    overlay.classList.remove('hidden');
    
    // ポンッ！というアニメーションを毎回再生させるリセット技
    content.classList.remove('modal-pop');
    void content.offsetWidth; // 強制リフロー
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
        // APIからデータ取得
        const response = await fetch('/api/items');
        console.log("2. 通信結果:", response.status);

        if (!response.ok) {
            throw new Error(`HTTPエラー: ${response.status}`);
        }

        const data = await response.json();
        console.log("3. データ受け取ったよ:", data);

        // 表示場所の確保
        const listContainer = document.getElementById('item-list');
        if (!listContainer) {
            console.error("4. エラー: item-list (表示する箱) が見つからない！");
            return;
        }

        // 中身をクリア
        listContainer.innerHTML = '';

        // カード生成ループ
        data.forEach(item => {
            const card = document.createElement('div');
            // デザイン: 白背景、角丸、影、ホバーで浮き上がる
            card.className = "border border-gray-200 rounded-xl shadow-sm bg-white hover:shadow-lg transition-all duration-300 overflow-hidden relative group flex flex-col";

            card.innerHTML = `
                <div class="relative w-full h-48 overflow-hidden bg-gray-100">
                    <a href="/items/detail?id=${item.id}" class="block w-full h-full">
                        <img src="${item.imageUrl}" alt="${item.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105">
                    </a>
                    <button onclick="openModal('カートに追加', '${item.name} をカートに入れますか？', () => showToast('追加しました！', 'success'))" 
                            class="absolute bottom-2 right-2 w-10 h-10 bg-green-500 rounded-full flex items-center justify-center shadow-lg translate-y-10 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-300 hover:scale-110 active:scale-95 z-10">
                        <span class="text-white text-xl font-bold">＋</span>
                    </button>
                </div>

                <div class="p-3 flex flex-col flex-grow justify-between">
                    <div>
                        <h3 class="font-bold text-gray-800 text-sm line-clamp-2 mb-1">${item.name}</h3>
                        <p class="text-xs text-gray-500 mb-1">出品: ${item.producer}</p>
                    </div>
                    <div class="flex justify-between items-end mt-2">
                        <p class="font-bold text-green-600 text-lg">¥${item.price}</p>
                    </div>
                </div>
            `;

            listContainer.appendChild(card);
        });

        console.log("5. 表示完了！ (DOMに追加しました)");
        // 成功トースト
        // showToast('データを読み込みました！', 'success'); 

    } catch (error) {
        console.error("★エラー発生:", error);
        showToast('データの読み込みに失敗しました', 'error');
    }
}

// ==========================================
//  5. 初期化処理 (ページが開いた時)
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    console.log("0. ページ読み込み完了！JS起動");

    const itemList = document.getElementById('item-list');
    if (itemList) {
        console.log("item-list (箱) を発見！ -> データを読みに行きます");
        fetchItems();
    } else {
        console.log("item-list (箱) がないため、データ読み込みをスキップします (詳細ページなど)");
    }
});