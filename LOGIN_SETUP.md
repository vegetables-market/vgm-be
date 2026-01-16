# ログイン機能のセットアップと使い方

## 概要
vgm-feとvgm-beを連携させたログイン機能を実装しました。

## 実装内容

### バックエンド (vgm-be)
- **エンティティ**: `User.kt` - ユーザー情報を保持
- **リポジトリ**: `UserRepository.kt` - データベースアクセス
- **DTO**: `LoginRequest.kt`, `LoginResponse.kt` - リクエスト/レスポンス型
- **サービス**: `AuthService.kt` - ログインロジック
- **コントローラー**: `AuthController.kt` - `/api/auth/login` エンドポイント
- **設定**: `WebConfig.kt` - CORS設定
- **マイグレーション**: `V1__Create_users_table.sql` - usersテーブル作成とテストデータ投入

### フロントエンド (vgm-fe)
- **API関数**: `src/lib/api.ts` - ログインAPI呼び出し関数
- **ログインページ**: `src/app/login/page.tsx` - ログインフォーム

## セットアップ手順

### 前提条件
- Docker Desktopがインストールされ、起動していること
- Java 21がインストールされていること
- Node.js がインストールされていること

### 1. バックエンドの起動

```bash
cd vgm-be

# Docker Desktopを起動してから、データベースを起動
docker-compose up -d postgres

# データベースが起動するまで少し待つ（10秒程度）

# アプリケーションを起動
./gradlew bootRun
```

**重要**: `application.yml` (`vgm-be/src/main/resources/application.yml:6`) にはデータベース接続のデフォルト値が設定されています：
- URL: `jdbc:postgresql://localhost:5432/myapp`
- User: `postgres`
- Password: `postgres`

### 2. フロントエンドの起動

```bash
cd vgm-fe

# 依存関係のインストール（初回のみ）
npm install

# 開発サーバーを起動
npm run dev
```

## 使い方

1. ブラウザで `http://localhost:3000/login` にアクセス
2. テストユーザーでログイン:
   - ユーザー名: `admin`
   - パスワード: `password`

   または

   - ユーザー名: `testuser`
   - パスワード: `test123`

3. ログイン成功後、トップページにリダイレクトされます

## API エンドポイント

### ログイン
- **URL**: `POST /api/auth/login`
- **リクエストボディ**:
  ```json
  {
    "username": "admin",
    "password": "password"
  }
  ```
- **レスポンス（成功時）**:
  ```json
  {
    "success": true,
    "message": "ログインに成功しました",
    "userId": 1,
    "username": "admin"
  }
  ```
- **レスポンス（失敗時）**:
  ```json
  {
    "success": false,
    "message": "ユーザーが見つかりません"
  }
  ```

## データベーススキーマ

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 注意事項

1. **セキュリティ**: 現在のパスワードは平文で保存されています。本番環境では必ずBCryptなどでハッシュ化してください。
2. **セッション管理**: 現在はlocalStorageにユーザー情報を保存していますが、本番環境ではJWTやセッションCookieを使用してください。
3. **CORS設定**: `http://localhost:3000` からのリクエストを許可しています。

## トラブルシューティング

### データベース接続エラー
- Docker Composeが起動していることを確認
- `application.yml`の環境変数が正しく設定されていることを確認

### CORSエラー
- バックエンドの`WebConfig.kt`で正しいオリジンが設定されていることを確認
- ブラウザのキャッシュをクリア

### ログインページが表示されない
- フロントエンドが正しく起動していることを確認（`http://localhost:3000`）
- Next.jsのビルドエラーがないか確認
