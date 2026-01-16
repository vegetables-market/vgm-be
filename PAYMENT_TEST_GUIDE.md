# 決済機能テストページ 使い方ガイド

## アクセス方法

1. ログイン後、プロフィールページ (`/profile`) にアクセス
2. 「決済機能テストページ」ボタンをクリック
3. `/payment-test` ページが開きます

## 機能概要

決済テストページでは以下の機能をテストできます：

### 1. 商品一覧タブ
- 販売中の商品を一覧表示
- 各商品から「購入テスト」ボタンで購入可能
- 自分の出品した商品は購入できません

### 2. 商品作成タブ
- 新規商品を出品できます
- 入力項目：
  - 商品名（必須）
  - 説明
  - 価格（円）（必須）
  - カテゴリー

### 3. 決済テストタブ
- 購入フローと決済処理をテストできます
- エスクロー解除（出品者への入金）をテストできます

## 使い方（完全な購入フロー）

### ステップ1: テスト用商品を作成

1. 「商品作成」タブを開く
2. 商品情報を入力:
   ```
   商品名: テスト商品
   説明: これはテスト用の商品です
   価格: 5000
   カテゴリー: テスト
   ```
3. 「商品を作成」ボタンをクリック
4. 成功メッセージが表示されます

### ステップ2: 別のユーザーで購入テスト

**重要:** 自分の商品は購入できないため、別のユーザーでログインしてください。

1. 現在のユーザーをログアウト
2. 別のアカウントでログイン（または新規登録）
3. プロフィールページから「決済機能テストページ」にアクセス
4. 「商品一覧」タブで先ほど作成した商品を確認
5. 「購入テスト」ボタンをクリック

### ステップ3: 決済フローの確認

購入テストをクリックすると、以下の処理が自動実行されます：

1. **注文作成**
   - 注文IDが発行されます
   - 商品が「予約済み」ステータスに変更されます

2. **決済開始**
   - Stripe決済の場合: `Client Secret` が表示されます
   - PayPay決済の場合: 決済URLが表示されます

**表示例:**
```
✅ 注文作成成功 (注文ID: 123)
✅ Stripe決済開始成功
Client Secret: pi_xxx_secret_xxx

⚠️ 実際の決済にはStripe Elementsの統合が必要です
```

3. 表示された**注文ID**をメモしておきます

### ステップ4: エスクロー解除（出品者への入金）

商品を受け取ったと仮定して、出品者に入金します。

1. 「決済テスト」タブを開く
2. 「注文ID」フィールドにステップ3でメモした注文IDを入力
3. 「エスクロー解除（出品者へ入金）」ボタンをクリック
4. 成功すると以下のメッセージが表示されます:
   ```
   ✅ 出品者への入金が完了しました
   ```

## 金額の計算

決済テストページでは、以下のように金額が計算されます：

- **総額**: 商品の価格
- **プラットフォーム手数料**: 総額 × 10%（デフォルト）
- **出品者入金額**: 総額 - プラットフォーム手数料

**例: 商品価格が5,000円の場合**
```
総額: ¥5,000
プラットフォーム手数料: ¥500 (10%)
出品者入金額: ¥4,500
```

## 取引ステータスの遷移

### 商品ステータス
1. `AVAILABLE` - 販売中
2. `RESERVED` - 予約済み（注文作成時）
3. `SOLD` - 売却済み（エスクロー解除時）

### 注文ステータス
1. `PENDING_PAYMENT` - 支払い待ち（注文作成時）
2. `PAID` - 支払い完了（決済完了時）
3. `SHIPPED` - 発送済み（※手動更新が必要）
4. `DELIVERED` - 配送完了（※手動更新が必要）
5. `COMPLETED` - 取引完了（エスクロー解除時）

### トランザクションステータス
1. `ESCROWED` - エスクロー中（資金保留）
2. `COMPLETED` - 完了（出品者へ入金）

## 現在の制限事項

### 実装済み機能
- ✅ 商品作成・一覧表示
- ✅ 注文作成
- ✅ 決済開始（Stripe/PayPay）
- ✅ エスクロー管理
- ✅ 手数料計算

### 未実装・制限あり
- ❌ 実際のStripe決済フォーム（Stripe Elementsの統合が必要）
- ❌ PayPay決済の実装（法人契約が必要）
- ❌ 商品画像アップロード
- ❌ 配送追跡
- ❌ メッセージング機能
- ❌ 商品検索・フィルタリング

## Stripe決済の実装方法（今後）

実際のクレジットカード決済を実装するには、以下が必要です：

### 1. Stripe APIキーの設定

```bash
# バックエンド（application.yml）
export STRIPE_API_KEY=sk_test_your_stripe_secret_key
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
```

### 2. フロントエンドでStripe Elementsを統合

```bash
npm install @stripe/stripe-js @stripe/react-stripe-js
```

```tsx
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';

const stripePromise = loadStripe('pk_test_your_publishable_key');

// 決済フォームコンポーネント
function PaymentForm({ clientSecret }: { clientSecret: string }) {
  const stripe = useStripe();
  const elements = useElements();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) return;

    const result = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: elements.getElement(CardElement)!,
      },
    });

    if (result.error) {
      console.error(result.error.message);
    } else {
      // 決済成功
      console.log('Payment successful!', result.paymentIntent);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <CardElement />
      <button type="submit" disabled={!stripe}>
        支払う
      </button>
    </form>
  );
}
```

### 3. Webhookエンドポイントの実装

Stripeからの決済イベントを受け取るため、`PaymentController.kt` の `handleStripeWebhook` メソッドを実装してください。

## PayPay決済の実装方法（今後）

PayPay決済を実装するには：

1. [PayPay for Developers](https://developer.paypay.ne.jp/) で法人登録
2. API認証情報を取得
3. `PayPayPaymentService.kt` のTODOコメント部分を実装
4. QRコード決済フローを統合

## トラブルシューティング

### 商品が表示されない
- データベースに商品が登録されているか確認
- バックエンドのログを確認（`/api/products` エンドポイント）

### 購入テストが失敗する
- 自分の商品を購入しようとしていないか確認
- 商品のステータスが `AVAILABLE` になっているか確認
- ブラウザのコンソールでエラーメッセージを確認

### エスクロー解除が失敗する
- 正しい注文IDを入力しているか確認
- 注文のステータスが `PAID` になっているか確認
- トランザクションが `ESCROWED` ステータスになっているか確認

### APIエラーが発生する
- バックエンドが起動しているか確認（`http://localhost:8081`）
- データベース（PostgreSQL）が起動しているか確認
- ブラウザのコンソールとバックエンドのログを確認

## データベース確認方法

PostgreSQLに接続して、テーブルの状態を確認できます：

```sql
-- 商品一覧
SELECT * FROM products;

-- 注文一覧
SELECT * FROM orders;

-- トランザクション一覧
SELECT * FROM transactions;

-- 決済一覧
SELECT * FROM payments;
```

## まとめ

このテストページでは、メルカリのようなCtoCマーケットプレイスの決済フローの基本的な部分をテストできます。

実際の運用には、以下の追加実装が必要です：
- Stripe Elementsによる決済フォーム
- Webhook処理の完全実装
- 商品画像アップロード
- メッセージング機能
- 配送追跡
- レビュー・評価機能

詳細は `PAYMENT_SYSTEM_README.md` を参照してください。
