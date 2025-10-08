# プロジェクトのビルドと起動方法

## 概要
サーバー起動には3つのターミナルで並列実行する必要がある。

1. Spring Boot サーバー
2. Spring Boot 継続ビルド
3. TS,Tailwindなどのnpmサーバー

**※** すべての実行は`myapp`ディレクトリ行う。

### 技術スタック

-   **Spring Boot:** 3.5.4
-   **Java:** 21
-   **Kotlin:** 1.9.25


-   **Tailwind CSS:** 4.1.12
-   **TypeScript:** 5.9.2
-   **その他はpackage.jsonを参照...**

## 1.初回セットアップ

*   **npmインストール（初回だけ）**
    ```bash
    npm install
    ```

## 2.サーバー起動

*   **1:Spring Boot 継続ビルド**
    ```bash
    ./gradlew build --continuous
    ```

*   **2:Spring Boot サーバー起動**
    ```bash
    ./gradlew bootRun
    ```

*   **3:npmサーバー起動**
    ```bash
    npm run watch
    ```
    
### 開発用コマンド

*   **ESlint実行**
    ```bash
    npm run lint
    ```
*   **html/cssのフォーマット**
    ```bash
    npm run format
    ```