# VGM決済システム実装ガイド

メルカリのようなCtoCマーケットプレイスに必要な決済機能を実装しました。

## 実装された機能

### 1. 複数決済手段対応
- **Stripe**: クレジットカード決済、Apple Pay、Google Pay、コンビニ決済
- **PayPay**: PayPayアプリ決済

### 2. エスクロー機能
メルカリと同様に、買い手の支払いを一時的に保留し、商品受取確認後に出品者へ入金する仕組みを実装しました。

**取引フロー:**
1. 買い手が商品を購入して決済
2. 資金がエスクロー状態で保留される
3. 出品者が商品を発送
4. 買い手が商品を受け取り確認
5. エスクローが解除され、出品者に入金

### 3. 主要エンティティ

#### Product（商品）
- 商品情報（名前、説明、価格、画像）
- 出品者情報
- ステータス（販売中、予約済み、売却済み、削除済み）

#### Order（注文）
- 買い手・売り手情報
- 商品情報
- 金額（総額、プラットフォーム手数料、出品者入金額）
- ステータス（支払い待ち、支払い完了、発送済み、配送完了、取引完了）
- 配送先情報

#### Transaction（取引）
- 注文に紐づく取引情報
- エスクロー状態管理
- タイプ（支払い、入金、返金）

#### Payment（決済）
- 決済方法（クレジットカード、PayPay）
- 決済プロバイダー情報（Stripe Payment Intent ID等）
- ステータス（処理待ち、完了、失敗、返金済み）

## API エンドポイント

### 決済関連

#### POST /api/payment/create
決済を開始します。

**リクエスト:**
```json
{
  "orderId": 1,
  "userId": 123,
  "paymentMethod": "CREDIT_CARD",
  "amount": 5000.00
}
```

**レスポンス（Stripe）:**
```json
{
  "success": true,
  "message": "Stripe決済を開始しました",
  "clientSecret": "pi_xxx_secret_xxx"
}
```

**レスポンス（PayPay）:**
```json
{
  "success": true,
  "message": "PayPay決済を開始しました",
  "paypayUrl": "https://paypay.ne.jp/...",
  "paypayDeeplink": "paypay://payment?code=xxx"
}
```

#### POST /api/payment/confirm
Stripe決済を確認してエスクロー状態にします。

**リクエスト:**
```json
{
  "paymentIntentId": "pi_xxx"
}
```

**レスポンス:**
```json
{
  "success": true,
  "message": "決済が完了しました（エスクロー状態）",
  "paymentId": 456,
  "status": "COMPLETED"
}
```

#### POST /api/payment/release-escrow
商品受取確認後、エスクローを解除して出品者に入金します。

**リクエスト:**
```json
{
  "orderId": 1
}
```

**レスポンス:**
```json
{
  "success": true,
  "message": "出品者への入金が完了しました",
  "transactionId": 789
}
```

#### POST /api/payment/refund
返金処理を実行します。

**リクエスト:**
```json
{
  "orderId": 1,
  "reason": "商品が破損していた"
}
```

**レスポンス:**
```json
{
  "success": true,
  "message": "返金が完了しました",
  "refundId": "pi_xxx"
}
```

#### GET /api/payment/{paymentId}/status
決済ステータスを取得します。

**レスポンス:**
```json
{
  "success": true,
  "paymentId": 456,
  "status": "COMPLETED",
  "amount": 5000.00,
  "paymentMethod": "CREDIT_CARD",
  "createdAt": "2025-01-15T10:30:00",
  "completedAt": "2025-01-15T10:30:15"
}
```

## セットアップ手順

### 1. Stripe設定

