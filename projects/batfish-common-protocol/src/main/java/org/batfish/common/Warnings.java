package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;

@JsonSerialize(using = Warnings.Serializer.class)
@JsonDeserialize(using = Warnings.Deserializer.class)
public class Warnings implements Serializable {

  public static class Deserializer extends JsonDeserializer<Warnings> {

    @Override
    public Warnings deserialize(JsonParser parser, DeserializationContext ctx)
        throws IOException, JsonProcessingException {
      JsonNode node = parser.getCodec().readTree(parser);
      Warnings warnings = new Warnings();
      if (node.has(PEDANTIC_VAR)) {
        JsonNode warningsNode = node.get(PEDANTIC_VAR);
        fillWarningList(warnings._pedanticWarnings, warningsNode);
      }
      if (node.has(RED_FLAGS_VAR)) {
        JsonNode warningsNode = node.get(RED_FLAGS_VAR);
        fillWarningList(warnings._redFlagWarnings, warningsNode);
      }
      if (node.has(UNIMPLEMENTED_VAR)) {
        JsonNode warningsNode = node.get(UNIMPLEMENTED_VAR);
        fillWarningList(warnings._unimplementedWarnings, warningsNode);
      }
      return warnings;
    }

    private void fillWarningList(List<Warning> warnings, JsonNode node) {
      for (Iterator<Entry<String, JsonNode>> iter = node.fields(); iter.hasNext(); ) {
        Entry<String, JsonNode> e = iter.next();
        String msg = e.getValue().asText();
        int colonIndex = msg.indexOf(":");
        String tag = msg.substring(0, colonIndex);
        String text = msg.substring(colonIndex + 2, msg.length());
        Warning warning = new Warning(text, tag);
        warnings.add(warning);
      }
    }
  }

  public static class Serializer extends JsonSerializer<Warnings> {

    @Override
    public void serialize(Warnings value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      if (!value._pedanticWarnings.isEmpty()) {
        jgen.writeFieldName(PEDANTIC_VAR);
        jgen.writeStartObject();
        for (int i = 0; i < value._pedanticWarnings.size(); i++) {
          Warning taggedWarning = value._pedanticWarnings.get(i);
          String text = taggedWarning.getFirst();
          String tag = taggedWarning.getSecond();
          String msg = tag + ": " + text;
          jgen.writeFieldName(Integer.toString(i + 1));
          jgen.writeString(msg);
        }
        jgen.writeEndObject();
      }
      if (!value._redFlagWarnings.isEmpty()) {
        jgen.writeFieldName(RED_FLAGS_VAR);
        jgen.writeStartObject();
        for (int i = 0; i < value._redFlagWarnings.size(); i++) {
          Warning taggedWarning = value._redFlagWarnings.get(i);
          String text = taggedWarning.getFirst();
          String tag = taggedWarning.getSecond();
          String msg = tag + ": " + text;
          jgen.writeFieldName(Integer.toString(i + 1));
          jgen.writeString(msg);
        }
        jgen.writeEndObject();
      }
      if (!value._unimplementedWarnings.isEmpty()) {
        jgen.writeFieldName(UNIMPLEMENTED_VAR);
        jgen.writeStartObject();
        for (int i = 0; i < value._unimplementedWarnings.size(); i++) {
          Warning taggedWarning = value._unimplementedWarnings.get(i);
          String text = taggedWarning.getFirst();
          String tag = taggedWarning.getSecond();
          String msg = tag + ": " + text;
          jgen.writeFieldName(Integer.toString(i + 1));
          jgen.writeString(msg);
        }
        jgen.writeEndObject();
      }
      jgen.writeEndObject();
    }
  }

  private static final String MISCELLANEOUS = "MISCELLANEOUS";

  private static final String PEDANTIC_VAR = "Pedantic complaints";

  private static final String RED_FLAGS_VAR = "Red flags";

  /** */
  private static final long serialVersionUID = 1L;

  private static final String UNIMPLEMENTED_VAR = "Unimplemented features";

  private transient boolean _pedanticAsError;

  private transient boolean _pedanticRecord;

  protected final List<Warning> _pedanticWarnings;

  private transient boolean _printParseTree;

  private transient boolean _redFlagAsError;

  private transient boolean _redFlagRecord;

  protected final List<Warning> _redFlagWarnings;

  private transient boolean _unimplementedAsError;

  private transient boolean _unimplementedRecord;

  protected final List<Warning> _unimplementedWarnings;

  public Warnings() {
    _pedanticWarnings = new ArrayList<>();
    _redFlagWarnings = new ArrayList<>();
    _unimplementedWarnings = new ArrayList<>();
  }

  public Warnings(
      boolean pedanticAsError,
      boolean pedanticRecord,
      boolean redFlagAsError,
      boolean redFlagRecord,
      boolean unimplementedAsError,
      boolean unimplementedRecord,
      boolean printParseTree) {
    this();
    _pedanticAsError = pedanticAsError;
    _pedanticRecord = pedanticRecord;
    _printParseTree = printParseTree;
    _redFlagAsError = redFlagAsError;
    _redFlagRecord = redFlagRecord;
    _unimplementedAsError = unimplementedAsError;
    _unimplementedRecord = unimplementedRecord;
  }

  public List<Warning> getPedanticWarnings() {
    return _pedanticWarnings;
  }

  public List<Warning> getRedFlagWarnings() {
    return _redFlagWarnings;
  }

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
    pedantic(msg, MISCELLANEOUS);
  }

  public void pedantic(String msg, String tag) {
    if (_pedanticAsError) {
      throw new PedanticBatfishException(msg);
    } else if (_pedanticRecord) {
      _pedanticWarnings.add(new Warning(msg, tag));
    }
  }

  public void redFlag(String msg) {
    redFlag(msg, MISCELLANEOUS);
  }

  public void redFlag(String msg, String tag) {
    if (_redFlagAsError) {
      throw new RedFlagBatfishException(msg);
    } else if (_redFlagRecord) {
      _redFlagWarnings.add(new Warning(msg, tag));
    }
  }

  public void todo(
      ParserRuleContext ctx, String feature, BatfishCombinedParser<?, ?> parser, String text) {
    if (!_unimplementedRecord && !_unimplementedAsError) {
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
    String[] ruleTextLines = ruleText.split("\\n");
    for (int line = startLine, i = 0; i < ruleTextLines.length; line++, i++) {
      String contextPrefix = prefix + " line " + line + ": ";
      sb.append(contextPrefix + ruleTextLines[i] + "\n");
    }
    if (_printParseTree) {
      sb.append(prefix + "Parse tree follows:\n");
      String parseTreePrefix = prefix + "PARSE TREE: ";
      String parseTreeText = ParseTreePrettyPrinter.print(ctx, parser);
      String[] parseTreeLines = parseTreeText.split("\n");
      for (String parseTreeLine : parseTreeLines) {
        sb.append(parseTreePrefix + parseTreeLine + "\n");
      }
    }
    String warning = sb.toString();
    if (_unimplementedAsError) {
      throw new UnimplementedBatfishException(warning);
    } else {
      _unimplementedWarnings.add(new Warning(sb.toString(), "UNIMPLEMENTED"));
    }
  }

  public void unimplemented(String msg) {
    unimplemented(msg, MISCELLANEOUS);
  }

  public void unimplemented(String msg, String tag) {
    if (_unimplementedAsError) {
      throw new UnimplementedBatfishException(msg);
    } else if (_unimplementedRecord) {
      _unimplementedWarnings.add(new Warning(msg, tag));
    }
  }
}
