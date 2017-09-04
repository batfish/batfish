package org.batfish.grammar;

public interface GrammarSettings {

  int getMaxParserContextLines();

  int getMaxParserContextTokens();

  boolean getDisableUnrecognized();

  boolean getThrowOnLexerError();

  boolean getThrowOnParserError();

  boolean printParseTree();

  void setDisableUnrecognized(boolean b);

  void setThrowOnLexerError(boolean b);

  void setThrowOnParserError(boolean b);
}
