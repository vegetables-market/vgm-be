document.addEventListener('DOMContentLoaded', async () => {
    // 1. URLから ID を取得する (?id=1 の "1" を取る)
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');

    if (!id) {
        alert("商品IDが見つかりません");
        return;
    }

    try {
        // 2. サーバーからその商品のデータだけをもらう
        const response = await fetch(`/api/items/${id}`);
        if (!response.ok) throw new Error("データが見つかりません");

        const item = await response.json();

        // 3. 画面を書き換える
        document.getElementById('detail-image').src = item.imageUrl;
        document.getElementById('detail-name').innerText = item.name;
        document.getElementById('detail-price').innerText = `¥${item.price}`; // 円マークをつける
        document.getElementById('detail-producer').innerText = item.producer;
        document.getElementById('detail-description').innerText = item.description;

        // ページタイトルも書き換えちゃう（ブラウザのタブ名）
        document.title = `${item.name} - GrandMarket`;

    } catch (error) {
        console.error(error);
        document.getElementById('detail-name').innerText = "商品が見つかりませんでした 😢";
    }
});