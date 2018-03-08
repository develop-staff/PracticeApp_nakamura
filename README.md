# PracticeApp_nakamura
練習のために作ったアプリ

## photo_album
jpgファイルをアップロードし、それを一覧形式で表示する。<br>
「ランダムに並び替える」ボタンを押すと、画像一覧の並び順が変わる。

## demo
上記の「photo_album」を、Spring Bootを用いて実装

## demo_DB
上記のSpring Bootを用いたアプリを、H2データベースを用いて実装

## songs_album
課題アプリ。
サーバーに保存されている音楽ファイルを無作為に(最大で５つ)選び、それを再生していく。

[実行時の注意]
- 実行する前に、SongController.java の storePath 変数を、実行するパソコンにおけるパス(target/classes/static)に変更すること。
- ファイルをアップする際に、ファイルが０バイトだとisEmpty判定時にtrueと判定されるので、アップするファイルには何かしらを適当に書き込んで置くこと。
