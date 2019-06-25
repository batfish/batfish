package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexer;
import org.batfish.grammar.BatfishParser;

/** A class containing details regarding an error (e.g. parse error or convert error). */
public class ErrorDetails implements Serializable {

  private static final String PROP_PARSE_EXCEPTION_CONTEXT = "Parse_Exception_Context";
  private static final String PROP_MESSAGE = "Message";

  @Nullable private final ParseExceptionContext _parseExceptionContext;
  @Nullable private final String _message;

  @JsonCreator
  public ErrorDetails(
      @Nullable @JsonProperty(PROP_MESSAGE) String message,
      @Nullable @JsonProperty(PROP_PARSE_EXCEPTION_CONTEXT)
          ParseExceptionContext parseExceptionContext) {
    _message = message;
    _parseExceptionContext = parseExceptionContext;
  }

  public ErrorDetails(@Nonnull String message) {
    this(message, null);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ErrorDetails)) {
      return false;
    }

    ErrorDetails other = (ErrorDetails) o;
    return Objects.equals(_message, other._message)
        && Objects.equals(_parseExceptionContext, other._parseExceptionContext);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_message, _parseExceptionContext);
  }

  @JsonProperty(PROP_MESSAGE)
  @Nullable
  public String getMessage() {
    return _message;
  }

  @JsonProperty(PROP_PARSE_EXCEPTION_CONTEXT)
  @Nullable
  public ParseExceptionContext getParseExceptionContext() {
    return _parseExceptionContext;
  }

  /** A class to represent context for a parse exception for a file. */
  public static class ParseExceptionContext implements Serializable {

    private static final String PROP_LINE_CONTENT = "Line_Content";
    private static final String PROP_LINE_NUMBER = "Line_Number";
    private static final String PROP_PARSER_CONTEXT = "Parser_Context";

    @Nullable private final String _lineContent;
    @Nullable private final Integer _lineNumber;
    @Nullable private final String _parserContext;

    @JsonCreator
    public ParseExceptionContext(
        @Nullable @JsonProperty(PROP_LINE_CONTENT) String lineContent,
        @Nullable @JsonProperty(PROP_LINE_NUMBER) Integer lineNumber,
        @Nullable @JsonProperty(PROP_PARSER_CONTEXT) String parserContext) {
      _lineContent = lineContent;
      _lineNumber = lineNumber;
      _parserContext = parserContext;
    }

    public ParseExceptionContext(
        @Nonnull ParserRuleContext parseRuleContext,
        @Nonnull BatfishCombinedParser<? extends BatfishParser, ? extends BatfishLexer> parser,
        @Nonnull String rawText) {
      _lineNumber = parser.getLine(parseRuleContext.getStart());
      _lineContent = getFullText(parseRuleContext, rawText);
      List<String> ruleNames = Arrays.asList(parser.getParser().getRuleNames());
      _parserContext = parseRuleContext.toString(ruleNames);
    }

    public ParseExceptionContext(
        @Nonnull ParserRuleContext parseRuleContext,
        @Nonnull BatfishCombinedParser<? extends BatfishParser, ? extends BatfishLexer> parser) {
      _lineNumber = parser.getLine(parseRuleContext.getStart());
      _lineContent = getFullText(parseRuleContext, "");
      List<String> ruleNames = Arrays.asList(parser.getParser().getRuleNames());
      _parserContext = parseRuleContext.toString(ruleNames);
    }

    private static String getFullText(ParserRuleContext parserRuleContext, String rawText) {
      int start = parserRuleContext.getStart().getStartIndex();
      int end = parserRuleContext.getStop().getStopIndex();
      return rawText.substring(start, end + 1);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof ParseExceptionContext)) {
        return false;
      }

      ParseExceptionContext other = (ParseExceptionContext) o;
      return Objects.equals(_lineContent, other._lineContent)
          && Objects.equals(_lineNumber, other._lineNumber)
          && Objects.equals(_parserContext, other._parserContext);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_lineContent, _lineNumber, _parserContext);
    }

    @JsonProperty(PROP_LINE_CONTENT)
    @Nullable
    public String getLineContent() {
      return _lineContent;
    }

    @JsonProperty(PROP_LINE_NUMBER)
    @Nullable
    public Integer getLineNumber() {
      return _lineNumber;
    }

    @JsonProperty(PROP_PARSER_CONTEXT)
    @Nullable
    public String getParserContext() {
      return _parserContext;
    }
  }
}
