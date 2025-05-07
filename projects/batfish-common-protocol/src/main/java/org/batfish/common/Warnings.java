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
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.FormatMethod;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

@ParametersAreNonnullByDefault
public class Warnings implements Serializable {

  public static class Settings implements Serializable {
    private final boolean _pedanticRecord;
    private final boolean _redFlagRecord;
    private final boolean _unimplementedRecord;

    public Settings(boolean pedanticRecord, boolean redFlagRecord, boolean unimplementedRecord) {
      this._pedanticRecord = pedanticRecord;
      this._redFlagRecord = redFlagRecord;
      this._unimplementedRecord = unimplementedRecord;
    }

    public static @Nonnull Settings fromLogger(BatfishLogger logger) {
      return new Warnings.Settings(
          logger.isActive(LEVEL_PEDANTIC),
          logger.isActive(LEVEL_REDFLAG),
          logger.isActive(LEVEL_UNIMPLEMENTED));
    }
  }

  /**
   * Indicates that this warning will cause the config to not successfully commit onto the device
   */
  public static final String FATAL_FLAG = "FATAL: ";

  /**
   * Indicates that this warning is for a construct on the device that may result in unexpected
   * undesired behavior
   */
  public static final String RISKY_FLAG = "RISK: ";

  public static final String TAG_PEDANTIC = "MISCELLANEOUS";

  public static final String TAG_RED_FLAG = "MISCELLANEOUS";

  public static final String TAG_UNIMPLEMENTED = "UNIMPLEMENTED";
  private static final String PROP_ERROR_DETAILS = "Error details";
  private static final String PROP_PARSE_WARNINGS = "Parse warnings";
  private static final String PROP_PEDANTIC = "Pedantic complaints";
  private static final String PROP_RED_FLAGS = "Red flags";
  private static final String PROP_UNIMPLEMENTED = "Unimplemented features";

  private final Settings _settings;

  private @Nullable ErrorDetails _errorDetails;

  private final @Nonnull List<ParseWarning> _parseWarnings;

  private final @Nonnull SortedSet<Warning> _pedanticWarnings;

  private final @Nonnull SortedSet<Warning> _redFlagWarnings;

  private final @Nonnull SortedSet<Warning> _unimplementedWarnings;

  public static @Nonnull Warnings forLogger(BatfishLogger logger) {
    return new Warnings(
        logger.isActive(LEVEL_PEDANTIC),
        logger.isActive(LEVEL_REDFLAG),
        logger.isActive(LEVEL_UNIMPLEMENTED));
  }

  @JsonCreator
  private static Warnings create(
      @JsonProperty(PROP_PEDANTIC) @Nullable SortedSet<Warning> pedanticWarnings,
      @JsonProperty(PROP_RED_FLAGS) @Nullable SortedSet<Warning> redFlagWarnings,
      @JsonProperty(PROP_UNIMPLEMENTED) @Nullable SortedSet<Warning> unimplementedWarnings,
      @JsonProperty(PROP_PARSE_WARNINGS) @Nullable List<ParseWarning> parseWarnings,
      @JsonProperty(PROP_ERROR_DETAILS) @Nullable ErrorDetails errorDetails) {
    return new Warnings(
        new Settings(false, false, false),
        firstNonNull(pedanticWarnings, new TreeSet<>()),
        firstNonNull(redFlagWarnings, new TreeSet<>()),
        firstNonNull(unimplementedWarnings, new TreeSet<>()),
        firstNonNull(parseWarnings, new LinkedList<>()),
        errorDetails);
  }

  public Warnings() {
    this(new Settings(false, false, false));
  }

  public Warnings(boolean pedanticRecord, boolean redFlagRecord, boolean unimplementedRecord) {
    this(new Settings(pedanticRecord, redFlagRecord, unimplementedRecord));
  }

