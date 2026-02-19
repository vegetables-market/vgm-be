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
MAIL_FROM_NAME=VGM Application
```

**必須の環境変数:**
- `MAIL_USERNAME` - Gmailアドレス（SMTP認証用、送信元アドレスとしても使用）
- `MAIL_PASSWORD` - Googleアプリパスワード（通常のパスワードではありません）
- `MAIL_FROM_NAME` - メール送信者名（任意、デフォルト: "VGM Application"）

**重要な注意事項:**
- SMTPサーバー（`smtp.gmail.com:587`）は `application.yml` に設定済みのため、`MAIL_HOST` や `MAIL_PORT` の環境変数は不要です
- 送信元メールアドレスは自動的に `MAIL_USERNAME` の値が使用されます（`application.yml` で `${spring.mail.username}` を参照）
- Googleのセキュリティポリシーにより、送信元は認証に使用したアカウントに強制されます

## 🗄 データベース接続

### ローカル開発環境

```
Host: localhost
Port: 5433
User: postgres
Password: postgres
Database: myapp
JDBC URL: jdbc:postgresql://localhost:5433/myapp
```

**環境変数設定 (`.env.local`):**
```dotenv
DB_URL=jdbc:postgresql://localhost:5433/myapp
DB_USER=postgres
DB_PASSWORD=postgres
IS_TAILSCALE=false
```

### Tailscale経由のリモートDB接続（Cloud Run用）

本プロジェクトは、Cloud Run環境から Tailscale VPN 経由でプライベートなデータベースに安全に接続できるよう設計されています。

**Tailscaleとは:**
- WireGuardベースのセキュアなVPNサービス
- パブリッククラウド（Cloud Run）からプライベートネットワーク（自宅サーバー等）への安全な接続を実現

**仕組み:**
1. `entrypoint.sh` でTailscaleデーモンを起動（userspace-networkingモード）
2. SOCKS5プロキシ経由でリモートDBへのトンネルを確立
3. アプリケーションは `127.0.0.1:5432` に接続（内部でリモートDBにルーティング）

**必要な環境変数（Cloud Runデプロイ時）:**
```yaml
IS_TAILSCALE=true
TAILSCALE_AUTH_KEY=tskey-auth-xxxxx      # Tailscale認証キー
TAILSCALE_VGM_DB_HOST=100.x.x.x          # TailscaleネットワークでのDBのIPアドレス
DB_URL=jdbc:postgresql://127.0.0.1:5432/vgm_db_prod
DB_USER=db_username
DB_PASSWORD=db_password
```

**注意:**
- `TAILSCALE_VGM_DB_HOST` はネットワークトンネル用のホストIPです
- アプリケーションの `DB_URL` には `127.0.0.1:5432` を指定します（socatトンネル経由）
- ローカル開発では `IS_TAILSCALE=false` にしてTailscaleを無効化してください

詳細は [`Tailscale機能調査レポート.md`](./Tailscale機能調査レポート.md) を参照してください。

## 🔐 Firebase認証設定

本プロジェクトは、Firebase Admin SDK を使用してトークン検証を行います。

**ローカル開発:**
```dotenv
FIREBASE_CREDENTIALS_PATH=grandmarket-app-firebase-adminsdk-fbsvc-ce7593aba8.json
```

**Cloud Runデプロイ:**
```dotenv
FIREBASE_CREDENTIALS_JSON=ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCg...（Base64エンコード）
```

**Base64エンコード方法（PowerShell）:**
```powershell
cd vgm-be
[Convert]::ToBase64String([IO.File]::ReadAllBytes(".\grandmarket-app-firebase-adminsdk-fbsvc-ce7593aba8.json"))
```

詳細は [`Firebase認証情報の設定方法.md`](./Firebase認証情報の設定方法.md) を参照してください。

## ⚙️ 環境変数一覧

### 必須の環境変数

| 環境変数 | 説明 | ローカル例 | 本番例 |
|---------|------|-----------|--------|
| `DB_URL` | PostgreSQL接続URL | `jdbc:postgresql://localhost:5433/myapp` | `jdbc:postgresql://127.0.0.1:5432/vgm_db_prod` |
| `DB_USER` | データベースユーザー名 | `postgres` | `prod_user` |
| `DB_PASSWORD` | データベースパスワード | `postgres` | `secure_password` |
| `MAIL_USERNAME` | Gmail送信用アドレス | `your-email@gmail.com` | `noreply@yourdomain.com` |
| `MAIL_PASSWORD` | Googleアプリパスワード | `xxxx xxxx xxxx xxxx` | `xxxx xxxx xxxx xxxx` |
| `FIREBASE_CREDENTIALS_PATH` or `FIREBASE_CREDENTIALS_JSON` | Firebase認証情報 | `path/to/file.json` | `Base64文字列` |

