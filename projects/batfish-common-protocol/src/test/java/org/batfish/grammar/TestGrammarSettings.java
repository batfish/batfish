package org.batfish.grammar;

/**
 * Immutable implementation of {@link GrammarSettings} where settings must all be explicitly
 * provided upon instantiation.
 */
public class TestGrammarSettings implements GrammarSettings {

  private final boolean _disableUnrecognized;

  private final int _maxParserContextLines;

  private final int _maxParserContextTokens;

  private final boolean _printParseTree;

  private final boolean _throwOnLexerError;

  private final boolean _throwOnParserError;

  /**
   * Constructor where all {@link GrammarSettings} settings must be explicitly provided
   *
   * @param disableUnrecognized See {@link GrammarSettings#getDisableUnrecognized()}
   * @param maxParserContextLines See {@link GrammarSettings#getMaxParserContextLines()}
   * @param maxParserContextTokens See {@link GrammarSettings#getMaxParserContextTokens()}
   * @param printParseTree See {@link GrammarSettings#getPrintParseTree()}
   * @param throwOnLexerError See {@link GrammarSettings#getThrowOnLexerError()}
   * @param throwOnParserError See {@link GrammarSettings#getThrowOnParserError()}
   */
  public TestGrammarSettings(
      boolean disableUnrecognized,
      int maxParserContextLines,
      int maxParserContextTokens,
      boolean printParseTree,
      boolean throwOnLexerError,
      boolean throwOnParserError) {
    _disableUnrecognized = disableUnrecognized;
    _maxParserContextLines = maxParserContextLines;
    _maxParserContextTokens = maxParserContextTokens;
    _printParseTree = printParseTree;
    _throwOnLexerError = throwOnLexerError;
    _throwOnParserError = throwOnParserError;
  }

  @Override
  public boolean getDisableUnrecognized() {
    return _disableUnrecognized;
  }

  @Override
  public int getMaxParserContextLines() {
    return _maxParserContextLines;
  }

  @Override
  public int getMaxParserContextTokens() {
    return _maxParserContextTokens;
  }

  @Override
  public boolean getPrintParseTree() {
    return _printParseTree;
  }

  @Override
  public boolean getThrowOnLexerError() {
    return _throwOnLexerError;
  }

  @Override
  public boolean getThrowOnParserError() {
    return _throwOnParserError;
  }

  @Override
  public void setDisableUnrecognized(boolean b) {
    throw new UnsupportedOperationException("immutable");
  }

  @Override
  public void setPrintParseTree(boolean b) {
    throw new UnsupportedOperationException("immutable");
  }

  @Override
  public void setThrowOnLexerError(boolean b) {
    throw new UnsupportedOperationException("immutable");
  }

  @Override
  public void setThrowOnParserError(boolean b) {
    throw new UnsupportedOperationException("immutable");
  }
}
