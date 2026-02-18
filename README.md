# VGM Backend API

VGM (Video Game Market) のバックエンド API サーバーです。
Spring Boot と Kotlin で構築され、マイクロサービスライクな構成（将来的な拡張を見据えたドメイン分割）を採用しています。

## 🛠 技術スタック

| Category       | Technology                            | Version     |
| -------------- | ------------------------------------- | ----------- |
| **Language**   | Kotlin                                | 2.2.10      |
| **Framework**  | Spring Boot                           | 3.5.4       |
| **JDK**        | Java                                  | 21          |
| **Build Tool** | Gradle (Kotlin DSL)                   | 8.x         |
| **Database**   | PostgreSQL                            | 15 (Docker) |
| **Migration**  | Flyway                                | 11.7.2      |
| **Auth**       | Spring Security, JJWT, Firebase Admin | -           |
| **MFA**        | TOTP (dev.samstevens.totp)            | 1.7.1       |
| **API Docs**   | SpringDoc OpenAPI (Swagger)           | 2.8.5       |

## ✨ 主な機能

- **認証 (Authentication)**
  - メールアドレス/パスワード認証
  - OAuth2 連携 (Google, GitHub, Microsoft) via Firebase Authentication
  - 多要素認証 (MFA): TOTP (Authenticator App), Email Code
  - セッション管理 (Cookieベース + Redis/DB)

- **ユーザー管理 (User Management)**
  - プロフィール管理 (アバター, 自己紹介)
  - アカウント設定 (メールアドレス変更, パスワード変更)
  - ゲストユーザー機能 (データ統合)

- **マーケットプレイス (Marketplace)**
  - 商品管理 (出品, 編集, 削除, 下書き)
  - 商品検索・詳細表示
  - ショッピングカート機能
  - お気に入り (Wishlist) 機能

## 📂 プロジェクト構成

レイヤードアーキテクチャを採用し、機能（ドメイン）ごとにパッケージを分割しています。

```
com.example.myapp
├── config/             # Spring設定 (Security, Web, AppConfig etc.)
├── controller/         # API エンドポイント
│   ├── auth/           # 認証関連 (Login, Signup, Verify etc.)
│   ├── market/         # マーケット機能 (Item, Cart, Favorite etc.)
│   ├── user/           # ユーザー機能 (Profile, Account, Security etc.)
│   └── common/         # 共通コントローラー
├── service/            # ビジネスロジック
│   ├── auth/           # 認証サービス (LoginService, SignupService etc.)
│   ├── market/         # マーケットサービス
│   ├── user/           # ユーザーサービス
│   └── email/          # メール送信サービス
├── repository/         # データアクセス (JPA)
├── entity/             # JPA エンティティ
├── dto/                # データ転送オブジェクト (Request/Response)
├── security/           # セキュリティ設定・フィルター
├── exception/          # 例外ハンドリング・共通エラーレスポンス
└── util/               # ユーティリティ
```

## 🚀 開発ワークフロー

### 1. データベースの起動 (Docker)

開発時は、データベース (PostgreSQL) を Docker で起動し、アプリケーションはローカルで動かす構成を推奨します。

```bash
# DBをバックグラウンド起動
docker-compose up -d postgres
```

**起動されるサービス:**
- PostgreSQL: ホスト側ポート **5433** でアクセス可能

**メール送信について:**
- 実際のGmail SMTPサーバーを使用します
- `.env.local` に Google アカウント情報を設定してください

### 2. アプリケーションの起動 (Gradle)

```bash
# アプリケーション起動
./gradlew bootRun
```

サーバーは `http://localhost:8080` で起動します。
また、ホットリロード (DevTools) が有効になります。

### 3. ビルド

```bash
# テストを含めてビルド
./gradlew build

# テストをスキップしてビルド
./gradlew build -x test
```

### 4. データベースのリセット

データ整合性の問題などで、データベースを初期状態に戻したい場合は以下の方法があります。

#### 方法 A: Flyway タスクを使用する (推奨)

**前提**: データベース（Dockerコンテナ）が起動している必要があります。

```bash
# 1. DBコンテナが起動していない場合は起動
docker-compose up -d postgres

# 2. DBをクリーン（全テーブル削除）して、マイグレーションを再実行
./gradlew flywayClean flywayMigrate
```

#### 方法 B: Docker ボリュームを削除する (完全リセット)

Docker のボリュームを削除して、完全にクリーンな状態から作り直す方法です。

```bash
# 1. コンテナとボリュームを削除 (データを完全に消去)
docker-compose down -v

# 2. DBコンテナを再起動
docker-compose up -d postgres

# 3. マイグレーション実行
# (アプリ起動時にも自動実行されますが、手動で行う場合)
./gradlew flywayMigrate
```

## 📖 API ドキュメント

アプリケーション起動後、以下の URL で Swagger UI にアクセスできます。

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## 🧪 テスト

```bash
# 全テスト実行
./gradlew test

# 特定のテストのみ実行 (例)
./gradlew test --tests "com.example.myapp.service.auth.*"
```

## 📧 メール機能

本プロジェクトでは、Gmail SMTPサーバーを使用してメール送信を行います。

### メール設定

**Gmail を使用する場合:**
1. Googleアカウントで2段階認証を有効化
2. アプリパスワードを生成: https://myaccount.google.com/apppasswords
3. `.env.local` に以下を設定:

```dotenv
# メール設定
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-google-app-password
MAIL_FROM_NAME=GrandMarket
```

**設定項目:**
- `MAIL_USERNAME` - Gmailアドレス（認証用）
- `MAIL_PASSWORD` - Googleアプリパスワード
- `MAIL_FROM_NAME` - メール送信者名（カスタマイズ可能）

**注意:**
- SMTPサーバー（`smtp.gmail.com:587`）は固定値として `application.yml` に設定済み
- 送信元アドレスは自動的に `MAIL_USERNAME` が使用されます
- Googleが送信元を強制的に認証アカウントに設定するため、別のアドレスからの送信はできません

## データベース接続

### 接続情報

```
Host: localhost
Port: 5433
User: postgres
Password: postgres
Database: myapp
```
