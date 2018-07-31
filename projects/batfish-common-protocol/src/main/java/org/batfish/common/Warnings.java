package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;

public class Warnings implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String MISCELLANEOUS = "MISCELLANEOUS";

  private static final String PROP_PEDANTIC = "Pedantic complaints";

  private static final String PROP_RED_FLAGS = "Red flags";

  private static final String PROP_UNIMPLEMENTED = "Unimplemented features";

  private transient boolean _pedanticRecord;

  private final List<Warning> _pedanticWarnings;

  private transient boolean _printParseTree;

  private transient boolean _redFlagRecord;

  private final List<Warning> _redFlagWarnings;

  private transient boolean _unimplementedRecord;

  private final List<Warning> _unimplementedWarnings;

  @JsonCreator
  public Warnings(
      @Nullable @JsonProperty(PROP_PEDANTIC) List<Warning> pedanticWarnings,
      @Nullable @JsonProperty(PROP_RED_FLAGS) List<Warning> redFlagWarnings,
      @Nullable @JsonProperty(PROP_UNIMPLEMENTED) List<Warning> unimplementedWarnings) {
    _pedanticWarnings = firstNonNull(pedanticWarnings, new LinkedList<>());
    _redFlagWarnings = firstNonNull(redFlagWarnings, new LinkedList<>());
    _unimplementedWarnings = firstNonNull(unimplementedWarnings, new LinkedList<>());
  }

  public Warnings(
      boolean pedanticRecord,
      boolean redFlagRecord,
      boolean unimplementedRecord,
      boolean printParseTree) {
    this(null, null, null);
    _pedanticRecord = pedanticRecord;
    _printParseTree = printParseTree;
    _redFlagRecord = redFlagRecord;
    _unimplementedRecord = unimplementedRecord;
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
        && _unimplementedWarnings.isEmpty();
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

  public void todo(
      ParserRuleContext ctx, String feature, BatfishCombinedParser<?, ?> parser, String text) {
    if (!_unimplementedRecord) {
      return;
    }
    String prefix = "WARNING: UNIMPLEMENTED: " + (_unimplementedWarnings.size() + 1) + ": ";
    StringBuilder sb = new StringBuilder();
    List<String> ruleNames = Arrays.asList(parser.getParser().getRuleNames());
    String ruleStack = ctx.toString(ruleNames);
    sb.append(
        prefix
            + "Missing implementation for top (leftmost) parser rule in stack: '"
            + ruleStack
            + "'.\n");
    sb.append(prefix + "Unimplemented feature: " + feature + "\n");
    sb.append(prefix + "Rule context follows:\n");
    int start = ctx.start.getStartIndex();
    int startLine = ctx.start.getLine();
    int end = ctx.stop.getStopIndex();
    String ruleText = text.substring(start, end + 1);
    String[] ruleTextLines = ruleText.split("\\n", -1);
    for (int line = startLine, i = 0; i < ruleTextLines.length; line++, i++) {
      String contextPrefix = prefix + " line " + line + ": ";
      sb.append(contextPrefix + ruleTextLines[i] + "\n");
    }
    if (_printParseTree) {
      sb.append(prefix + "Parse tree follows:\n");
      String parseTreePrefix = prefix + "PARSE TREE: ";
      String parseTreeText = ParseTreePrettyPrinter.print(ctx, parser);
      String[] parseTreeLines = parseTreeText.split("\n", -1);
      for (String parseTreeLine : parseTreeLines) {
        sb.append(parseTreePrefix + parseTreeLine + "\n");
      }
    }
    _unimplementedWarnings.add(new Warning(sb.toString(), "UNIMPLEMENTED"));
  }

  public void unimplemented(String msg) {
    unimplemented(msg, MISCELLANEOUS);
  }

  public void unimplemented(String msg, String tag) {
    if (!_unimplementedRecord) {
      return;
    }
    _unimplementedWarnings.add(new Warning(msg, tag));
  }
}
