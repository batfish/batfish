package org.batfish.grammar;

public interface GrammarSettings {

  boolean getDisableUnrecognized();

  int getMaxParserContextLines();

  int getMaxParserContextTokens();

  boolean getThrowOnLexerError();

  boolean getThrowOnParserError();

  boolean printParseTree();

  void setDisableUnrecognized(boolean b);

  void setThrowOnLexerError(boolean b);

  void setThrowOnParserError(boolean b);
}
