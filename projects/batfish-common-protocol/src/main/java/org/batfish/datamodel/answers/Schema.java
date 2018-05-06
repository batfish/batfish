package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.FileLinePair;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.pojo.Node;

public class Schema {

  private static String getClassString(Class<?> cls) {
    return String.format("class:%s", cls.getCanonicalName());
  }

  private static final Map<String, String> schemaAliases =
      ImmutableMap.<String, String>builder()
          .put("Boolean", getClassString(Boolean.class))
          .put("Environment", getClassString(Environment.class))
          .put("FileLine", getClassString(FileLinePair.class))
          .put("Flow", getClassString(Flow.class))
          .put("FlowTrace", getClassString(FlowTrace.class))
          .put("Integer", getClassString(Long.class))
          .put("Interface", getClassString(NodeInterfacePair.class))
          .put("Ip", getClassString(Ip.class))
          .put("Issue", getClassString(Issue.class))
          .put("Object", getClassString(Object.class))
          .put("Node", getClassString(Node.class))
          .put("Prefix", getClassString(Prefix.class))
          .put("String", getClassString(String.class))
          .build();

  public static final Schema BOOLEAN = new Schema("Boolean");
  public static final Schema ENVIRONMENT = new Schema("Environment");
  public static final Schema FILE_LINE = new Schema("FileLine");
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

  public static final Schema list(Schema baseSchema) {
    return new Schema("List<" + baseSchema._schemaStr + ">");
  }

  private Class<?> _baseType;

  private boolean _isListType;

  private String _schemaStr;

  @JsonCreator
  private Schema(String schema) {
    _schemaStr = schema;

    String baseTypeName = schema;
    _isListType = false;

    Matcher matcher = Pattern.compile("List<(.+)>").matcher(schema);
    if (matcher.find()) {
      baseTypeName = matcher.group(1);
      _isListType = true;
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

  public Class<?> getBaseType() {
    return _baseType;
  }

  public boolean isList() {
    return _isListType;
  }

  @Override
  @JsonValue
  public String toString() {
    return _schemaStr;
  }

  public boolean isIntOrIntList() {
    return _baseType.equals(Long.class);
  }
}
