# 取引機能詳細

本ドキュメントは、CtoCサイト「grand-market」におけるユーザー間の取引機能の詳細を定義します。

## 1. 機能一覧

- **商品購入機能**
  - ユーザーが出品されている商品を購入する機能。
  - 購入ボタンを押すと取引が開始される。
  - **エンティティ:** `Transaction` (仮)
    - `transactionId` (PK)
    - `productId` (FK)
    - `buyerUserId` (FK)
    - `sellerUserId` (FK)
    - `price` (購入時の価格)
    - `status` ('pending_payment', 'paid', 'shipped', 'completed', 'cancelled')
    - `createdAt`, `updatedAt`
  - **API:**
    - `POST /api/products/{productId}/purchase`: 商品の購入を開始し、取引を作成

- **決済機能**
  - 外部決済サービス（Stripe, PayPalなど）との連携を想定。
  - フェーズ1ではダミーの決済処理を実装し、取引ステータスを更新する。
  - **API:**
    - `POST /api/transactions/{transactionId}/payment`: 決済を実行（外部サービスへのリダイレクト等を想定）
    - `POST /api/webhooks/payment`: 決済サービスからのコールバックを受け取り、ステータスを 'paid' に更新

- **ユーザー間メッセージ機能**
  - 取引に関連して、購入者と出品者がコミュニケーションを取るための機能。
  - **エンティティ:** `Message` (仮)
    - `messageId` (PK)
    - `transactionId` (FK)
    - `senderUserId` (FK)
    - `receiverUserId` (FK)
    - `content` (テキスト)
    - `createdAt`
  - **API:**
    - `GET /api/transactions/{transactionId}/messages`: 取引のメッセージ一覧を取得
    - `POST /api/transactions/{transactionId}/messages`: メッセージを送信

- **取引管理**
  - ユーザーが自身の取引（購入・販売）状況を確認できる機能。
  - **API:**
    - `GET /api/users/me/transactions`: 自身の取引一覧を取得
    - `GET /api/transactions/{transactionId}`: 取引詳細を取得
    - `POST /api/transactions/{transactionId}/ship`: 出品者が発送を通知
    - `POST /api/transactions/{transactionId}/complete`: 購入者が受取を完了

## 2. 実装タスク（例）

1.  `Transaction`, `Message` エンティティとリポジトリを作成。
2.  商品購入API (`/api/products/{productId}/purchase`) を実装。
3.  ダミーの決済処理と取引ステータス更新ロジックを実装。
4.  メッセージ送受信APIとサービスを実装。
5.  フロントエンドで取引管理画面、メッセージ画面を作成。
