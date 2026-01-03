package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Immutable implementation of {@link GrammarSettings} where settings must all be explicitly
 * provided upon instantiation.
 */
@ParametersAreNonnullByDefault
public final class MockGrammarSettings implements GrammarSettings {

  public static final class Builder {

    private boolean _disableUnrecognized;
    private int _maxParserContextLines;
    private int _maxParserContextTokens;
    private int _maxParseTreePrintLength;
    private boolean _printParseTree;
    private boolean _printParseTreeLineNums;
    private boolean _throwOnLexerError;
    private boolean _throwOnParserError;

    private Builder() {}

    public @Nonnull MockGrammarSettings build() {
      return new MockGrammarSettings(
          _disableUnrecognized,
          _maxParserContextLines,
          _maxParserContextTokens,
          _maxParseTreePrintLength,
          _printParseTree,
          _printParseTreeLineNums,
          _throwOnLexerError,
          _throwOnParserError);
    }

    public @Nonnull Builder setDisableUnrecognized(boolean disableUnrecognized) {
      _disableUnrecognized = disableUnrecognized;
      return this;
    }

    public @Nonnull Builder setMaxParserContextLines(int maxParserContextLines) {
      _maxParserContextLines = maxParserContextLines;
      return this;
    }

    public @Nonnull Builder setMaxParserContextTokens(int maxParserContextTokens) {
      _maxParserContextTokens = maxParserContextTokens;
      return this;
    }

    public @Nonnull Builder setMaxParseTreePrintLength(int maxParseTreePrintLength) {
      _maxParseTreePrintLength = maxParseTreePrintLength;
      return this;
    }

    public @Nonnull Builder setPrintParseTree(boolean printParseTree) {
      _printParseTree = printParseTree;
      return this;
    }

    public @Nonnull Builder setPrintParseTreeLineNums(boolean printParseTreeLineNums) {
      _printParseTreeLineNums = printParseTreeLineNums;
      return this;
    }

    public @Nonnull Builder setThrowOnLexerError(boolean throwOnLexerError) {
      _throwOnLexerError = throwOnLexerError;
      return this;
    }

    public @Nonnull Builder setThrowOnParserError(boolean throwOnParserError) {
      _throwOnParserError = throwOnParserError;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final boolean _disableUnrecognized;
  private final int _maxParserContextLines;
  private final int _maxParserContextTokens;
  private final int _maxParseTreePrintLength;
  private final boolean _printParseTree;
  private final boolean _printParseTreeLineNums;
  private final boolean _throwOnLexerError;
  private final boolean _throwOnParserError;

  /**
   * Constructor where all {@link GrammarSettings} settings must be explicitly provided
   *
   * @param disableUnrecognized See {@link GrammarSettings#getDisableUnrecognized()}
   * @param maxParserContextLines See {@link GrammarSettings#getMaxParserContextLines()}
   * @param maxParserContextTokens See {@link GrammarSettings#getMaxParserContextTokens()}
   * @param maxParseTreePrintLength See {@link GrammarSettings#getMaxParseTreePrintLength()}
   * @param printParseTree See {@link GrammarSettings#getPrintParseTree()}
   * @param throwOnLexerError See {@link GrammarSettings#getThrowOnLexerError()}
   * @param throwOnParserError See {@link GrammarSettings#getThrowOnParserError()}
   */
  private MockGrammarSettings(
      boolean disableUnrecognized,
      int maxParserContextLines,
      int maxParserContextTokens,
      int maxParseTreePrintLength,
      boolean printParseTree,
      boolean printParseTreeLineNums,
      boolean throwOnLexerError,
      boolean throwOnParserError) {
    _disableUnrecognized = disableUnrecognized;
    _maxParserContextLines = maxParserContextLines;
    _maxParserContextTokens = maxParserContextTokens;
    _maxParseTreePrintLength = maxParseTreePrintLength;
    _printParseTree = printParseTree;
    _printParseTreeLineNums = printParseTreeLineNums;
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
  public int getMaxParseTreePrintLength() {
    return _maxParseTreePrintLength;
  }

  @Override
  public boolean getPrintParseTree() {
    return _printParseTree;
  }

  @Override
  public boolean getPrintParseTreeLineNums() {
    return _printParseTreeLineNums;
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
  public void setPrintParseTreeLineNums(boolean b) {
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
