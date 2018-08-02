package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclTrace;
import org.batfish.datamodel.collections.FileLinePair;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.pojo.Node;

public class Schema {

  public enum Type {
    BASE,
    LIST,
    SET,
    OBJECT,
    TREESET,
    TREEMAP
  }

  private static final Pattern LIST_PATTERN = Pattern.compile("List<(.+)>");

  private static final Pattern SET_PATTERN = Pattern.compile("Set<(.+)>");

  private static String getClassString(Class<?> cls) {
    return String.format("class:%s", cls.getCanonicalName());
  }

  private static final Map<String, String> schemaAliases =
      ImmutableMap.<String, String>builder()
          .put("AclTrace", getClassString(AclTrace.class))
          .put("Boolean", getClassString(Boolean.class))
          .put("Double", getClassString(Double.class))
          .put("Environment", getClassString(Environment.class))
          .put("FileLine", getClassString(FileLinePair.class))
          .put("FileLines", getClassString(FileLines.class))
          .put("Flow", getClassString(Flow.class))
          .put("FlowTrace", getClassString(FlowTrace.class))
          .put("Integer", getClassString(Integer.class))
          .put("Interface", getClassString(NodeInterfacePair.class))
          .put("Ip", getClassString(Ip.class))
          .put("Issue", getClassString(Issue.class))
          .put("Object", getClassString(Object.class))
          .put("Node", getClassString(Node.class))
          .put("Prefix", getClassString(Prefix.class))
          .put("String", getClassString(String.class))
          .build();

  public static final Schema ACL_TRACE = new Schema("AclTrace");
  public static final Schema BOOLEAN = new Schema("Boolean");
  public static final Schema DOUBLE = new Schema("Double");
  public static final Schema ENVIRONMENT = new Schema("Environment");
  public static final Schema FILE_LINE = new Schema("FileLine");
  public static final Schema FILE_LINES = new Schema("FileLines");
  public static final Schema FLOW = new Schema("Flow");
  public static final Schema FLOW_TRACE = new Schema("FlowTrace");
  public static final Schema INTEGER = new Schema("Integer");
  public static final Schema INTERFACE = new Schema("Interface");
  public static final Schema IP = new Schema("Ip");
  public static final Schema ISSUE = new Schema("Issue");
  public static final Schema OBJECT = new Schema("Object");
  public static final Schema NODE = new Schema("Node");
  public static final Schema PREFIX = new Schema("Prefix");
  public static final Schema STRING = new Schema("String");

  /** Generates a list Schema with the give base schema */
  public static final Schema list(Schema baseSchema) {
    return new Schema("List<" + baseSchema._schemaStr + ">");
  }

  /** Generates a set Schema from the give base schema */
  public static final Schema set(Schema baseSchema) {
    return new Schema("Set<" + baseSchema._schemaStr + ">");
  }

  private Class<?> _baseType;

  private Type _type;

  private String _schemaStr;

  @JsonCreator
  Schema(String schema) {
    _schemaStr = schema;

    String baseTypeName = schema;
    _type = Type.BASE;

    Matcher listMatcher = LIST_PATTERN.matcher(schema);
    if (listMatcher.find()) {
      baseTypeName = listMatcher.group(1);
      _type = Type.LIST;
    }

    Matcher setMatcher = SET_PATTERN.matcher(schema);
    if (setMatcher.find()) {
      baseTypeName = setMatcher.group(1);
      _type = Type.SET;
    }

    if (!schemaAliases.containsKey(baseTypeName)) {
      throw new BatfishException("Unknown schema type: " + baseTypeName);
    }

    baseTypeName = schemaAliases.get(baseTypeName);

    if (!baseTypeName.startsWith("class:")) {
      throw new BatfishException("Only class-based schemas are supported. Got " + baseTypeName);
    }

    baseTypeName = baseTypeName.replaceFirst("class:", "");

    try {
      _baseType = Class.forName(baseTypeName);
    } catch (ClassNotFoundException e) {
      throw new BatfishException("Could not get a class from " + baseTypeName);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Schema)) {
      return false;
    }
    return Objects.equals(_baseType, ((Schema) o)._baseType)
        && Objects.equals(_type, ((Schema) o)._type);
  }

  public Class<?> getBaseType() {
    return _baseType;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_baseType, _type);
  }

  /** Whether this Schema object is Integer-based (base, list, or set) */
  public boolean isIntBased() {
    return _baseType.equals(Integer.class);
  }

  @Override
  @JsonValue
  public String toString() {
    return _schemaStr;
  }
}
