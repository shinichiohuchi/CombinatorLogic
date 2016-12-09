package lib.string.combinator.strategy;

import lib.string.combinator.CombinatorLogic;

/**
 * 括弧で括られたCLTermに対して実行される戦略クラス。
 * @author shinichi666
 */
public final class BracketStrategy extends Strategy {
  /**
   * 括弧を扱うコンストラクタ。
   * @param aCode コンビネータインスタンス
   */
  public BracketStrategy(CombinatorLogic aCode) {
    code = aCode;
  }

  /**
   * 文字列の先頭が'('だった場合に実行されるメソッド<br>
   * 括弧の中の文字列に対して計算を行い、計算が終了したものを返す。
   * この時、計算結果は括弧で括られていない。
   * @return 計算結果のCLTerm
   */
  @Override
  public String calculate(String clterm) {
    // CLTermの一番外に存在する括弧の削除
    StringBuilder sb = new StringBuilder(clterm);
    sb.deleteCharAt(0);
    int length = sb.length();
    sb.deleteCharAt(length - 1);

    // 括弧内の文字列で再びCode.calculateを実行
    CombinatorLogic code = new CombinatorLogic(new String(sb));

    String result = "";
    while (code.canStep()) {
      result = code.getValue();
      code.step();
    }

    return result;
  }
}
