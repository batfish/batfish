package org.batfish.grammar.juniper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.juniper.JuniperParser.Braced_clauseContext;
import org.batfish.grammar.juniper.JuniperParser.Bracketed_clauseContext;
import org.batfish.grammar.juniper.JuniperParser.Flat_statementContext;
import org.batfish.grammar.juniper.JuniperParser.Hierarchical_statementContext;
import org.batfish.grammar.juniper.JuniperParser.Juniper_configurationContext;
import org.batfish.grammar.juniper.JuniperParser.TagContext;
import org.batfish.grammar.juniper.JuniperParser.TerminatorContext;
import org.batfish.grammar.juniper.JuniperParser.WordContext;

public class JuniperFlattener extends JuniperParserBaseListener implements Flattener {

  /** An ordered list of all flat statements, including those not to be retained */
  private List<String> _allFlatStatements;

  private List<WordContext> _currentBracketedWords;
  private List<WordContext> _currentStatement;
  private String _flattenedConfigurationText;
  private final String _header;
  private final Integer _headerLineCount;
  private boolean _inBrackets;
  private boolean _inDelete;
  private FlattenerLineMap _lineMap;
  private List<List<WordContext>> _stack;
  private final String _text;
  private boolean _inEmptyBracedClause;
  private final List<Set<Integer>> _extraLines;

  public JuniperFlattener(String header, String text) {
    _header = header;
    _text = text;
    // Determine length of header to offset subsequent line numbers for original line mapping
    _headerLineCount = header.split("\n", -1).length;
    _lineMap = new FlattenerLineMap();
    _stack = new ArrayList<>();
    _allFlatStatements = new ArrayList<>();
    _extraLines = new ArrayList<>();
  }

  @Override
  public void enterBraced_clause(Braced_clauseContext ctx) {
    _inEmptyBracedClause = true;
  }

  @Override
  public void exitBraced_clause(Braced_clauseContext ctx) {
    if (_inEmptyBracedClause) {
      constructSetLine();
      _inEmptyBracedClause = false;
    }
  }

  @Override
  public void enterBracketed_clause(Bracketed_clauseContext ctx) {
    _currentBracketedWords = new ArrayList<>();
    _inBrackets = true;
  }

  @Override
  public void exitBracketed_clause(Bracketed_clauseContext ctx) {
    for (WordContext word : ctx.word()) {
      recordExtraLines(word.getStart().getLine());
    }
    _inBrackets = false;
  }

  @Override
  public void enterFlat_statement(Flat_statementContext ctx) {
    _currentStatement = new ArrayList<>();
  }

  @Override
  public void exitFlat_statement(Flat_statementContext ctx) {
    // Record index of this statement in the current subtree
    _lineMap.setOriginalLine(
        _allFlatStatements.size() + _headerLineCount, 0, ctx.getStart().getLine());
    _allFlatStatements.add(getFullText(ctx).trim());
  }

  @Override
  public void enterHierarchical_statement(Hierarchical_statementContext ctx) {
    // Only clear _inEmptyBracedClause if this statement has content (no delete tag)
    if (_inEmptyBracedClause && ctx.tag().stream().noneMatch(tag -> tag.DELETE() != null)) {
      _inEmptyBracedClause = false;
    }

    int firstWordLine = ctx.words.get(0).getStart().getLine();
    ImmutableSet.Builder<Integer> extraLinesBuilder =
        ImmutableSet.<Integer>builder().add(firstWordLine);
    ctx.descriptive_comment.forEach(
        comment ->
            IntStream.range(comment.getLine(), firstWordLine).forEach(extraLinesBuilder::add));
    if (ctx.close != null) {
      extraLinesBuilder.add(ctx.close.getLine());
    }
    _extraLines.add(extraLinesBuilder.build());
    _currentStatement = new ArrayList<>();
    _stack.add(_currentStatement);
    _inDelete = false;
    for (TagContext tagCtx : ctx.tag()) {
      constructTagCommand(tagCtx, ctx.words);
      _inDelete |= tagCtx.DELETE() != null;
    }
  }

