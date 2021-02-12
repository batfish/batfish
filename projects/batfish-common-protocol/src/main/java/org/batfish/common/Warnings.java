package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.batfish.common.BatfishLogger.LEVEL_PEDANTIC;
import static org.batfish.common.BatfishLogger.LEVEL_REDFLAG;
import static org.batfish.common.BatfishLogger.LEVEL_UNIMPLEMENTED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class Warnings implements Serializable {

  public static final String TAG_PEDANTIC = "MISCELLANEOUS";

  public static final String TAG_RED_FLAG = "MISCELLANEOUS";

  public static final String TAG_UNIMPLEMENTED = "UNIMPLEMENTED";
  private static final String PROP_ERROR_DETAILS = "Error details";
  private static final String PROP_PARSE_WARNINGS = "Parse warnings";
  private static final String PROP_PEDANTIC = "Pedantic complaints";
  private static final String PROP_RED_FLAGS = "Red flags";
  private static final String PROP_UNIMPLEMENTED = "Unimplemented features";

  private ErrorDetails _errorDetails;

  @Nonnull private final List<ParseWarning> _parseWarnings;

  private transient boolean _pedanticRecord;

  private final SortedSet<Warning> _pedanticWarnings;

  private transient boolean _redFlagRecord;

  private final SortedSet<Warning> _redFlagWarnings;

  private transient boolean _unimplementedRecord;

  private final SortedSet<Warning> _unimplementedWarnings;

  public static @Nonnull Warnings forLogger(BatfishLogger logger) {
    return new Warnings(
        logger.isActive(LEVEL_PEDANTIC),
        logger.isActive(LEVEL_REDFLAG),
        logger.isActive(LEVEL_UNIMPLEMENTED));
  }

  @JsonCreator
  private Warnings(
      @Nullable @JsonProperty(PROP_PEDANTIC) SortedSet<Warning> pedanticWarnings,
      @Nullable @JsonProperty(PROP_RED_FLAGS) SortedSet<Warning> redFlagWarnings,
      @Nullable @JsonProperty(PROP_UNIMPLEMENTED) SortedSet<Warning> unimplementedWarnings,
      @Nullable @JsonProperty(PROP_PARSE_WARNINGS) List<ParseWarning> parseWarnings,
      @Nullable @JsonProperty(PROP_ERROR_DETAILS) ErrorDetails errorDetails) {
    _pedanticWarnings = firstNonNull(pedanticWarnings, new TreeSet<>());
    _redFlagWarnings = firstNonNull(redFlagWarnings, new TreeSet<>());
    _parseWarnings = firstNonNull(parseWarnings, new LinkedList<>());
    _unimplementedWarnings = firstNonNull(unimplementedWarnings, new TreeSet<>());
    _errorDetails = errorDetails;
  }

  public Warnings() {
    this(false, false, false);
  }

  public Warnings(boolean pedanticRecord, boolean redFlagRecord, boolean unimplementedRecord) {
    this(null, null, null, null, null);
    _pedanticRecord = pedanticRecord;
    _redFlagRecord = redFlagRecord;
    _unimplementedRecord = unimplementedRecord;
  }

  @Nonnull
  @JsonProperty(PROP_PARSE_WARNINGS)
  public List<ParseWarning> getParseWarnings() {
    return _parseWarnings;
  }

  @JsonProperty(PROP_PEDANTIC)
  public SortedSet<Warning> getPedanticWarnings() {
    return _pedanticWarnings;
  }

  @JsonProperty(PROP_RED_FLAGS)
  public SortedSet<Warning> getRedFlagWarnings() {
    return _redFlagWarnings;
  }

  @JsonProperty(PROP_UNIMPLEMENTED)
  public SortedSet<Warning> getUnimplementedWarnings() {
    return _unimplementedWarnings;
  }

  @JsonProperty(PROP_ERROR_DETAILS)
  public ErrorDetails getErrorDetails() {
    return _errorDetails;
  }

  @JsonProperty(PROP_ERROR_DETAILS)
  public void setErrorDetails(ErrorDetails errorDetails) {
    _errorDetails = errorDetails;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _pedanticWarnings.isEmpty()
        && _redFlagWarnings.isEmpty()
        && _unimplementedWarnings.isEmpty()
        && _parseWarnings.isEmpty();
  }

  public void pedantic(String msg) {
    if (!_pedanticRecord) {
      return;
    }
    pedantic(msg, TAG_PEDANTIC);
  }

  public void pedantic(String msg, String tag) {
    _pedanticWarnings.add(new Warning(msg, tag));
  }

  public void redFlag(String msg) {
    redFlag(msg, TAG_RED_FLAG);
  }

  public void redFlag(String msg, String tag) {
    if (!_redFlagRecord) {
      return;
    }
    _redFlagWarnings.add(new Warning(msg, tag));
  }

  /**
   * Adds a note that there is work to do to handle the given {@link ParserRuleContext}. The output
   * will include the text of the given {@code line} and, for debugging/implementation, the current
   * parser rule stack, and the given {@code comment} if present.
   *
   * <p>This function warns on the line of the first token of {@code ctx}. To override the line
   * number, use {@link #addWarningOnLine(int, ParserRuleContext, String, BatfishCombinedParser,
   * String)}.
   */
  public void addWarning(
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser,
      @Nonnull String comment) {
    int lineNumber = parser.getLine(ctx.getStart());
    addWarningOnLine(lineNumber, ctx, line, parser, comment);
  }

  /**
   * Adds a note that there is work to do to handle the given {@link ParserRuleContext}. The output
   * will include the text of the given {@code line} and, for debugging/implementation, the current
   * parser rule stack, and the given {@code comment} if present.
   *
   * <p>This function warns on the given line. To use the line number of the first token
   * automatically, use {@link #addWarning}.
   */
  public void addWarningOnLine(
      int lineNumber,
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser,
      @Nonnull String comment) {
    String ruleStack = ctx.toString(Arrays.asList(parser.getParser().getRuleNames()));
    String trimmedLine = line.trim();
    _parseWarnings.add(new ParseWarning(lineNumber, trimmedLine, ruleStack, comment));
  }

  /** @see #addWarning(ParserRuleContext, String, BatfishCombinedParser, String) */
  public void todo(
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser) {
    addWarning(ctx, line, parser, "This feature is not currently supported");
  }

  public void unimplemented(String msg) {
    if (!_unimplementedRecord) {
      return;
    }
    _unimplementedWarnings.add(new Warning(msg, TAG_UNIMPLEMENTED));
  }

  /** A class to represent a parse warning in a file. */
  public static final class ParseWarning implements Serializable {

    private static final String PROP_COMMENT = "Comment";
    private static final String PROP_LINE = "Line";
    private static final String PROP_PARSER_CONTEXT = "Parser_Context";
    private static final String PROP_TEXT = "Text";

    @Nullable private final String _comment;
    private final int _line;
    @Nonnull private final String _parserContext;
    @Nonnull private final String _text;

    @JsonCreator
    private static ParseWarning create(
        @JsonProperty(PROP_LINE) @Nullable Integer line,
        @JsonProperty(PROP_TEXT) @Nullable String text,
        @JsonProperty(PROP_PARSER_CONTEXT) @Nullable String parserContext,
        @JsonProperty(PROP_COMMENT) @Nullable String comment) {
      checkArgument(line != null, "Missing %s", PROP_LINE);
      // empty strings can get serialized as nulls
      return new ParseWarning(
          line, firstNonNull(text, ""), firstNonNull(parserContext, ""), comment);
    }

    public ParseWarning(
        int line, @Nonnull String text, @Nonnull String parserContext, @Nullable String comment) {
      _line = line;
      _text = requireNonNull(text, PROP_TEXT);
      _parserContext = requireNonNull(parserContext, PROP_PARSER_CONTEXT);
      _comment = comment;
    }

    @JsonProperty(PROP_COMMENT)
    @Nullable
    public String getComment() {
      return _comment;
    }

    @JsonProperty(PROP_LINE)
    public int getLine() {
      return _line;
    }

    @JsonProperty(PROP_PARSER_CONTEXT)
    @Nonnull
    public String getParserContext() {
      return _parserContext;
    }

    @JsonProperty(PROP_TEXT)
    @Nonnull
    public String getText() {
      return _text;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ParseWarning)) {
        return false;
      }
      ParseWarning that = (ParseWarning) o;
      return _line == that._line
          && Objects.equals(_comment, that._comment)
          && _parserContext.equals(that._parserContext)
          && _text.equals(that._text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_comment, _line, _parserContext, _text);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .omitNullValues()
          .add("line", _line)
          .add("text", _text)
          .add("comment", _comment)
          .add("parserContext", _parserContext)
          .toString();
    }
  }
}
