package org.batfish.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

@ParametersAreNonnullByDefault
public class UnrecognizedLineToken implements Token {
  private final int _line;
  private final String _parserContext;
  private final String _text;

  public UnrecognizedLineToken(String text, int line, String parserContext) {
    _line = line;
    _parserContext = parserContext;
    _text = text;
  }

  @Override
  public int getChannel() {
    return Lexer.DEFAULT_TOKEN_CHANNEL;
  }

  @Override
  public int getCharPositionInLine() {
    return 0;
  }

  @Override
  public CharStream getInputStream() {
    return null;
  }

  @Override
  public int getLine() {
    return _line;
  }

  public String getParserContext() {
    return _parserContext;
  }

  @Override
  public int getStartIndex() {
    return -1;
  }

  @Override
  public int getStopIndex() {
    return -1;
  }

  @Override
  public String getText() {
    return _text;
  }

  @Override
  public int getTokenIndex() {
    return 0;
  }

  @Override
  public TokenSource getTokenSource() {
    return null;
  }

  @Override
  public int getType() {
    return BatfishLexer.UNRECOGNIZED_LINE_TOKEN;
  }
}