### オプションの環境変数

| 環境変数 | 説明 | デフォルト値 |
|---------|------|------------|
| `PORT` | サーバーポート | `8080` |
| `CORS_ALLOWED_ORIGINS` | CORS許可オリジン | `http://localhost:3000` |
| `MAIL_FROM_NAME` | メール送信者名 | `VGM Application` |
| `IS_TAILSCALE` | Tailscale使用フラグ | `false` |
| `TAILSCALE_AUTH_KEY` | Tailscale認証キー（Cloud Run用） | - |
| `TAILSCALE_VGM_DB_HOST` | TailscaleネットワークのDBホストIP | - |
| `MEDIA_JWT_SECRET` | vgm-media通信用JWT秘密鍵 | `your-256-bit-secret...` |

### 環境変数に関する重要な注意事項

#### CORS設定について
- `CORS_ALLOWED_ORIGINS`はカンマ区切りで**複数のオリジンを指定可能**です
- 例: `https://develop.vgm-app.pages.dev,https://vgm-app.pages.dev`
- **ローカル開発**: `http://localhost:3000`（デフォルト）
- **開発環境**: `https://develop.vgm-app.pages.dev`
- **本番環境**: `https://vgm-app.pages.dev`
- ⚠️ **CORSエラーが発生する場合は、フロントエンドのURLが正しく設定されているか確認してください**

#### メール送信設定について
- `MAIL_USERNAME`が**送信元メールアドレス**としても使用されます
- `application.yml`の`mail.from.email`は`${spring.mail.username}`から取得されます
- ~~`MAIL_FROM_EMAIL`環境変数は不要です~~（過去に使用されていましたが削除されました）
- Gmailを使用する場合、SMTPサーバー設定は`application.yml`に固定されています（`smtp.gmail.com:587`）

#### Firebase認証設定について
- **ローカル開発**: `FIREBASE_CREDENTIALS_PATH`にJSONファイルのパスを指定
- **Cloud Run**: `FIREBASE_CREDENTIALS_JSON`にBase64エンコードされたJSONを指定
- Base64エンコード方法（PowerShell）:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes(".\grandmarket-app-firebase-adminsdk-fbsvc-ce7593aba8.json"))
  ```

#### Tailscale設定について
- `IS_TAILSCALE=true`の場合、`entrypoint.sh`がTailscaleネットワークを起動します
- Cloud Runでは`TAILSCALE_AUTH_KEY`が必須です
- `DB_URL`は`127.0.0.1`を指定し、Tailscale経由でリモートDBに接続します

#### メディアサービス（vgm-media）設定について
- `MEDIA_JWT_SECRET`はバックエンド⇔メディアサーバー間のJWT署名用秘密鍵です
- **必ず256ビット以上のランダムな値を設定してください**
- 生成方法（PowerShell）:
  ```powershell
  $bytes = New-Object byte[] 32
  [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
  [Convert]::ToBase64String($bytes)
  ```
- デフォルト値（`your-256-bit-secret-must-be-very-long-and-secure`）は開発用です。本番環境では必ず変更してください

### 現在未使用の環境変数

以下の環境変数は削除されました：

- ~~`PLATFORM_FEE_RATE`~~ - マーケットプレイス手数料率（コード内で未使用のため削除）
- ~~`MAIL_FROM_EMAIL`~~ - `MAIL_USERNAME`が代わりに使用されます
- ~~`DB_HOST`~~, ~~`DB_NAME`~~ - `DB_URL`に統合されました
- `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET` - Stripe決済（コード全体がコメントアウト）
- `PAYPAY_API_KEY`, `PAYPAY_API_SECRET`, `PAYPAY_MERCHANT_ID`, `PAYPAY_API_BASE_URL` - PayPay決済（コード全体がコメントアウト）

詳細な設定例は [`.env.local.example`](./.env.local.example) を参照してください。

## データベース接続（旧セクション - 削除予定）