  @Override
  public void exitHierarchical_statement(Hierarchical_statementContext ctx) {
    int firstWordLine = ctx.words.get(0).getStart().getLine();
    recordExtraLines(firstWordLine);
    _extraLines.remove(_extraLines.size() - 1);
    _stack.remove(_stack.size() - 1);
    _inDelete = false;
    // Finished recording set lines for this node key, so pop up
  }

  @Override
  public void exitJuniper_configuration(Juniper_configurationContext ctx) {
    StringBuilder sb = new StringBuilder();
    sb.append(_header);
    for (String flatStatement : _allFlatStatements) {
      sb.append(flatStatement).append("\n");
    }
    _flattenedConfigurationText = sb.toString();
  }

  @Override
  public void exitTerminator(TerminatorContext ctx) {
    if (_inDelete) {
      // Tag command already constructed; nothing more to do.
      return;
    }
    if (_currentBracketedWords != null) {
      // Make a separate set-line for each of the bracketed words
      for (WordContext bracketedWordCtx : _currentBracketedWords) {
        _stack.add(ImmutableList.of(bracketedWordCtx));
        constructSetLine();
        _stack.remove(_stack.size() - 1);
      }
      _currentBracketedWords = null;
    } else {
      constructSetLine();
    }
  }

  @Override
  public void exitWord(WordContext ctx) {
    if (_inDelete) {
      // Do not add this word to a line being built.
      return;
    }
    if (_inBrackets) {
      _currentBracketedWords.add(ctx);
    } else {
      _currentStatement.add(ctx);
    }
  }

  //////////////////////////////////////////////////////////////////////

  private void constructSetLine() {
    constructFlatLine("set", ImmutableList.of());
  }

  private void constructTagCommand(TagContext tagCtx, List<WordContext> suffixWords) {
    constructFlatLine(toCommandString(tagCtx), suffixWords);
  }

  private static @Nonnull String toCommandString(TagContext ctx) {
    if (ctx.ACTIVE() != null) {
      return "activate";
    } else if (ctx.INACTIVE() != null) {
      return "deactivate";
    } else if (ctx.DELETE() != null) {
      return "delete";
    } else {
      assert ctx.REPLACE() != null;
      return "replace";
    }
  }

  /**
   * Helper method to construct and save a flat-line and line-mapping.
   *
   * <p>Non-empty {@code suffixWords} are added by caller in case we must construct a line before
   * visiting words later/deeper in the parse tree that are needed at the end of the line to be
   * constructed. In this case, the suffix words are temporarily added to the top list of the {@link
   * #_stack}.
   *
   * <p>Precondition: {@code suffixWords} is empty, or the last element {@link #_stack}
   * (corresponding to the deepest parse tree element reflected in the stack) is a mutable list.
   */
  private void constructFlatLine(String command, List<WordContext> suffixWords) {

    List<WordContext> deepestParseTreeNodeWordsOnStack = _stack.get(_stack.size() - 1);
    if (!suffixWords.isEmpty()) {
      // Precondition requires this be mutable
      deepestParseTreeNodeWordsOnStack.addAll(suffixWords);
    }

    StringBuilder sb = new StringBuilder();
    sb.append(command);
    for (List<WordContext> line : _stack) {
      int originalLine = _allFlatStatements.size() + _headerLineCount;
      for (WordContext wordCtx : line) {
        sb.append(" ");
        // Offset new line number by header line count
        _lineMap.setOriginalLine(originalLine, sb.length(), wordCtx.WORD().getSymbol().getLine());
        sb.append(wordCtx.getText());
      }
    }
    String flatStatementText = sb.toString();
    // Record index of new statement in the current subtree
    _allFlatStatements.add(flatStatementText);

    for (int i = 0; i < suffixWords.size(); i++) {
      deepestParseTreeNodeWordsOnStack.remove(deepestParseTreeNodeWordsOnStack.size() - 1);
    }
  }

  @Override
  public String getFlattenedConfigurationText() {
    return _flattenedConfigurationText;
  }

  @Override
  public FlattenerLineMap getOriginalLineMap() {
    return _lineMap;
  }

  private String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    return _text.substring(start, end + 1);
  }

  private void recordExtraLines(int originalLine) {
    _lineMap.setExtraLines(originalLine, _extraLines.get(_extraLines.size() - 1));
  }
}
