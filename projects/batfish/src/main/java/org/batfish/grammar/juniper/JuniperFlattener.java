package org.batfish.grammar.juniper;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.util.CommonUtil;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.juniper.JuniperParser.Bracketed_clauseContext;
import org.batfish.grammar.juniper.JuniperParser.Juniper_configurationContext;
import org.batfish.grammar.juniper.JuniperParser.StatementContext;
import org.batfish.grammar.juniper.JuniperParser.TerminatorContext;
import org.batfish.grammar.juniper.JuniperParser.WordContext;

public class JuniperFlattener extends JuniperParserBaseListener implements Flattener {

  private List<String> _allSetStatements;
  private List<WordContext> _currentBracketedWords;
  private LineTree.Node _currentNode;
  private List<WordContext> _currentStatement;
  private String _flattenedConfigurationText;
  private final String _header;
  private final Integer _headerLineCount;
  private StatementContext _inactiveStatement;
  private boolean _inBrackets;
  private FlattenerLineMap _lineMap;
  private List<List<WordContext>> _stack;
  private LineTree _tree;

  public JuniperFlattener(String header) {
    _header = header;
    // Determine length of header to offset subsequent line numbers for original line mapping
    _headerLineCount = header.split("\n", -1).length;
    _lineMap = new FlattenerLineMap();
    _stack = new ArrayList<>();
    _tree = new LineTree();
    _allSetStatements = new ArrayList<>();
  }

  @Override
  public void enterBracketed_clause(Bracketed_clauseContext ctx) {
    if (_inactiveStatement == null) {
      _currentBracketedWords = new ArrayList<>();
      _inBrackets = true;
    }
  }

  @Override
  public void enterJuniper_configuration(Juniper_configurationContext ctx) {
    _currentNode = _tree.getRoot();
  }

  @Override
  public void enterStatement(StatementContext ctx) {
    if (_inactiveStatement == null) {
      if (ctx.INACTIVE() != null) {
        _inactiveStatement = ctx;
      } else {
        String nodeKey =
            ctx.words.stream().map(ParserRuleContext::getText).collect(Collectors.joining(" "));
        if (ctx.REPLACE() != null) {
          _currentNode.getChildren().remove(nodeKey);
        }
        _currentNode =
            _currentNode
                .getChildren()
                .computeIfAbsent(nodeKey, k -> new LineTree.Node(_currentNode));
        _currentStatement = new ArrayList<>();
        _stack.add(_currentStatement);
      }
    }
  }

  @Override
  public void exitBracketed_clause(Bracketed_clauseContext ctx) {
    if (_inactiveStatement == null) {
      _inBrackets = false;
    }
  }

  @Override
  public void exitJuniper_configuration(Juniper_configurationContext ctx) {
    StringBuilder sb = new StringBuilder();
    sb.append(_header);
    Set<Integer> remainingSetStatements = _tree.getSetStatements();
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
    if (_inactiveStatement == null) {
      _stack.remove(_stack.size() - 1);
      _currentNode = _currentNode.getParent();
    } else if (_inactiveStatement == ctx) {
      _inactiveStatement = null;
    }
  }

  /** Helper method to construct and save set-line and line-mapping */
  private void constructSetLine() {
    StringBuilder sb = new StringBuilder();
    sb.append("set");
    for (List<WordContext> line : _stack) {
      for (WordContext wordCtx : line) {
        sb.append(" ");
        // Offset new line number by header line count
        _lineMap.setOriginalLine(
            _allSetStatements.size() + _headerLineCount,
            sb.length(),
            wordCtx.WORD().getSymbol().getLine());
        sb.append(wordCtx.getText());
      }
    }
    String setStatementText = sb.toString();
    _currentNode.getLines().add(_allSetStatements.size());
    _allSetStatements.add(setStatementText);
  }

  @Override
  public void exitTerminator(TerminatorContext ctx) {
    if (_inactiveStatement == null) {
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
  }

  @Override
  public void exitWord(WordContext ctx) {
    if (_inactiveStatement == null) {
      if (_inBrackets) {
        _currentBracketedWords.add(ctx);
      } else {
        _currentStatement.add(ctx);
      }
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
