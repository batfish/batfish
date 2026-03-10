package org.batfish.datamodel.answers;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.trace.TraceTree;

public class Schema {

  public enum Type {
    BASE,
    LIST,
    SET
  }

  private static final Pattern LIST_PATTERN = Pattern.compile("^List<(.+)>$");

  private static final Pattern SET_PATTERN = Pattern.compile("^Set<(.+)>$");

  private static String getClassString(Class<?> cls) {
    return String.format("class:%s", cls.getCanonicalName());
  }

  private static final Map<String, String> schemaAliases =
      ImmutableMap.<String, String>builder()
          .put("BgpRoute", getClassString(BgpRoute.class))
          .put("BgpRouteDiffs", getClassString(BgpRouteDiffs.class))
          .put("Boolean", getClassString(Boolean.class))
          .put("Double", getClassString(Double.class))
          .put("FileLines", getClassString(FileLines.class))
          .put("Flow", getClassString(Flow.class))
          .put("Integer", getClassString(Integer.class))
          .put("Interface", getClassString(NodeInterfacePair.class))
          .put("Ip", getClassString(Ip.class))
          .put("Issue", getClassString(Issue.class))
          .put("Long", getClassString(Long.class))
          .put("NextHop", getClassString(NextHop.class))
          .put("NextHopResult", getClassString(NextHopResult.class))
          .put("Object", getClassString(Object.class))
          .put("Node", getClassString(Node.class))
          .put("Prefix", getClassString(Prefix.class))
          .put("SelfDescribing", getClassString(SelfDescribingObject.class))
          .put("String", getClassString(String.class))
          .put("Trace", getClassString(Trace.class))
          .put("TraceTree", getClassString(TraceTree.class))
          .build();

  public static final Schema BGP_ROUTE = new Schema("BgpRoute");
  public static final Schema BGP_ROUTE_DIFFS = new Schema("BgpRouteDiffs");
  public static final Schema BOOLEAN = new Schema("Boolean");
  public static final Schema DOUBLE = new Schema("Double");
  public static final Schema FILE_LINES = new Schema("FileLines");
  public static final Schema FLOW = new Schema("Flow");
  public static final Schema INTEGER = new Schema("Integer");
  public static final Schema INTERFACE = new Schema("Interface");
  public static final Schema IP = new Schema("Ip");
  public static final Schema ISSUE = new Schema("Issue");
  public static final Schema LONG = new Schema("Long");
  public static final Schema NEXT_HOP = new Schema("NextHop");
  public static final Schema NEXT_HOP_RESULT = new Schema("NextHopResult");
  public static final Schema NODE = new Schema("Node");
  public static final Schema OBJECT = new Schema("Object");
  public static final Schema PREFIX = new Schema("Prefix");
  public static final Schema SELF_DESCRIBING = new Schema("SelfDescribing");
  public static final Schema STRING = new Schema("String");
  public static final Schema TRACE = new Schema("Trace");
  public static final Schema TRACE_TREE = new Schema("TraceTree");

  /** Generates a list Schema with the give base schema */
  public static final Schema list(Schema baseSchema) {
    return new Schema("List<" + baseSchema._schemaStr + ">");
  }

  /** Generates a set Schema from the give base schema */
  public static final Schema set(Schema baseSchema) {
    return new Schema("Set<" + baseSchema._schemaStr + ">");
  }

  /** Captures what this Schema finally contains after levels of nesting */
  private final @Nonnull Class<?> _baseType;

  /** For list/set types this field represents what is inside; for base types it is null */
  private final @Nullable Schema _innerSchema;

  /** The string representaion from which this Schema was derived; kept around for printing */
  private final @Nonnull String _schemaStr;

  /** Is this Schema a list, set, or base? */
  private final @Nonnull Type _type;

  @JsonCreator
  Schema(String schema) {

    // base case
    Schema innerSchema = null;
    Type type = Type.BASE;
    Class<?> baseType = null;

    // list case
    Matcher listMatcher = LIST_PATTERN.matcher(schema);
    if (listMatcher.find()) {
      innerSchema = new Schema(listMatcher.group(1));
      type = Type.LIST;
      baseType = innerSchema._baseType;
    }

    // set case
    Matcher setMatcher = SET_PATTERN.matcher(schema);
    if (setMatcher.find()) {
      innerSchema = new Schema(setMatcher.group(1));
      type = Type.SET;
      baseType = innerSchema._baseType;
    }

    if (type == Type.BASE) {
      if (!schemaAliases.containsKey(schema)) {
        throw new BatfishException("Unknown schema type: " + schema);
      }

      String baseTypeName = schemaAliases.get(schema);

      checkArgument(
          baseTypeName.startsWith("class:"),
          "Only class-based schemas are supported. Got " + baseTypeName);

      baseTypeName = baseTypeName.substring("class:".length());

      try {
        baseType = Class.forName(baseTypeName);
      } catch (ClassNotFoundException e) {
        throw new BatfishException("Could not get a class from " + baseTypeName);
      }
    }

    _innerSchema = innerSchema;
    _type = type;
    _schemaStr = schema;
    _baseType = baseType;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Schema)) {
      return false;
    }
    return Objects.equals(_baseType, ((Schema) o)._baseType)
        && Objects.equals(_innerSchema, ((Schema) o)._innerSchema)
        && Objects.equals(_type, ((Schema) o)._type);
  }

  public Class<?> getBaseType() {
    return _baseType;
  }

  public @Nullable Schema getInnerSchema() {
    return _innerSchema;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_baseType, _innerSchema, _type);
  }

  /** Whether this Schema is List or Set */
  public boolean isCollection() {
    return _type == Type.LIST || _type == Type.SET;
  }

  /**
   * Whether this Schema object ultimately resolves to an Integer-based (base, list, or set)
   *
   * <p>TODO: Get rid of this call; clients can just do this logic on their end.
   */
  public boolean isIntBased() {
    return _baseType.equals(Integer.class);
  }

  @Override
  @JsonValue
  public String toString() {
    return _schemaStr;
  }
}
