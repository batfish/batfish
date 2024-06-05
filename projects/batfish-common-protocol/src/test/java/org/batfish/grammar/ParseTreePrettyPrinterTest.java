package org.batfish.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.ParseTreeSentences;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.recovery.RecoveryCombinedParser;
import org.junit.Test;

/** Test of {@link ParseTreePrettyPrinter}. */
@ParametersAreNonnullByDefault
public final class ParseTreePrettyPrinterTest {

  private static final GrammarSettings SETTINGS =
      MockGrammarSettings.builder()
          .setMaxParseTreePrintLength(1000)
          .setPrintParseTree(true)
          .setPrintParseTreeLineNums(true)
          .setThrowOnLexerError(true)
          .setThrowOnParserError(true)
          .build();

  @Test
  public void testParseTreePrettyPrintWithCharacterLimit() {
    List<String> strings = new ArrayList<>();
    strings.add("1234");

    String string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 0);
    assertThat(string, equalTo("1234"));

    string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 3);
    assertThat(string, equalTo("1234"));

    string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 4);
    assertThat(string, equalTo("1234"));

    strings.add("5678");
    string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 0);
    assertThat(string, equalTo("1234\n5678"));

    string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 1);
    assertThat(string, equalTo("1234\nand 1 more line(s)"));

    string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 5);
    assertThat(string, equalTo("1234\nand 1 more line(s)"));

    string = ParseTreePrettyPrinter.printWithCharacterLimit(strings, 6);
    assertThat(string, equalTo("1234\n5678"));
  }

  @Test
  public void testGetParseTreeSentencesLineNumbers() {
    String configText = readResource("org/batfish/grammar/line_numbers", UTF_8);
    RecoveryCombinedParser cp = new RecoveryCombinedParser(configText, SETTINGS);
    ParserRuleContext tree = cp.parse();
    ParseTreeSentences ptSentencesLineNums =
        ParseTreePrettyPrinter.getParseTreeSentences(tree, cp, true);

    /* Confirm printed parse tree includes line numbers when that option is set */
    assertThat(ptSentencesLineNums.getSentences().get(3), containsString("SIMPLE:'simple' line:1"));
    assertThat(ptSentencesLineNums.getSentences().get(9), containsString("BLOCK:'block' line:2"));
    assertThat(ptSentencesLineNums.getSentences().get(12), containsString("INNER:'inner' line:3"));
    assertThat(
        ptSentencesLineNums.getSentences().get(14), containsString("SIMPLE:'simple' line:3)"));
    assertThat(ptSentencesLineNums.getSentences().get(16), containsString("EOF:<EOF> line:5)"));
  }

  @Test
  public void testGetParseTreeSentencesMappedLineNumbers() {
    String configText = readResource("org/batfish/grammar/line_numbers", UTF_8);
    FlattenerLineMap lineMap = new FlattenerLineMap();
    /* Map words on each line to different original lines */
    /* (first) simple */
    lineMap.setOriginalLine(1, 0, 5);
    /* block */
    lineMap.setOriginalLine(2, 0, 6);
    /* inner */
    lineMap.setOriginalLine(3, 2, 7);
    /* (last) simple */
    lineMap.setOriginalLine(3, 7, 8);
    /* EOF */
    lineMap.setOriginalLine(5, 0, 9);
    RecoveryCombinedParser cp = new RecoveryCombinedParser(configText, SETTINGS, lineMap);
    ParserRuleContext tree = cp.parse();
    ParseTreeSentences ptSentencesLineNums =
        ParseTreePrettyPrinter.getParseTreeSentences(tree, cp, true);

    /* Confirm printed parse tree includes original line numbers */
    assertThat(ptSentencesLineNums.getSentences().get(3), containsString("SIMPLE:'simple' line:5"));
    assertThat(ptSentencesLineNums.getSentences().get(9), containsString("BLOCK:'block' line:6"));
    assertThat(ptSentencesLineNums.getSentences().get(12), containsString("INNER:'inner' line:7"));
    assertThat(
        ptSentencesLineNums.getSentences().get(14), containsString("SIMPLE:'simple' line:8)"));
    assertThat(ptSentencesLineNums.getSentences().get(16), containsString("EOF:<EOF> line:9)"));
  }
}
