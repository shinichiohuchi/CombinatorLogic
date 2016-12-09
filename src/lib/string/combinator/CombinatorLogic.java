package lib.string.combinator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lib.string.combinator.strategy.BracketStrategy;
import lib.string.combinator.strategy.VariableStrategy;

/**
 * <p>コンビネータ論理計算を行うクラス。<br>
 * 初期コンビネータとしてSKIBCのコンビネータを標準で保持している。<br>
 * これらのコンビネータを後述の追加定義した場合でも、
 * 初期定義してあるコンビネータが優先される。<br>
 * ただし、setメソッドを使用した場合はこの限りではない。</p>
 *
 * <p>コンビネータを追加で定義する場合は、
 * 専用のフォーマットにしたがったテキストファイルをメソッドを通してセットするか、
 * リスト型に格納済みのものをメソッドを通してセットするかの2通りの方法が存在する。</p>
 *
 * <p>コンビネータを定義したファイルでコンビネータを追加する場合、
 * 以下のフォーマットにしたがったファイルである必要がある。</p>
 *
 * {@literal @formatter:off} <br>
 *
 * <ol type="1">
 * 	<li><p>定義は1行単位で読み込むため、
 *         複数の定義を1行に記述することはできない。</p></li>
 *
 * 	<li><p>'#'で始まる行はコメント文として無視される。
 *         ただしインラインコメントとして利用することはできない。</p></li>
 *
 * 	<li><p>何も記述していない改行のみの行は無視される。</p></li>
 *
 * 	<li><p>コンビネータ名, 置換するコンビネータ<br>
 *         または<br>
 *         コンビネータ名, 必要な引数の数, 置換フォーマット<br>
 *         という書式でコンビネータを定義する。</p></li>
 *
 * 	<li><p>空白文字や全角空白文字、タブ文字などは読み込み時に無視される。<br>
 *         これにより、コンビネータの定義に空白を挟んで整形などを行える。<br>
 *         上記の理由により、コンビネータの振舞いに空白文字を使用できない。</p></li>
 * </ol>
 *
 * <p>上記のフォーマットを適用した例は次のようになる。<br></p>
 * <ul>
 *  <li># Sabc = ac(bc)</li>
 *  <li>S, 3, 02(12)</li>
 *  <li></li>
 *
 *  <li># {@literal <zero>} = (KI)</li>
 *  <li>  {@literal <zero>}, 0, (KI)</li>
 *  <li></li>
 *
 *  <li># {@literal <0>} = (KI)</li>
 *  <li>  {@literal <0>}, (KI)</li>
 * </ul>
 *
 * <p>このフォーマットは','で区切った配列として取り込まれる<br>
 * 同様に、List&lt;String[]&gt;でコンビネータを追加する場合は、
 * 上記のフォーマットと同様に<br></p>
 * <ul>
 *  <li>str[0] = コンビネータ名</li>
 *  <li>str[1] = 必要な引数の数</li>
 *  <li>str[2] = フォーマット</li>
 * </ul>
 * <p>といった書式でリストに登録しなければならない。</p><br>
 *
 * {@literal @formatter:on} <br>
 *
 * @author Shinichi Ouchi
 * @version 1.4
 */
public class CombinatorLogic {
  /**
   * 先頭のコンビネータが引数不足に直面するまでループするのを制御するためのスイッチ。
   */
  private boolean stepable = true;

  /**
   * 計算に用いる可変長コンビネータ論理文字列。
   */
  private StringBuilder codeSb;

  /**
   * CodeSbの先頭に存在するコンビネータ論理項を取り出した物。
   */
  private String clterm;

  /**
   * マクロ関数のリスト。
   */
  private static List<MacroCombinator> macroCombinatorList;

  /**
   * 変数の正規表現パターン。
   */
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[a-z][_0-9]*");

  /**
   * SKIBCコンビネータの定義。
   */
  public static final String[][] INITIAL_COMBINATORS = {
      {
          "S", "3", "02(12)"
      },
      {
          "K", "2", "0"
      },
      {
          "I", "1", "0"
      },
      {
          "B", "3", "0(12)"
      },
      {
          "C", "3", "021"
      },
  };

  /**
   * コンストラクタ。
   * @param string 計算する文字列
   */
  public CombinatorLogic(String string) {
    codeSb = new StringBuilder(string);
    if (macroCombinatorList == null) {
      setMacroCombinatorList(Arrays.asList(INITIAL_COMBINATORS));
    }
  }

  /**
   * 初期定義のSKIBCコンビネータに後から定義したコンビネータを追加する。
   * 定義する配列は一つ目がコンビネータ名、二つ目が置換するコンビネータ
   * の順である必要がある。
   * リストにnullが渡された場合、初期コンビネータのみが定義される。
   * @param string 計算する文字列
   * @param array コンビネータの定義
   */
  public CombinatorLogic(String string, String[] array) {
    codeSb = new StringBuilder(string);
    List<String[]> newList = new ArrayList<>();
    newList.addAll(Arrays.asList(INITIAL_COMBINATORS));
    if (array != null) {
      newList.add(formatCombinatorsArray(array));
    }
    setMacroCombinatorList(newList);
  }

