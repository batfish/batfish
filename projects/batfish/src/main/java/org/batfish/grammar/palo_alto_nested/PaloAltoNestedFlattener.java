package org.batfish.grammar.palo_alto_nested;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.util.CommonUtil;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedParser.Bracketed_clauseContext;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedParser.Palo_alto_nested_configurationContext;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedParser.StatementContext;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedParser.TerminatorContext;
import org.batfish.grammar.palo_alto_nested.PaloAltoNestedParser.WordContext;

public class PaloAltoNestedFlattener extends PaloAltoNestedParserBaseListener implements Flattener {

  /** An ordered list of all produced set statements, including those not to be retained */
  private List<String> _allSetStatements;

  private List<WordContext> _currentBracketedWords;
  private List<WordContext> _currentStatement;
  private SetStatementTree _currentTree;
  private String _flattenedConfigurationText;
  private final String _header;
  private boolean _inBrackets;
  private FlattenerLineMap _lineMap;

  /**
   * Number of lines in the output text, used for line mapping. This needs to be updated as the
   * output set statement list is being populated.
   */
  private int _outputLineCount;

  private SetStatementTree _root;

  private LinkedList<List<WordContext>> _stack;

  public PaloAltoNestedFlattener(String header) {
    _header = header;
    // Determine length of header to offset subsequent line numbers for original line mapping
    _outputLineCount = countLines(header);
    _lineMap = new FlattenerLineMap();
    _stack = new LinkedList<>();
    _root = new SetStatementTree();
    _allSetStatements = new LinkedList<>();
  }

  /** Count the number of lines in a given string */
  private static int countLines(String string) {
    if (string == null) {
      return 0;
    }
    if (string.isEmpty()) {
      return 1;
    }

    int count = 1;
    int i = 0;

    while (i < string.length()) {
      char c = string.charAt(i);

      if (c == '\r') {
        if (i + 1 < string.length() && string.charAt(i + 1) == '\n') {
          count++;
          i += 2;
        } else {
          count++;
          i++;
        }
      } else if (c == '\n') {
        count++;
        i++;
      } else {
        i++;
      }
    }

    return count;
  }

  @Override
  public void enterBracketed_clause(Bracketed_clauseContext ctx) {
    _currentBracketedWords = new ArrayList<>();
    _inBrackets = true;
  }

  @Override
  public void enterPalo_alto_nested_configuration(Palo_alto_nested_configurationContext ctx) {
    _currentTree = _root;
  }

  @Override
  public void enterStatement(StatementContext ctx) {
    String statementTextAtCurrentDepth =
        ctx.words.stream().map(ParserRuleContext::getText).collect(Collectors.joining(" "));
    // Grab or add child at the current tree node for the node key for this statement
    _currentTree = _currentTree.getOrAddSubtree(statementTextAtCurrentDepth);
    _currentStatement = new LinkedList<>();
    _stack.add(_currentStatement);
  }

  @Override
  public void exitBracketed_clause(Bracketed_clauseContext ctx) {
    _inBrackets = false;
  }

  @Override
  public void exitPalo_alto_nested_configuration(Palo_alto_nested_configurationContext ctx) {
    StringBuilder sb = new StringBuilder();
    sb.append(_header);
    Set<Integer> remainingSetStatements = _root.getSetStatementIndices();
    CommonUtil.forEachWithIndex(
        _allSetStatements,
        (i, setStatement) -> {
          if (!remainingSetStatements.contains(i)) {
            return;
          }
          sb.append(setStatement).append("\n");
        });
    _flattenedConfigurationText = sb.toString();
  }

  @Override
  public void exitStatement(StatementContext ctx) {
    _stack.removeLast();
    // Finished recording set lines for this node key, so pop up
    _currentTree = _currentTree.getParent();
  }

  /** Helper method to construct and save set-line and line-mapping */
  private void constructSetLine() {
    StringBuilder sb = new StringBuilder();
    sb.append("set");
    for (List<WordContext> line : _stack) {
      for (WordContext wordCtx : line) {
        sb.append(" ");
        int orgLine = wordCtx.getStart().getLine();
        // Assume that sb length corresponds to column in the current line, i.e. no multiline
        // tokens before this (assume they're always last token on their line)
        _lineMap.setOriginalLine(_outputLineCount, sb.length(), orgLine);

        // Account for newlines inside of tokens, e.g. "something\nsomething" is two lines
        // Subtract 1 since token's line is counted later and don't want to double count
        int tokenExtraLines = countLines(wordCtx.getText()) - 1;
        for (int i = 1; i <= tokenExtraLines; i++) {
          _lineMap.setOriginalLine(_outputLineCount + i, 0, orgLine);
        }
        _outputLineCount += tokenExtraLines;
        sb.append(wordCtx.getText());
      }
    }
    String setStatementText = sb.toString();
    // Record index of new statement in the current subtree
    _currentTree.addSetStatementIndex(_allSetStatements.size());
    // Account for newlines for each new set statement
    _outputLineCount++;
    _allSetStatements.add(setStatementText);
  }

  @Override
  public void exitTerminator(TerminatorContext ctx) {
    if (_currentBracketedWords != null) {
      // Make a separate set-line for each of the bracketed words
      for (WordContext bracketedWordCtx : _currentBracketedWords) {
        _stack.add(ImmutableList.of(bracketedWordCtx));
        constructSetLine();
        _stack.removeLast();
      }
      _currentBracketedWords = null;
    } else {
      constructSetLine();
    }
  }

  @Override
  public void exitWord(WordContext ctx) {
    if (_inBrackets) {
      _currentBracketedWords.add(ctx);
    } else {
      _currentStatement.add(ctx);
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
}