1. [Stripe Dashboard](https://dashboard.stripe.com/) でアカウント作成
2. APIキーを取得（テスト環境: `sk_test_...`）
3. 環境変数を設定:

```bash
export STRIPE_API_KEY=sk_test_your_stripe_secret_key_here
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
```

### 2. PayPay設定

1. [PayPay for Developers](https://developer.paypay.ne.jp/) で法人登録
2. API認証情報を取得
3. 環境変数を設定:

```bash
export PAYPAY_API_KEY=a_iwJP24FrNC_FVKU
export PAYPAY_API_SECRET=your_paypay_api_secret_here
export PAYPAY_MERCHANT_ID=your_merchant_id_here
```

### 3. データベースマイグレーション

```bash
./gradlew clean build
./gradlew bootRun
```

Flywayが自動的に以下のテーブルを作成します:
- `products` - 商品
- `orders` - 注文
- `transactions` - 取引
- `payments` - 決済

### 4. プラットフォーム手数料設定

デフォルトは10%です。変更する場合:

```bash
export PLATFORM_FEE_RATE=0.10  # 10%
```

## エスクローの仕組み

### Stripe エスクロー実装

Stripeでは**手動キャプチャ（Manual Capture）**を使用してエスクローを実装しています。

1. **Payment Intent作成時に `capture_method: manual` を指定**
   - 資金を確保（authorize）するが、即座に決済確定（capture）しない

2. **買い手が商品受取確認後に `capture()` を実行**
   - この時点で初めて資金が確定し、出品者への入金が可能になる

3. **問題がある場合は `cancel()` で返金**
   - キャプチャ前であれば簡単にキャンセル可能

**コード例（StripePaymentService.kt）:**
```kotlin
// Payment Intent作成（エスクロー開始）
val params = PaymentIntentCreateParams.builder()
    .setAmount(amount)
    .setCurrency("jpy")
    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL) // 手動キャプチャ
    .build()

// 商品受取確認後にキャプチャ（エスクロー解除）
paymentIntent.capture()
```

### PayPay エスクロー実装

PayPayの公式APIでは、決済完了後にWebhookでステータスを受け取り、手動で入金処理を実行します。

## セキュリティ対策

### 1. Webhook署名検証
Stripeからのwebhookリクエストは署名検証を実装してください。

```kotlin
// PaymentController.kt の handleStripeWebhook メソッド内
val event = Webhook.constructEvent(
    payload,
    signature,
    webhookSecret
)
```

### 2. 二要素認証（TOTP）
既に実装済みの二要素認証を決済時にも適用することを推奨します。

### 3. レート制限
決済APIに対するレート制限を実装してください。

### 4. 金額検証
フロントエンドから送られた金額を必ずバックエンドで再計算・検証してください。

## 追加実装が必要な機能

### 1. Stripe Connect（出品者への直接入金）
現在の実装では、エスクロー解除時に出品者アカウントへの入金処理がTODOになっています。
実際の入金には [Stripe Connect](https://stripe.com/docs/connect) の設定が必要です。

**手順:**
1. 出品者にStripe Connectアカウントを作成してもらう
2. アカウントIDをUserテーブルに保存
3. `Transfer API` または `Destination Charge` で入金

### 2. PayPay API実装
PayPayの公式APIは法人契約が必要です。`PayPayPaymentService.kt` はインターフェースのみ実装しています。

実際の統合には以下が必要:
- PayPay for Developersでの法人登録
- REST APIまたはSDKの統合
- QRコード決済フローの実装
- Webhook処理

### 3. Webhook処理の完全実装
`PaymentController.kt` の `handleStripeWebhook` と `handlePayPayWebhook` を完全に実装してください。

### 4. 商品管理機能
現在、Productエンティティとリポジトリのみ実装済みです。
以下の機能が必要です:
- 商品CRUD API
- 商品検索・フィルタリング
- 商品画像アップロード

### 5. 注文管理機能
Orderエンティティとリポジトリのみ実装済みです。
以下の機能が必要です:
- 注文作成API
- 注文ステータス更新
- 発送・配送追跡

## データベーススキーマ

マイグレーションファイル: `V3__Create_marketplace_tables.sql`

```sql
products (商品)
├── id
├── name
├── description
├── price
├── seller_id → users(id)
├── status
└── created_at

orders (注文)
├── id
├── buyer_id → users(id)
├── seller_id → users(id)
├── product_id → products(id)
├── total_amount
├── platform_fee
├── seller_amount
├── status
└── shipping_*

transactions (取引)
├── id
├── order_id → orders(id)
├── type
├── amount
├── status
└── escrow_*

payments (決済)
├── id
├── order_id → orders(id)
├── user_id → users(id)
├── payment_method
├── amount
├── status
└── external_payment_id
```

## ステータス遷移図

### Order Status
```
PENDING_PAYMENT → PAID → SHIPPED → DELIVERED → COMPLETED
                   ↓
                CANCELLED ← ← ← ← ← ← ← ← ← ←
                   ↓
                REFUNDED
```

### Transaction Status
```
PENDING → PROCESSING → ESCROWED → COMPLETED
                          ↓
                       REFUNDED
```

### Payment Status
```
PENDING → PROCESSING → COMPLETED
                          ↓
                       REFUNDED
```

## テスト

### Stripeテストカード

**成功する決済:**
- カード番号: `4242 4242 4242 4242`
- 有効期限: 任意の未来の日付
- CVC: 任意の3桁

**3Dセキュア認証:**
- カード番号: `4000 0025 0000 3155`

[その他のテストカード](https://stripe.com/docs/testing)

### PayPayテスト環境

PayPay Sandboxを使用してテストしてください。

## トラブルシューティング

### Stripe APIエラー
- APIキーが正しく設定されているか確認
- テスト環境キー（`sk_test_`）を使用しているか確認

### データベースマイグレーションエラー
- PostgreSQLが起動しているか確認
- `flyway.baseline-on-migrate=true` が設定されているか確認

### 決済が完了しない
- Webhookエンドポイントが公開されているか確認
- Stripe DashboardでWebhookイベントを確認

## 参考リンク

- [Stripe API Documentation](https://stripe.com/docs/api)
- [Stripe Connect Documentation](https://stripe.com/docs/connect)
- [PayPay for Developers](https://developer.paypay.ne.jp/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## ライセンス

このプロジェクトは個人開発用です。
