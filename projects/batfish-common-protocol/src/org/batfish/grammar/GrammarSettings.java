package org.batfish.grammar;

public interface GrammarSettings {

   int getMaxParserContextLines();

   int getMaxParserContextTokens();

   boolean getThrowOnLexerError();

   boolean getThrowOnParserError();

   boolean printParseTree();

   void setThrowOnLexerError(boolean b);

   void setThrowOnParserError(boolean b);

}
