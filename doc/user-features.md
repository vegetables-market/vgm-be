# ユーザー機能詳細

本ドキュメントは、CtoCサイト「grand-market」におけるユーザー関連機能の詳細を定義します。
認証に関する基本的な設計は `auth-plan.md` を参照してください。

## 1. 機能一覧

- **ユーザー登録 / ログイン**
  - `auth-plan.md` に基づき、メールアドレス・パスワードによる基本認証を実装。
  - OAuth、二段階認証(TOTP)も同計画に基づき実装。

- **プロフィール管理**
  - **エンティティ:** `UserProfile` (仮)
    - `userId` (FK)
    - `nickname` (文字列)
    - `avatarUrl` (文字列)
    - `introduction` (テキスト)
  - **API:**
    - `GET /api/users/{userId}/profile`: プロフィール表示
    - `PUT /api/users/me/profile`: 自身のプロフィール更新

- **ダッシュボード（マイページ）**
  - ログインユーザー専用ページ。
  - 出品中の商品、購入した商品、お知らせなどを一覧表示。

- **評価システム**
  - 取引完了後、購入者と出品者が相互に評価できる機能。
  - **エンティティ:** `UserRating` (仮)
    - `transactionId` (FK)
    - `raterUserId` (FK)
    - `rateeUserId` (FK)
    - `rating` (5段階評価など)
    - `comment` (テキスト)
  - **API:**
    - `POST /api/transactions/{transactionId}/ratings`: 評価を投稿
    - `GET /api/users/{userId}/ratings`: ユーザーの評価一覧を取得

## 2. 画面遷移（案）

- `トップページ` -> `ログイン` -> `ダッシュボード`
- `商品詳細ページ` -> `出品者プロフィールページ`
- `ダッシュボード` -> `プロフィール編集ページ`

## 3. 実装タスク（例）

1.  `UserProfile` エンティティとリポジトリの作成。
2.  プロフィール取得・更新API (`/api/users/...`) のコントローラーとサービスを作成。
3.  フロントエンドでプロフィール表示・編集画面を作成。
4.  `UserRating` エンティティと関連APIを作成。
