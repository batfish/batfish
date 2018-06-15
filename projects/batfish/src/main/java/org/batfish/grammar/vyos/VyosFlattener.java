package org.batfish.grammar.vyos;

import java.util.ArrayList;
import java.util.List;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.vyos.VyosParser.StatementContext;
import org.batfish.grammar.vyos.VyosParser.TerminatorContext;
import org.batfish.grammar.vyos.VyosParser.Vyos_configurationContext;
import org.batfish.grammar.vyos.VyosParser.WordContext;

public class VyosFlattener extends VyosParserBaseListener implements Flattener {

  private List<String> _currentBracketedWords;

  private List<String> _currentStatement;

  private String _flattenedConfigurationText;

  private final String _header;

  private boolean _inBrackets;

  private List<String> _setStatements;

  private List<List<String>> _stack;

  public VyosFlattener(String header) {
    _header = header;
    _stack = new ArrayList<>();
    _setStatements = new ArrayList<>();
  }

  @Override
  public void enterStatement(StatementContext ctx) {
    _currentStatement = new ArrayList<>();
    _stack.add(_currentStatement);
  }

  @Override
  public void exitStatement(StatementContext ctx) {
    _stack.remove(_stack.size() - 1);
  }

  @Override
  public void exitTerminator(TerminatorContext ctx) {
    StringBuilder sb = new StringBuilder();
    sb.append("set");
    for (List<String> prefix : _stack) {
      for (String word : prefix) {
        sb.append(" " + word);
      }
    }
    String setStatementBase = sb.toString();
    if (_currentBracketedWords != null) {
      for (String bracketedWord : _currentBracketedWords) {
        String setStatement = setStatementBase + " " + bracketedWord;
        _setStatements.add(setStatement);
      }
      _currentBracketedWords = null;
    } else {
      _setStatements.add(setStatementBase);
    }
  }

  @Override
  public void exitVyos_configuration(Vyos_configurationContext ctx) {
    StringBuilder sb = new StringBuilder();
    sb.append(_header);
    for (String setStatement : _setStatements) {
      sb.append(setStatement + "\n");
    }
    _flattenedConfigurationText = sb.toString();
  }

  @Override
  public void exitWord(WordContext ctx) {
    String word = ctx.getText();
    if (_inBrackets) {
      _currentBracketedWords.add(word);
    } else {
      _currentStatement.add(word);
    }
  }

  @Override
  public String getFlattenedConfigurationText() {
    return _flattenedConfigurationText;
  }

  @Override
  public FlattenerLineMap getOriginalLineMap() {
    throw new UnsupportedOperationException("getOriginalLines is not supported for VyosFlattener");
  }
}
