# EcoVote

[![License](https://img.shields.io/badge/license-LGPL--3.0-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-0.11-green.svg)](pom.xml)
[![Minecraft](https://img.shields.io/badge/minecraft-1.20.4-orange.svg)](https://www.spigotmc.org/)

節電鯖用 nuVotifier 拡張プラグイン / Minecraft voting reward system plugin

## 概要 / Overview

EcoVoteは、MinecraftサーバーでのプレイヤーによるWebサイトでの投票を検出し、投票者に報酬アイテムを配布するプラグインです。nuVotifierと連携して動作し、投票データの永続化や統計情報の管理も行います。

EcoVote is a Minecraft plugin that detects player votes on voting websites and distributes reward items to voters. It works in conjunction with nuVotifier and also provides vote data persistence and statistics management.

## 機能 / Features

- 🗳️ **投票検出**: nuVotifierからの投票イベントを受信
- 🎁 **報酬配布**: 投票者への自動アイテム配布システム
- 📊 **統計管理**: プレイヤーの投票数や投票履歴の記録
- 💾 **データベース対応**: SQLite/MySQL での永続化
- 📢 **ブロードキャスト**: 投票時のサーバー内アナウンス
- ⚙️ **柔軟な設定**: 投票サイトのホワイトリスト機能

## 必要環境 / Requirements

### 必須依存関係 / Required Dependencies
- **Minecraft Server**: Spigot/Paper 1.20.4+
- **Java**: 17+
- **[nuVotifier](https://www.spigotmc.org/resources/nuvotifier.13449/)**: 2.6.0+
- **EcoFramework**: 0.29+ (カスタムフレームワーク)

### オプション依存関係 / Optional Dependencies
- **EcoEgg**: 1.11+ (オプション機能 - アイテム報酬システム用)
- **EcoMQTTServerLog**: 0.6+ (プレイヤーUUID解決用)

## インストール / Installation

1. **依存関係のインストール**
   ```
   plugins/
   ├── Votifier.jar (nuVotifier)
   ├── EcoFramework.jar
   └── EcoVote.jar
   ```

2. **設定ファイルの生成**
   サーバーを一度起動し、`plugins/EcoVote/config.yml` を生成

3. **設定の編集**
   ```yaml
   servers:
     - 'example.com'  # 投票を受け入れるサイトのドメイン
   max-stock-gift: 30
   broadcast:
     use: true
     message: '{player} voted in {service}'
   ```

4. **サーバー再起動**

## 設定 / Configuration

### config.yml

```yaml
# 投票を受け入れるサーバーのリスト
servers:
  - 'example.com'
  - 'voting-site.net'

# 最大ストック可能なギフト数
max-stock-gift: 30

# ブロードキャスト設定
broadcast:
  use: true  # ブロードキャストを有効にする
  message: '{player} voted in {service}'  # メッセージフォーマット

# データベース設定
votedb:
  use: false  # データベース使用の有効/無効
  db: "sqlite"  # データベースタイプ: sqlite または mysql
  name: "vote.db"  # データベース名/ファイル名
  server: "localhost:3306"  # MySQLサーバー（MySQLの場合のみ）
  user: "username"  # MySQLユーザー名
  pass: "password"  # MySQLパスワード
```

## コマンド / Commands

| コマンド | 説明 | 権限 | 使用例 |
|---------|------|------|--------|
| `/evote` | 投票統計情報を表示 | `ecovote` | `/evote` |
| `/evote get` | ストックされた報酬アイテムを取得 | `ecovote.get` | `/evote get` |
| `/evote test` | プラグインのテスト機能（管理者用） | `ecovote.test` | `/evote test vote 1` |
| `/evote reload` | 設定ファイルを再読み込み | `ecovote.reload` | `/evote reload` |

### テストコマンドの使用方法

```bash
# 投票イベントのシミュレーション
/evote test server [service_name] [username]

# 報酬アイテムのテスト
/evote test vote [1-3]
```

## 権限 / Permissions

| 権限ノード | デフォルト | 説明 |
|-----------|----------|------|
| `ecovote` | true | 基本的なコマンドの使用権限 |
| `ecovote.get` | true | 報酬アイテムの取得権限 |
| `ecovote.test` | op | テストコマンドの使用権限 |
| `ecovote.reload` | op | 設定再読み込み権限 |

## データベーススキーマ / Database Schema

### totalvote テーブル
プレイヤーの総投票数を記録

| カラム | 型 | 説明 |
|-------|----|----|
| most | BIGINT | プレイヤーUUID上位64bit |
| least | BIGINT | プレイヤーUUID下位64bit |
| vote | BIGINT | 総投票数 |
| lastdate | BIGINT | 最終投票日時 |

### itemqueue テーブル  
未配布のアイテムを記録

| カラム | 型 | 説明 |
|-------|----|----|
| most | BIGINT | プレイヤーUUID上位64bit |
| least | BIGINT | プレイヤーUUID下位64bit |
| date | BIGINT | 投票日時 |
| items | TEXT | シリアライズされたアイテムデータ |

## 開発情報 / Development

### ビルド / Build

```bash
mvn clean compile package
```

### 主要クラス / Main Classes

- **EcoVote.java**: メインプラグインクラス
- **VoteListener.java**: 投票イベントリスナー
- **AsyncVoteTimer.java**: 非同期投票処理タイマー
- **VoteStore.java**: データベース操作クラス
- **VotePayload.java**: スレッド間データ通信用ペイロード

### フレームワーク依存関係

このプラグインは EcoFramework に依存しており、以下の基底クラスを使用しています：
- `PluginFrame`: プラグインの基底クラス
- `CommandFrame`: コマンドの基底クラス
- `ListenerFrame`: イベントリスナーの基底クラス
- `DatabaseFrame`: データベース操作の基底クラス

## トラブルシューティング / Troubleshooting

### よくある問題

1. **投票が検出されない**
   - nuVotifierが正しく設定されているか確認
   - `servers` リストに投票サイトのドメインが含まれているか確認

2. **報酬アイテムが配布されない**
   - EcoEggプラグインがインストールされているか確認
   - プレイヤーのインベントリに空きがあるか確認

3. **データベースエラー**
   - データベース設定を確認
   - ファイル権限を確認（SQLiteの場合）

## ライセンス / License

このプロジェクトは GNU Lesser General Public License v3.0 の下で公開されています。詳細は [LICENSE](LICENSE) ファイルを参照してください。

## 作者 / Author

- **ecolight** - 開発者

## 貢献 / Contributing

バグ報告や機能要望は GitHub Issues で受け付けています。プルリクエストも歓迎します。

## 更新履歴 / Changelog

### v0.11
- 現在のバージョン
- Minecraft 1.20.4 対応
- 基本的な投票報酬システム
