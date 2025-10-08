# 商品出品・一覧機能詳細

本ドキュメントは、CtoCサイト「grand-market」における商品関連機能の詳細を定義します。

## 1. 機能一覧

- **商品出品機能**
  - ユーザーが商品を販売するために情報を登録する機能。
  - **エンティティ:** `Product` (仮)
    - `productId` (PK)
    - `sellerUserId` (FK)
    - `name` (文字列)
    - `description` (テキスト)
    - `price` (数値)
    - `categoryId` (FK)
    - `condition` (新品、中古など)
    - `status` (出品中、売り切れなど)
    - `createdAt`, `updatedAt`
  - **API:**
    - `POST /api/products`: 新規商品を出品
    - `PUT /api/products/{productId}`: 商品情報を更新
    - `DELETE /api/products/{productId}`: 商品を削除

- **商品画像アップロード**
  - 1商品につき複数枚の画像を登録可能にする。
  - 画像はファイルストレージ（S3など）に保存し、URLをDBに保持する。
  - **エンティティ:** `ProductImage` (仮)
    - `imageId` (PK)
    - `productId` (FK)
    - `imageUrl` (文字列)
    - `displayOrder` (数値)

- **商品一覧・検索機能**
  - トップページやカテゴリページで商品を一覧表示する。
  - キーワード、カテゴリ、価格帯などで商品を検索できる機能。
  - **API:**
    - `GET /api/products`: 商品一覧を取得（ページネーション、ソート対応）
    - `GET /api/products/search`: 商品を検索

- **商品詳細表示**
  - 商品の個別ページ。
  - 商品情報、出品者情報、関連商品などを表示。
  - **API:**
    - `GET /api/products/{productId}`: 商品詳細を取得

- **カテゴリ機能**
  - 商品を分類するためのカテゴリ。
  - **エンティティ:** `Category` (仮)
    - `categoryId` (PK)
    - `name` (文字列)
    - `parentCategoryId` (FK, 自己参照)

## 2. 実装タスク（例）

1.  `Product`, `ProductImage`, `Category` エンティティとリポジトリを作成。
2.  商品出品・更新・削除APIのコントローラーとサービスを作成。
3.  画像アップロード処理を実装（ストレージ連携）。
4.  商品一覧取得・検索APIを実装。
5.  フロントエンドで商品一覧ページ、詳細ページ、出品フォームを作成。
