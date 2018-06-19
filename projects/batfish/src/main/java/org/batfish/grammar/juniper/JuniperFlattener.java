package org.batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.List;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.grammar.juniper.JuniperParser.Bracketed_clauseContext;
import org.batfish.grammar.juniper.JuniperParser.Juniper_configurationContext;
import org.batfish.grammar.juniper.JuniperParser.StatementContext;
import org.batfish.grammar.juniper.JuniperParser.TerminatorContext;
import org.batfish.grammar.juniper.JuniperParser.WordContext;

public class JuniperFlattener extends JuniperParserBaseListener implements Flattener {

  private List<String> _currentBracketedWords;

  private List<String> _currentStatement;

  private String _flattenedConfigurationText;

  private final String _header;

  private StatementContext _inactiveStatement;

  private boolean _inBrackets;

  private List<String> _setStatements;

  private List<List<String>> _stack;

  public JuniperFlattener(String header) {
    _header = header;
    _stack = new ArrayList<>();
    _setStatements = new ArrayList<>();
  }

  @Override
  public void enterBracketed_clause(Bracketed_clauseContext ctx) {
    if (_inactiveStatement == null) {
      _currentBracketedWords = new ArrayList<>();
      _inBrackets = true;
    }
  }

  @Override
  public void enterStatement(StatementContext ctx) {
    if (_inactiveStatement == null) {
      if (ctx.INACTIVE() != null) {
        _inactiveStatement = ctx;
      } else {
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
    for (String setStatement : _setStatements) {
      sb.append(setStatement + "\n");
    }
    _flattenedConfigurationText = sb.toString();
  }

  @Override
  public void exitStatement(StatementContext ctx) {
    if (_inactiveStatement == null) {
      _stack.remove(_stack.size() - 1);
    } else if (_inactiveStatement == ctx) {
      _inactiveStatement = null;
    }
  }

  @Override
  public void exitTerminator(TerminatorContext ctx) {
    if (_inactiveStatement == null) {
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
  }

  @Override
  public void exitWord(WordContext ctx) {
    if (_inactiveStatement == null) {
      String word = ctx.getText();
      if (_inBrackets) {
        _currentBracketedWords.add(word);
      } else {
        _currentStatement.add(word);
      }
    }
  }

  @Override
  public String getFlattenedConfigurationText() {
    return _flattenedConfigurationText;
  }

  @Override
  public FlattenerLineMap getOriginalLineMap() {
    throw new UnsupportedOperationException(
        "getOriginalLines is not supported for JuniperFlattener");
  }
}
