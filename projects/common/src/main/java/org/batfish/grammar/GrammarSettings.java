package org.batfish.grammar;

/** Settings for parsers and lexers */
public interface GrammarSettings {

  /**
   * When {@code true}, unrecognized lines will trigger a crash. When {@code false}, custom recovery
   * via {@link BatfishANTLRErrorStrategy} will be performed for supported grammars; for other
   * grammars, vanilla ANTLR recovery will be performed and syntax errors logged via {@link
   * BatfishParserErrorListener} and {@link BatfishLexerErrorListener}.
   */
  boolean getDisableUnrecognized();

  /**
   * In error report, the maximum number of lines of context in a file surrounding a line triggering
   * a parsing/lexing error.
   *
   * @return The max number of context lines
   */
  int getMaxParserContextLines();

  /**
   * In error report, the maximum number of lines of context tokens surrounding a line triggering a
   * parsing/lexing error.
   *
   * @return The max number of context tokens
   */
  int getMaxParserContextTokens();

  /**
   * The maximum number of characters display of a parse tree when prettyPrinting.
   *
   * @return The max number of characters to display
   */
  int getMaxParseTreePrintLength();

  /**
   * Controls whether parse trees are stored in parse job results.
   *
   * @return true iff parse trees should be stored in parse job results
   */
  boolean getPrintParseTree();

  /**
   * Controls whether parse tree line numbers are printed in parse trees.
   *
   * @return true iff parse trees should include line numbers
   */
  boolean getPrintParseTreeLineNums();

  /**
   * Whether or not to throw an exception immediately upon a report of an error to the {@link
   * BatfishLexerErrorListener}, ceasing processing for the given text.
   *
   * @return true iff an exception should be thrown immediately upon a report of an error to the
   *     {@link BatfishLexerErrorListener}
   */
  boolean getThrowOnLexerError();

  /**
   * Whether or not to throw an exception immediately upon a report of an error to the {@link
   * BatfishParserErrorListener}, ceasing processing for the given text.
   *
   * @return true iff an exception should be thrown immediately upon a report of an error to the
   *     {@link BatfishParserErrorListener}
   */
  boolean getThrowOnParserError();

  /**
   * See {@link GrammarSettings#getDisableUnrecognized()}
   *
   * @param disableUnrecognized The new value to be returned by subsequent calls to {@link
   *     GrammarSettings#getDisableUnrecognized()}
   */
  void setDisableUnrecognized(boolean disableUnrecognized);

  /**
   * See {@link GrammarSettings#getPrintParseTree()}
   *
   * @param printParseTree The new value to be returned by subsequent calls to {@link
   *     GrammarSettings#getPrintParseTree()}
   */
  void setPrintParseTree(boolean printParseTree);

  /**
   * See {@link GrammarSettings#getPrintParseTreeLineNums()}
   *
   * @param printParseTreeLineNums The new value to be returned by subsequent calls to {@link
   *     GrammarSettings#getPrintParseTreeLineNums()}
   */
  void setPrintParseTreeLineNums(boolean printParseTreeLineNums);

  /**
   * See {@link GrammarSettings#getThrowOnLexerError()}
   *
   * @param throwOnLexerError The new value to be returned by subsequent calls to {@link
   *     GrammarSettings#getThrowOnLexerError()}
   */
  void setThrowOnLexerError(boolean throwOnLexerError);

  /**
   * See {@link GrammarSettings#getThrowOnParserError()}
   *
   * @param throwOnParserError The new value to be returned by subsequent calls to {@link
   *     GrammarSettings#getThrowOnParserError()}
   */
  void setThrowOnParserError(boolean throwOnParserError);
}
