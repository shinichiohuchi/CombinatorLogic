package lib.string.combinator;

import java.util.LinkedList;

/**
 * コンビネータのふるまいを定義するクラス。
 * @author Shinichi Ouchi
 */
final class MacroCombinator {
  /**
   * CodeSbから取り出したCLTermのリスト。
   */
  private LinkedList<String> list = new LinkedList<>();

  /**
   * 関数名。
   */
  private final String name;

  /**
   * 関数が必要とする引数の数。
   */
  private final int argsCount;

  /**
   * 整形フォーマット。
   */
  private final String format;

  /**
   * 配列からコンビネータを定義する。
   * @param arrays コンビネータ名, 引数の数, フォーマット
   */
  MacroCombinator(String[] arrays) {
    this(arrays[0], arrays[1], arrays[2]);
  }

  /**
   * マクロコンビネータのインスタンスの値をコピーして
   * コンビネータを定義する。
   */
  MacroCombinator(MacroCombinator macro) {
    this(macro.name, String.valueOf(macro.argsCount), macro.format);
  }

  /**
   * コンビネータを定義する。
   * @param aFunctionName コンビネータ名
   * @param anArgsCount 引数の数
   * @param aFormat フォーマット
   */
  MacroCombinator(String aFunctionName, String anArgsCount, String aFormat) {
    name = aFunctionName;
    argsCount = Integer.parseInt(anArgsCount);
    format = aFormat;
  }

  /**
   * 関数を実行し、可能だった場合は整形後の文字列、あるいはフォーマットをそのまま返す。<br>
   * @return 実行できる場合 = true, 出来ない場合 = false
   */
  final String calculate(CombinatorLogic aCode) {
    return 0 < argsCount ? combinatorDo(aCode) : format;
  }

  /**
   * 取り出した文字が数値か否かを調べ、
   * 数値だった場合、数値を数値と対応したリストの文字列に置換する
   */
  private final String replaceNumber() {
    StringBuilder sb = new StringBuilder();
    for (String str : format.split("")) {
      if (str.matches("^[0-9]")) {
        int index = Integer.parseInt(str);
        str = list.get(index);
      }
      sb.append(str);
    }
    return new String(sb);
  }

  /**
   * コンビネータを実行する。
   * @param aCode
   * @return 計算結果
   */
  private final String combinatorDo(CombinatorLogic aCode) {
    String clterm = aCode.pollCLTerm();
    if (0 < clterm.length()) {
      list.addLast(clterm);

      if (argsCount <= list.size()) {
        // コンビネータ実行のための引数が足りた場合
        String formatedCode = replaceNumber();
        return formatedCode;
      }
      return combinatorDo(aCode);
    }
    // 元のコードの文字列が空になって、これ以上項を取り出すことが不可能な場合
    // 取り出していたリストに格納したコンビネータを復元し、呼び出し元に返却する。
    String combinator = String.join("", list);
    aCode.setLoopable(false);
    return combinator;
  }

  /**
   * コンビネータの名前を返す。
   * @return コンビネータ名
   */
  final String getName() {
    return name;
  }

  /**
   * 引数の数を返す。
   * @return 引数の数
   */
  final int getArgsCount() {
    return argsCount;
  }

  /**
   * 整形フォーマットを返す。
   * @return 整形フォーマット
   */
  final String getFormat() {
    return format;
  }

  @Override
  public String toString() {
    return String.format("name: %s, argsCount: %d, format: %s", name, argsCount, format);
  }
}
