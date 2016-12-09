package lib.string.combinator.strategy;

import lib.string.combinator.CombinatorLogic;

/**
 * 例外を処理するための戦略クラス。
 */
public abstract class Strategy {
  /**
   * コンビネータ論理のインスタンス
   */
  protected CombinatorLogic code;

  /**
   * コンストラクタ
   */
  public Strategy() {
  }

  /**
   * 計算結果を返す抽象メソッド
   * @param clterm 計算させるコード
   * @return 計算結果
   */
  public abstract String calculate(String clterm);
}
