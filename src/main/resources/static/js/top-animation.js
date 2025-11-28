gsap.registerPlugin(ScrollTrigger, CustomEase);
CustomEase.create("customEase", "M0,0 C0.86,0 0.07,1 1,1");

let lenis;
let autoScrollTimer; 
let lastInteractionTime = Date.now(); // 最後に操作した時間
let isAutoScrolling = false; // 現在自動スクロール中かどうか

// ==============================================
//  1. 初期化 & 画像プリロード (読み込み待ち)
// ==============================================
document.addEventListener("DOMContentLoaded", () => {
    initLenis();
    const overlay = document.getElementById('loading-overlay');
    
    // 画像の読み込み状況を監視
    const images = document.querySelectorAll('.background-image');
    let loadedCount = 0;
    const totalImages = images.length;

    // アプリ開始関数
    const startApp = () => {
        // F5リロードかどうかの判定
        const navEntries = performance.getEntriesByType("navigation");
        const isReload = navEntries.length > 0 && navEntries[0].type === 'reload';
        
        // 訪問済みフラグ
        const isVisited = sessionStorage.getItem('harvest_visited');

        if (!isReload && isVisited) {
            // 2回目以降(戻るボタンなど): 即表示
            overlay.style.display = 'none';
            initScrollAnimation();
            startAutoScrollLoop();
        } else {
            // 初回 または F5リロード: ロード演出あり
            sessionStorage.setItem('harvest_visited', 'true');
            
            // 読み込み完了後、少し余韻を持たせて幕を開ける
            gsap.to(overlay, {
                y: "-100%", duration: 1, ease: "power3.inOut", delay: 0.5,
                onComplete: () => { 
                    overlay.style.display = 'none'; 
                    initScrollAnimation();
                    startAutoScrollLoop();
                }
            });
        }
    };

    // 画像がない場合の安全策
    if (totalImages === 0) {
        startApp();
    } else {
        // 全画像の読み込み完了を待つ
        images.forEach(img => {
            if (img.complete) {
                loadedCount++;
                if (loadedCount === totalImages) startApp();
            } else {
                img.onload = () => {
                    loadedCount++;
                    if (loadedCount === totalImages) startApp();
                };
                img.onerror = () => {
                    loadedCount++;
                    if (loadedCount === totalImages) startApp();
                };
            }
        });
    }
});

// ==============================================
//  2. スムーズスクロール (Lenis)
// ==============================================
function initLenis() {
    lenis = new Lenis({ duration: 1.2, smooth: true });
    lenis.on("scroll", ScrollTrigger.update);
    gsap.ticker.add((time) => lenis.raf(time * 1000));
    gsap.ticker.lagSmoothing(0);
}

// ==============================================
//  3. 自動スクロール管理 (不死鳥モード)
// ==============================================
function startAutoScrollLoop() {
    console.log("Auto scroll system init...");

    // ユーザー操作があったら時間を更新（延期）
    const updateInteraction = () => {
        lastInteractionTime = Date.now();
    };

    window.addEventListener('wheel', updateInteraction);
    window.addEventListener('touchmove', updateInteraction);
    window.addEventListener('click', updateInteraction);
    window.addEventListener('keydown', updateInteraction);

    // 1秒ごとにチェック
    setInterval(() => {
        const now = Date.now();
        const timeSinceLastInteraction = now - lastInteractionTime;
        
        // 最後の操作から4秒経過していて、かつ現在スクロール中でなければ発動
        if (timeSinceLastInteraction > 4000 && !isAutoScrolling) {
             triggerAutoScroll();
             lastInteractionTime = Date.now(); // 連発防止
        }
    }, 1000);
}

function triggerAutoScroll() {
    const fixedSection = document.querySelector('.fixed-section');
    if (!fixedSection) return;
    
    const totalHeight = fixedSection.offsetHeight - window.innerHeight;
    const currentScroll = window.scrollY;
    
    // 次のセクション位置計算
    const oneSectionHeight = totalHeight / 10;
    let nextScroll = currentScroll + oneSectionHeight;

    // ★最後から最初に戻る時の「ワープ」処理
    if (nextScroll > totalHeight - 10) { 
        console.log("Looping back to start...");
        isAutoScrolling = true;
        
        lenis.scrollTo(0, { 
            immediate: true, // アニメーションなしで一瞬で戻る
            lock: true,
            onComplete: () => {
                isAutoScrolling = false;
            }
        });
    } else {
        // 通常のスクロール
        console.log("Auto scrolling to:", nextScroll);
        isAutoScrolling = true;

        lenis.scrollTo(nextScroll, {
            duration: 1.5, 
            ease: "power2.inOut",
            lock: true,
            onComplete: () => {
                isAutoScrolling = false;
            }
        });
    }
}