  /**
   * 初期定義のSKIBCコンビネータに後から定義したコンビネータを追加する。
   * 定義する配列は一つ目がコンビネータ名、二つ目が置換するコンビネータ
   * の順である必要がある。
   * リストにnullが渡された場合、初期コンビネータのみが定義される。
   * @param string 計算する文字列
   * @param list コンビネータの定義
   */
  public CombinatorLogic(String string, List<String[]> list) {
    codeSb = new StringBuilder(string);
    List<String[]> newList = new ArrayList<>();
    newList.addAll(Arrays.asList(INITIAL_COMBINATORS));
    if (list != null) {
      newList.addAll(makeMacroCombinatorsList(list));
    }
    setMacroCombinatorList(newList);
  }

  /**
   * 計算を行い、次のステップへと進む。
   */
  public void step() {
    combinatorDo();
    if (!stepable) {
      codeSb.insert(0, clterm);
    }
  }

  /**
   * CLTermをcodeSbから取り出す。<br>
   * この時、取り出したCLTermは削除される。
   * @return 取り出したCLTerm
   */
  public String pollCLTerm() {
    if (0 < codeSb.length()) {
      String clt = getCLTerm();
      codeSb.delete(0, clt.length());
      return clt;
    }
    return "";
  }

  /**
   * 既存のリストに定義を追加する。
   * @param array 定義配列
   */
  public static void addMacroCombinatorList(String[] array) {
    macroCombinatorList.add(new MacroCombinator(formatCombinatorsArray(array)));
  }

  /**
   * コンビネータの定義フォーマットのリストに整形したリストを返す。
   * @param list テキストのリスト
   * @return 先頭が#で始まるテキストを無視し、空白を削除し
   *         カンマで区切った配列のリスト
   */
  public static List<String[]> splitList(List<String> list) {
    return list.stream()
        .filter(l -> !l.startsWith("#") && l.length() != 0)
        .map(m -> m.replaceAll("[ ||　||\t]", "").split(","))
        .collect(Collectors.toList());
  }

