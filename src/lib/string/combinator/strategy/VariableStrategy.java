package lib.string.combinator.strategy;

import lib.string.combinator.CombinatorLogic;

/**
 * <p>
 * 未定義のコンビネータを処理する戦略クラス。<br>
 * ここでは未定義のコンビネータを変数と記述している。<br>
 * </p>
 * <p>
 * 変数として扱われる条件は<br>
 * </p>
 * <p>
 * アルファベットの小文字で始まり,アンダースコア、または数字が0個以上連続する文字列<br>
 * </p>
 * である。
 */
public final class VariableStrategy extends Strategy {
  /**
   * 変数を扱う
   * @param aCode コンビネータインスタンス
   */
  public VariableStrategy(CombinatorLogic aCode) {
    code = aCode;
  }

  /**
   * 文字列の先頭が変数だった場合に実行されるメソッド<br>
   * .
   * 変数が見つかった場合、何も行わずにループを抜け出す。
   */
  @Override
  public String calculate(String clterm) {
    return "";
  }
}