// ==============================================
//  4. データ取得 & 一覧表示
// ==============================================
async function fetchItems(categoryName) {
    lastInteractionTime = Date.now(); // 操作とみなす
    console.log(`fetching items for: ${categoryName}`);
    
    const listContainer = document.getElementById('item-list');
    listContainer.innerHTML = '<div class="col-span-full text-center py-10 text-gray-400">Loading items...</div>';

    try {
        const response = await fetch('/api/items'); 
        if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
        const data = await response.json();
        
        listContainer.innerHTML = ''; 

        data.forEach(item => {
            const card = document.createElement('div');
            card.className = "group bg-white rounded-lg overflow-hidden border border-gray-100 hover:shadow-xl transition-all duration-300 cursor-pointer flex flex-col";
            card.innerHTML = `
                <div class="relative h-48 overflow-hidden bg-gray-100">
                    <a href="/items/detail?id=${item.id}" class="block w-full h-full">
                        <img src="${item.imageUrl || '/images/default.jpg'}" alt="${item.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105">
                    </a>
                    <button onclick="event.stopPropagation(); alert('カートに追加: ${item.name}');" 
                            class="absolute bottom-2 right-2 w-10 h-10 bg-green-500 rounded-full flex items-center justify-center shadow-lg z-10 
                                   transition-all duration-300 active:scale-95 opacity-0 translate-y-4 group-hover:opacity-100 group-hover:translate-y-0">
                        <span class="text-white text-xl font-bold">＋</span>
                    </button>
                </div>
                <div class="p-4 flex flex-col flex-grow">
                    <h3 class="font-bold text-gray-800 text-sm mb-1 line-clamp-2">${item.name}</h3>
                    <p class="text-xs text-gray-500 mb-2">出品: ${item.producer || '農家さん'}</p>
                    <p class="text-green-600 font-bold text-lg mt-auto">¥${item.price}</p>
                </div>
            `;
            listContainer.appendChild(card);
        });
    } catch (error) {
        console.error("Error fetching items:", error);
        listContainer.innerHTML = '<div class="col-span-full text-center py-10 text-red-400">データの読み込みに失敗しました</div>';
    }
}

// ==============================================
//  5. モーダル制御
// ==============================================
const modal = document.getElementById('product-modal');
const modalContent = document.getElementById('modal-content');
const closeModal = document.getElementById('close-modal');
const modalTitle = document.getElementById('modal-category-title');
const menuItems = document.querySelectorAll('.artist');

menuItems.forEach(item => {
    item.addEventListener('click', () => {
        lastInteractionTime = Date.now();
        const category = item.innerText;
        modalTitle.innerText = category;
        modal.classList.remove('invisible', 'opacity-0');
        modalContent.classList.remove('translate-y-10');
        lenis.stop();
        fetchItems(category);
    });
});

const hideModal = () => {
    lastInteractionTime = Date.now();
    modal.classList.add('invisible', 'opacity-0');
    modalContent.classList.add('translate-y-10');
    lenis.start();
};

closeModal.addEventListener('click', hideModal);
modal.addEventListener('click', (e) => {
    if(e.target === modal) hideModal();
});

// ==============================================
//  6. スクロール連動アニメーション
// ==============================================
function initScrollAnimation() {
    const totalSections = 10;
    const progressFill = document.getElementById("progress-fill");
    const backgrounds = document.querySelectorAll(".background-image");
    const featuredContents = document.querySelectorAll(".featured-content");
    let currentSection = 0;

    ScrollTrigger.create({
        trigger: ".fixed-section", start: "top top", end: "bottom bottom", scrub: 0,
        onUpdate: (self) => {
            const progress = self.progress;
            progressFill.style.width = `${progress * 100}%`;
            const newSection = Math.min(Math.floor(progress * totalSections), totalSections - 1);
            if (newSection !== currentSection) {
                changeSection(newSection);
                currentSection = newSection;
            }
        }
    });

    function changeSection(index) {
        backgrounds.forEach((bg, i) => {
            if (i === index) {
                gsap.to(bg, { opacity: 1, duration: 0.5 });
                bg.classList.add('active');
            } else {
                gsap.to(bg, { opacity: 0, duration: 0.5 });
                bg.classList.remove('active');
            }
        });
        featuredContents.forEach((fc, i) => {
            if (i === index) {
                gsap.fromTo(fc, { opacity: 0, y: 20, visibility: 'hidden' }, { opacity: 1, y: 0, visibility: 'visible', duration: 0.5 });
            } else {
                gsap.to(fc, { opacity: 0, y: -20, duration: 0.5, visibility: 'hidden' });
            }
        });
    }
}