  /**
   * ファイルパスからマクロ定義用のリストを生成し返す。
   * @param file マクロを定義したファイル
   * @return コンビネータ定義リスト
   */
  public static List<String[]> makeMacroCombinatorsList(File file) {
    Path path = file.toPath();
    try (BufferedReader br = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
      return br.lines()
          .filter(l -> !l.startsWith("#") && l.length() != 0)
          .map(m -> m.replaceAll("[ ||　||\t]", "").split(","))
          .map(CombinatorLogic::formatCombinatorsArray)
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 計算中のプログラムの先頭の文字列が、マクロで定義したコンビネータ名と一致するかどうかを調べ、
   * 一致した場合はコンビネータを実行する。
   * 一致しなかった場合は、括弧で始まる場合か、変数の場合であると判定する。
   * 最終的にloopableをfalseにし、計算用に文字列を取り出した場合は復元して終了する。
   */
  private void combinatorDo() {
    clterm = pollCLTerm();

    String result = "";
    for (MacroCombinator macro : macroCombinatorList) {
      if (clterm.startsWith(macro.getName())) {
        result = new MacroCombinator(macro).calculate(this);
        codeSb.insert(0, result);
        return;
      }
    }

    if (0 < clterm.length()) {
      if (clterm.startsWith("(")) {
        result = new BracketStrategy(this).calculate(clterm);
      }
      if (clterm.startsWith("[a-z]")) {
        result = new VariableStrategy(this).calculate(clterm);
      }
      codeSb.insert(0, result);
    }
    stepable = 0 < result.length();
  }

  /**
   * 要素数が2の場合、3に整形した配列として返す。
   * 要素数が2出なかった場合はそのまま返す。
   * @param array
   * @return 整形後の配列。
   */
  private static String[] formatCombinatorsArray(String[] array) {
    if (array.length == 2) {
      String[] newArray = new String[3];
      newArray[0] = array[0];
      newArray[1] = "" + 0;
      newArray[2] = array[1];
      return newArray;
    }
    return array;
  }

  /**
   * 定義定義リストを作って返す。
   * @param list
   * @return 定義配列のリスト
   */
  private static List<String[]> makeMacroCombinatorsList(List<String[]> list) {
    return list.stream()
        .map(CombinatorLogic::formatCombinatorsArray)
        .collect(Collectors.toList());
  }

  // **************************************************
  // Getter
  // **************************************************
  /**
   * 次のステップを実行できるか否かを返す。
   * @return 実行可能 = true, 実行不可 = false
   */
  public boolean canStep() {
    return stepable;
  }

  /**
   * 括弧の入りと終わりが正しく同じ数を持つか否かを調べ、
   * 正常にコードを計算できるか否かを返す。
   * @return
   *         括弧の数が等しい場合、trueを返す。<br>
   *         括弧の数が不等な場合、falseを返す。<br>
   */
  public boolean isCorrectCode() {
    int count = 0;
    String code = new String(codeSb);
    for (char ch : code.toCharArray()) {
      count = ch == '(' ? count + 1 : count;
      count = ch == ')' ? count - 1 : count;
      if (count < 0) {
        return false;
      }
    }
    if (count == 0) {
      return true;
    }
    return false;
  }

  /**
   * <p>コンビネータ論理項(CLTerm)を返す。<br>
   * 取り出すCLTermはそれぞれ以下のいずれかである。<br></p>
   *
   * {@literal @formatter:off} <br>
   *
   * <ol type="1">
   *   <li> <p> 括弧で括られた括弧を含む文字列 </p> </li>
   *
   *   <li> <p> アルファベットの小文字で始まり、
   *            アンダースコアや数字が0個以上連続する文字列 </p> </li>
   *
   *   <li> <p> テキストファイルで定義したコンビネータ名と一致する文字列 </p> </li>
   * </ol>
   *
   * {@literal @formatter:on} <br>
   *
   * <p>上記のいずれとも一致しない文字列の場合は、先頭の1文字だけを返す。 </p>
   *
   * @return CLTerm 取り出したCLTerm
   */
  public String getCLTerm() {
    String code = new String(codeSb);
    // コードの先頭の文字を取り出して判定
    char top = code.charAt(0);
    if (top == '(') {
      // 括弧の場合
      return getCompoundTerm(code);
    }
    if (Character.isLowerCase(top)) {
      // 小文字で始まる変数項の場合
      Matcher m = VARIABLE_PATTERN.matcher(code);
      if (m.find()) {
        return m.group();
      }
    }
    for (MacroCombinator macro : macroCombinatorList) {
      // マクロ定義した関数の場合
      String functionName = macro.getName();
      if (code.startsWith(functionName)) {
        return code.substring(0, functionName.length());
      }
    }
    // 未定義の関数の場合
    String sub = code.substring(0, 1);
    return sub;
  }

  /**
   * CLTermの数を数える
   * @return CLTermの数
   */
  public int getCLTermCount() {
    int clTermCount = 0;
    StringBuilder copySb = new StringBuilder(codeSb);
    while (true) {
      if (0 < codeSb.length()) {
        pollCLTerm();
        clTermCount++;
      } else {
        break;
      }
    }
    codeSb = new StringBuilder(copySb);
    return clTermCount;
  }

  /**
   * 現在のCLCodeを返す。
   * @return CLCode
   */
  public String getValue() {
    return new String(codeSb);
  }

  /**
   * 定義したコンビネータを文字列のリストとして返す。
   * @return 定義コンビネータのリスト
   */
  public static List<String[]> getMacroCombinatorsStringList() {
    List<String[]> newList = new ArrayList<>();
    for (MacroCombinator macro : macroCombinatorList) {
      String[] array = new String[3];
      array[0] = macro.getName();
      array[1] = "" + macro.getArgsCount();
      array[2] = macro.getFormat();
      newList.add(array);
    }
    return newList;
  }

  /**
   * SKIBCコンビネータのみの初期リストを返す。
   * @return SKiコンビネータのみ定義したリスト
   */
  public static List<String[]> getInitialMacroCombinatorsList() {
    return Arrays.asList(INITIAL_COMBINATORS);
  }

  /**
   * 先頭の括弧でくくられた項を返す(括弧自体も含む)。
   * @param target 対象文字列
   * @return 括弧で括られた文字列
   */
  private String getCompoundTerm(String target) {
    return getCompoundTerm(target, 0);
  }

  /**
   * indexの位置から開始する括弧で括られた項を返す(括弧自体も含む)。
   * @param target 対象文字列
   * @param index 括弧の開始位置
   * @return 括弧で括られた文字列
   */
  private String getCompoundTerm(String target, int index) {
    int count = 0;
    StringBuilder sb = new StringBuilder();
    for (char ch : target.toCharArray()) {
      count = ch == '(' ? count + 1 : count;
      count = ch == ')' ? count - 1 : count;
      sb.append(ch);
      // ')'を見つけ、最終的にネストの回数が0になった時、括弧の終わりと判定し、ループを抜ける。
      if (count == 0) {
        break;
      }
    }
    return new String(sb);
  }

  // **************************************************
  // Setter
  // **************************************************
  /**
   * 引数に渡したコンビネータ定義ファイルからコンビネータのリストを生成しセットする。
   * @param combinatorsFile コンビネータを定義したファイル
   */
  public static void setMacroCombinatorList(File combinatorsFile) {
    makeMacroCombinatorsList(combinatorsFile)
        .forEach(array -> {
          macroCombinatorList.add(new MacroCombinator(array));
        });
  }

  /**
   * 配列のリストをコンビネータの定義としてリストにセットする。
   * @param combinators 配列のリストで定義したコンビネータ
   */
  public static void setMacroCombinatorList(List<String[]> combinators) {
    macroCombinatorList = combinators.stream()
        .map(MacroCombinator::new)
        .collect(Collectors.toList());
  }

  /**
   * 次のステップが実行可能かどうかをセットする。
   * @param aLoopable
   */
  void setLoopable(boolean aLoopable) {
    stepable = aLoopable;
  }
}
