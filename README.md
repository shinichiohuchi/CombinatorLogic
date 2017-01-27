combinator-logic.jar マニュアル
================================================================================

これはコンビネータ論理(CombinatoryLogic)の計算用Javaライブラリです。  

バージョン     : 1.4  
作者           : 大内真一 (Shinichi Oouchi)  
作成日         : 2016/12/09  
最終更新日     : 2016/12/09  
連絡先         : [GitHub](https://github.com/shinichiohuchi/CombinatorLogic)  
動作確認・開発環境
- OS         : Windows10 Pro 64bit, LinuxBean12.04
- プロセッサ : 2.00GHz Intel Core i7-3667U
- メモリ     : 8GB RAM
- 開発言語   : Java 1.8.0-111
- 実行環境   : Java 1.8.0-111

## 概要 ########################################################################

これはコンビネータ論理(CombinatoryLogic)の計算用Javaライブラリです。

一般的なコンビネータとしてSKIBCコンビネータをソース内で定義しており、これらは計
算を実行するCombinatorLogic#step()メソッドを呼び出した際に、追加定義をする必要
なく利用できます。

このライブラリでは、前述のSKIBCコンビネータ以外のコンビネータや独自のコンビネー
タを後から追加定義することが可能です。

## 注意点 ######################################################################

これはコンビネータ論理のシミュレータを作成するにあたって、計算用に作成したクラ
スをライブラリとして抜き出したものです。

動作をシミュレートするために作成したのですが内部のコンビネータ論理文字列(以下
CLCode)を計算するにあたって、文字列の並び替え処理などに本来必要ではない引数を使
用しています。

つまり、コンビネータ論理の本来の動きである引数を持たずに計算を行うという_関数型
の計算方法ではなく、引数を使用した一般的な計算方法_で文字列処理を行っています。

あくまでも文字列の処理をシミュレートすることに主眼を置いて作成したため、_本来の
コンビネータ論理と異なる実装方法になっている_ことをあらかじめ断っておきます。

## ライブラリの使い方 ##########################################################

### CLCodeの計算 ###############################################################

例えば計算させたいCLCode"Sxyz"が存在するとします。  
これをこのライブラリを使用して計算をさせる場合、  

    String clcode = "Sxyz";
    CombinatorLogic cl = new CombinatorLogic(clcode);
    cl.step();
    System.out.println(cl.getValue());

とすることで、先頭のコンビネータが計算を行った1ステップ後の計算結果を標準出力す
ることができます。

これが例えば非常に長い文字列であった場合に、計算不可能になって終了するまでス
テップを進め、結果を取得したい場合は、

    String clcode = "SKIBCSKIBC...";
    CombinatorLogic cl = new CombinatorLogic(clcode);
    while (cl.canStep()) {
      cl.step();
    }
    System.out.println(cl.getValue());

とすることで、無限ループに陥るCLCodeでない限り、計算が終了するまでステップを実
行し、結果を出力します。

### コンビネータの追加定義 #####################################################

概要でも述べました通り、このライブラリではコンビネータを後から追加定義すること
が可能です。

コンビネータの追加方法としては、

1. 専用のフォーマットにしたがったファイルをメソッドを通してセットする
2. 定義を格納した配列をメソッドを通してセットする

の2種類の方法があります。

コンビネータを定義したファイルでコンビネータを追加する場合、以下のフォーマット
にしたがったファイルでなければなりません。

1. 定義は1行単位で読み込むため、複数の定義を1行に記述することはできない。  
2. '#'で始まる行はコメント文として無視される。
   ただしインラインコメントとして利用することはできない。  
3. 何も記述していない改行のみの行は無視される。  
4. コンビネータ名, 置換するコンビネータ  
   または  
   コンビネータ名, 必要な引数の数, 置換フォーマット  
   という書式でコンビネータを定義する。  
   この時、空白文字や全角空白文字、タブ文字などは読み込み時に無視される。  
   これにより、コンビネータの定義に空白を挟んで整形などを行えるが、  
   上記の理由により、コンビネータの振舞いに空白文字を使用できない。  

上記のフォーマットを適用した例は次のようになります。  

    # Sabc -> ac(bc)
    S, 3, 02(12)

    # チャーチ数の0, <zero>をそのまま(KI)に置換
    <zero>, 0, (KI)

    # 2つ目の数値を省略した場合は、0を挿入した場合と同じ置換処理を行う
    <0>, (KI)

このフォーマットは','で区切った配列として取り込まれます。  
同様に、List<String[]>でコンビネータを追加する場合は、上記のフォーマットと同様に  

    string[0] = コンビネータ名
    string[1] = 引数の数
    string[2] = 整形フォーマット

または

    string[0] = コンビネータ名
    string[1] = 置換後の文字列

といった書式で配列に格納している必要があります。  

## 既知の不具合 ################################################################

- コンビネータを追加定義する場合、指定できる引数の上限は現バージョンでは9までと
  なっています。これは数値の桁数を考慮にいれずに作成したことに起因する問題です。
  今後修正を予定しています。

- 追加定義したコンビネータ名に数字を使用すると予期せぬ結果を返す場合があります。
  対処法としては、コンビネータの名前に数字を使わないようにしてください。

- コンストラクタによるコンビネータの追加と、メソッドによるコンビネータで追加の仕
  方がことなります。
  
  コンストラクタからコンビネータを追加した場合は、初期で用意しているSKIBCコンビ
  ネータが自動的に追加されるようになっていますが、メソッドでコンビネータを追加
  した場合は、初期コンビネータは追加されません。

## 更新履歴 ####################################################################

- Ver1.4
  プログラム公開
