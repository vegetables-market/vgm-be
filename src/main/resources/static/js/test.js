// test.js

// 1. タイプとTailwindクラスの対応表を作る
const TOAST_TYPES = {
  success: 'bg-green-500 text-white',
  error: 'bg-red-500 text-white',
  warning: 'bg-yellow-400 text-black', // 黄色は文字黒
  info: 'bg-blue-500 text-white',
};

// 2. 全トースト共通の基本スタイル
const BASE_CLASSES = 'p-4 rounded-lg shadow-lg min-w-[250px]';

/**
 * トーストを表示する関数
 * @param {string} message - 表示するメッセージ
 * @param {'success'|'error'|'warning'|'info'} type - トーストの種類
 * @param {number} [duration=5000] - トーストが表示される時間 (ミリ秒)
 */
function showToast(message, type, duration = 5000) {
  const container = document.getElementById('toast-container');

  // 3. トースト要素を作成
  const toast = document.createElement('div');

  // 4. メッセージを設定
  toast.innerText = message;

  // 5. Tailwindのクラスをまとめて設定 (基本 + 色 + 表示アニメーション)
  // (classNameで一括設定する)
  toast.className = `${BASE_CLASSES} ${TOAST_TYPES[type]} animate-toast-in`;
  
  // 6. コンテナに追加
  container.appendChild(toast);

  // 7. 指定時間後に消す処理
  
  // 7a. フェードアウト開始までの時間 (0.5秒はアニメーション時間)
  const fadeOutDelay = duration - 500; 

  setTimeout(() => {
    // 7b. フェードアウト開始 (表示アニメーションを消して、非表示アニメーションに切り替え)
    toast.classList.remove('animate-toast-in');
    toast.classList.add('animate-toast-out');

    // 7c. フェードアウトが終わったらDOMから削除 (0.5秒後)
    setTimeout(() => {
      toast.remove();
    }, 500); // animate-toast-outの時間

  }, fadeOutDelay);
}

async function fetchItems() {
    try {
        // 1. サーバーのAPI (/api/items) にアクセスしてデータを取る
        const response = await fetch('/api/items');
        const data = await response.json(); // JSONとして読み込む

        // 2. 表示する場所 (div) を取得
        const listContainer = document.getElementById('item-list');
        
        // 3. 中身を一旦リセット (空にする)
        listContainer.innerHTML = '';

        // ...
        data.forEach(item => {
            const card = document.createElement('div');
            // overflow-hidden を追加して、画像が角丸からはみ出さないようにする
            card.className = "border rounded-lg shadow-sm bg-white hover:shadow-md transition-shadow overflow-hidden";

            card.innerHTML = `
                <img src="${item.imageUrl}" alt="${item.name}" class="w-full h-48 object-cover bg-gray-100">

                <div class="p-4">
                    <div class="flex justify-between items-start mb-2">
                        <h3 class="font-bold text-lg text-green-800">${item.name}</h3>
                        <p class="font-bold text-xl text-gray-900">¥${item.price}</p>
                    </div>
                    
                    <p class="text-sm text-gray-500 mb-2">
                        出品者: <span class="font-semibold text-gray-700">${item.producer}</span>
                    </p>
                    
                    <p class="text-gray-600 text-sm bg-gray-50 p-2 rounded mb-3">
                        ${item.description}
                    </p>
                    
                    <button class="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600 text-sm font-bold">
                        カートに入れる
                    </button>
                </div>
            `;

            listContainer.appendChild(card);
        });
        
        // 成功したらトースト出す？
        showToast('データを読み込みました！', 'success');

    } catch (error) {
        console.error('エラー:', error);
        showToast('データの読み込みに失敗しました', 'error');
    }
}