  public Warnings(Settings settings) {
    this(settings, new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), new LinkedList<>(), null);
  }

  private Warnings(
      Settings settings,
      SortedSet<Warning> pedanticWarnings,
      SortedSet<Warning> redFlagWarnings,
      SortedSet<Warning> unimplementedWarnings,
      List<ParseWarning> parseWarnings,
      @Nullable ErrorDetails errorDetails) {
    _settings = settings;
    _pedanticWarnings = pedanticWarnings;
    _redFlagWarnings = redFlagWarnings;
    _unimplementedWarnings = unimplementedWarnings;
    _parseWarnings = parseWarnings;
    _errorDetails = errorDetails;
  }

  @JsonProperty(PROP_PARSE_WARNINGS)
  public @Nonnull List<ParseWarning> getParseWarnings() {
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
    if (!_settings._pedanticRecord) {
      return;
    }
    pedantic(msg, TAG_PEDANTIC);
  }

  public void pedantic(String msg, String tag) {
    _pedanticWarnings.add(new Warning(msg, tag));
  }

  public void redFlag(String msg) {
    if (!_settings._redFlagRecord) {
      return;
    }
    _redFlagWarnings.add(new Warning(msg, TAG_RED_FLAG));
  }

  @FormatMethod
  public void redFlagf(String format, Object... args) {
    if (!_settings._redFlagRecord) {
      return;
    }
    redFlag(String.format(format, args));
  }

  /** Indicate that this red flag warning is a fatal error */
  @FormatMethod
  public void fatalRedFlag(String msg, Object... args) {
    redFlag(FATAL_FLAG + String.format(msg, args));
  }

  /** Get all red flag warnings that are fatal error */
  @JsonIgnore
  public SortedSet<Warning> getFatalRedFlagWarnings() {
    SortedSet<Warning> fatalWarnings = new TreeSet<>();
    for (Warning warning : _redFlagWarnings) {
      if (warning.getText().startsWith(FATAL_FLAG)) {
        fatalWarnings.add(warning);
      }
    }
    return fatalWarnings;
  }

  /** Get all red flag warnings that are fatal error */
  @JsonIgnore
  public List<ParseWarning> getRiskyParseWarnings() {
    return _parseWarnings.stream()
        .filter(warning -> warning.getComment().startsWith(RISKY_FLAG))
        .collect(ImmutableList.toImmutableList());
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

  /** Wrapper around addWarning for risky warnings */
  public void addRiskyWarning(
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser,
      @Nonnull String comment) {
    addWarning(ctx, line, parser, Warnings.RISKY_FLAG + comment);
  }

  /** Wrapper around addWarningOnLine for risky warnings */
  public void addRiskyWarningOnLine(
      int lineNumber,
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser,
      @Nonnull String comment) {
    addWarningOnLine(lineNumber, ctx, line, parser, Warnings.RISKY_FLAG + comment);
  }

  /**
   * @see #addWarning(ParserRuleContext, String, BatfishCombinedParser, String)
   */
  public void todo(
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser) {
    addWarning(ctx, line, parser, "This feature is not currently supported");
  }

  public void unimplemented(String msg) {
    if (!_settings._unimplementedRecord) {
      return;
    }
    _unimplementedWarnings.add(new Warning(msg, TAG_UNIMPLEMENTED));
  }

  @FormatMethod
  public void unimplementedf(String format, Object... args) {
    if (!_settings._unimplementedRecord) {
      return;
    }
    unimplemented(String.format(format, args));
  }

  /** A class to represent a parse warning in a file. */
  public static final class ParseWarning implements Serializable {

    private static final String PROP_COMMENT = "Comment";
    private static final String PROP_LINE = "Line";
    private static final String PROP_PARSER_CONTEXT = "Parser_Context";
    private static final String PROP_TEXT = "Text";

    private final @Nonnull String _comment;
    private final int _line;
    private final @Nonnull String _parserContext;
    private final @Nonnull String _text;

    @JsonCreator
    private static ParseWarning create(
        @JsonProperty(PROP_LINE) @Nullable Integer line,
        @JsonProperty(PROP_TEXT) @Nullable String text,
        @JsonProperty(PROP_PARSER_CONTEXT) @Nullable String parserContext,
        @JsonProperty(PROP_COMMENT) @Nullable String comment) {
      checkArgument(line != null, "Missing %s", PROP_LINE);
      // empty strings can get serialized as nulls
      return new ParseWarning(
          line, firstNonNull(text, ""), firstNonNull(parserContext, ""), firstNonNull(comment, ""));
    }

    public ParseWarning(
        int line, @Nonnull String text, @Nonnull String parserContext, @Nonnull String comment) {
      _line = line;
      _text = requireNonNull(text, PROP_TEXT);
      _parserContext = requireNonNull(parserContext, PROP_PARSER_CONTEXT);
      _comment = requireNonNull(comment, PROP_COMMENT);
    }

    @JsonProperty(PROP_COMMENT)
    public @Nonnull String getComment() {
      return _comment;
    }

    @JsonProperty(PROP_LINE)
    public int getLine() {
      return _line;
    }

    @JsonProperty(PROP_PARSER_CONTEXT)
    public @Nonnull String getParserContext() {
      return _parserContext;
    }

    @JsonProperty(PROP_TEXT)
    public @Nonnull String getText() {
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
          .add("line", _line)
          .add("text", _text)
          .add("comment", _comment)
          .add("parserContext", _parserContext)
          .toString();
    }
  }
}
