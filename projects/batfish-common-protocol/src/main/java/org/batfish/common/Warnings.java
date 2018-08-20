package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;

public class Warnings implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String MISCELLANEOUS = "MISCELLANEOUS";

  private static final String PROP_PARSE_WARNINGS = "Parse warnings";

  private static final String PROP_PEDANTIC = "Pedantic complaints";

  private static final String PROP_RED_FLAGS = "Red flags";

  private static final String PROP_UNIMPLEMENTED = "Unimplemented features";

  @Nonnull private final List<ParseWarning> _parseWarnings;

  private transient boolean _pedanticRecord;

  private final List<Warning> _pedanticWarnings;

  private transient boolean _redFlagRecord;

  private final List<Warning> _redFlagWarnings;

  private transient boolean _unimplementedRecord;

  private final List<Warning> _unimplementedWarnings;

  @JsonCreator
  private Warnings(
      @Nullable @JsonProperty(PROP_PEDANTIC) List<Warning> pedanticWarnings,
      @Nullable @JsonProperty(PROP_RED_FLAGS) List<Warning> redFlagWarnings,
      @Nullable @JsonProperty(PROP_UNIMPLEMENTED) List<Warning> unimplementedWarnings,
      @Nullable @JsonProperty(PROP_PARSE_WARNINGS) List<ParseWarning> parseWarnings) {
    _pedanticWarnings = firstNonNull(pedanticWarnings, new LinkedList<>());
    _redFlagWarnings = firstNonNull(redFlagWarnings, new LinkedList<>());
    _parseWarnings = firstNonNull(parseWarnings, new LinkedList<>());
    _unimplementedWarnings = firstNonNull(unimplementedWarnings, new LinkedList<>());
  }

  public Warnings() {
    this(false, false, false);
  }

  public Warnings(boolean pedanticRecord, boolean redFlagRecord, boolean unimplementedRecord) {
    this(null, null, null, null);
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
  public List<Warning> getPedanticWarnings() {
    return _pedanticWarnings;
  }

  @JsonProperty(PROP_RED_FLAGS)
  public List<Warning> getRedFlagWarnings() {
    return _redFlagWarnings;
  }

  @JsonProperty(PROP_UNIMPLEMENTED)
  public List<Warning> getUnimplementedWarnings() {
    return _unimplementedWarnings;
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
    pedantic(msg, MISCELLANEOUS);
  }

  public void pedantic(String msg, String tag) {
    _pedanticWarnings.add(new Warning(msg, tag));
  }

  public void redFlag(String msg) {
    redFlag(msg, MISCELLANEOUS);
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
   */
  public void todo(
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser,
      @Nullable String comment) {
    int lineNumber = ctx.getStart().getLine();
    String ruleStack = ctx.toString(Arrays.asList(parser.getParser().getRuleNames()));
    String trimmedLine = line.trim();
    String commentMessage = firstNonNull(comment, "This feature is not currently supported");
    _parseWarnings.add(new ParseWarning(lineNumber, trimmedLine, ruleStack, commentMessage));
  }

  /** @see #todo(ParserRuleContext, String, BatfishCombinedParser, String) */
  public void todo(
      @Nonnull ParserRuleContext ctx,
      @Nonnull String line,
      @Nonnull BatfishCombinedParser<?, ?> parser) {
    todo(ctx, line, parser, /* no comment */ null);
  }

  public void unimplemented(String msg) {
    if (!_unimplementedRecord) {
      return;
    }
    _unimplementedWarnings.add(new Warning(msg, "UNIMPLEMENTED"));
  }

  /** A class to represent a parse warning in a file. */
  public static class ParseWarning implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String PROP_COMMENT = "Comment";
    private static final String PROP_LINE = "Line";
    private static final String PROP_PARSER_CONTEXT = "Parser_Context";
    private static final String PROP_TEXT = "Text";

    @Nullable private final String _comment;
    private final int _line;
    @Nonnull private final String _parserContext;
    @Nonnull private final String _text;

    @JsonCreator
    public ParseWarning(
        @JsonProperty(PROP_LINE) int line,
        @JsonProperty(PROP_TEXT) @Nonnull String text,
        @JsonProperty(PROP_PARSER_CONTEXT) @Nonnull String parserContext,
        @JsonProperty(PROP_COMMENT) @Nullable String comment) {
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
  }
}
