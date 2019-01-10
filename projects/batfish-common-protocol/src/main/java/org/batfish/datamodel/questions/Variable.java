package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;

public class Variable {

  public enum Type {
    ANSWER_ELEMENT("answerElement", true),
    BGP_PEER_PROPERTY_SPEC("bgpPeerPropertySpec", true),
    BGP_PROCESS_PROPERTY_SPEC("bgpProcessPropertySpec", true),
    BOOLEAN("boolean", false),
    COMPARATOR("comparator", true),
    DOUBLE("double", false),
    DISPOSITION_SPEC("dispositionSpec", true),
    FLOAT("float", false),
    HEADER_CONSTRAINT("headerConstraint", false),
    INTEGER("integer", false),
    INTERFACE_PROPERTY_SPEC("interfacePropertySpec", true),
    IP("ip", true),
    IP_PROTOCOL("ipProtocol", true),
    IP_WILDCARD("ipWildcard", true),
    JAVA_REGEX("javaRegex", true),
    JSON_PATH("jsonPath", true),
    JSON_PATH_REGEX("jsonPathRegex", true),
    LONG("long", false),
    NAMED_STRUCTURE_SPEC("namedStructureSpec", true),
    NODE_PROPERTY_SPEC("nodePropertySpec", true),
    NODE_SPEC("nodeSpec", true),
    OSPF_PROPERTY_SPEC("ospfPropertySpec", true),
    PATH_CONSTRAINT("pathConstraint", true),
    PREFIX("prefix", true),
    PREFIX_RANGE("prefixRange", true),
    PROTOCOL("protocol", true),
    QUESTION("question", true),
    STRING("string", true),
    SUBRANGE("subrange", true);

    private static final Map<String, Variable.Type> MAP = initMap();

    @JsonCreator
    public static Variable.Type fromString(String name) {
      Variable.Type value = MAP.get(name.toLowerCase());
      if (value == null) {
        throw new BatfishException(
            "No " + Variable.Type.class.getSimpleName() + " with name: '" + name + "'");
      }
      return value;
    }

    private static Map<String, Variable.Type> initMap() {
      ImmutableMap.Builder<String, Variable.Type> map = ImmutableMap.builder();
      for (Variable.Type value : Type.values()) {
        String name = value._name.toLowerCase();
        map.put(name, value);
      }
      return map.build();
    }

    private final String _name;

    private final boolean _stringType;

    Type(String name, boolean stringType) {
      _name = name;
      _stringType = stringType;
    }

    @JsonValue
    public String getName() {
      return _name;
    }

    public boolean getStringType() {
      return _stringType;
    }
  }

  private String _description;

  private String _displayName;

  private Map<String, Field> _fields;

  private String _longDescription;

  private Integer _minElements;

  private Integer _minLength;

  private boolean _optional;

  private Variable.Type _type;

  private JsonNode _value;

  private List<AllowedValue> _values;

  public Variable() {
    _values = new ArrayList<>();
  }

  @JsonProperty(BfConsts.PROP_ALLOWED_VALUES)
  @Deprecated
  public SortedSet<String> getAllowedValues() {
    return _values
        .stream()
        .map(AllowedValue::getName)
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  @JsonProperty(BfConsts.PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(BfConsts.PROP_DISPLAY_NAME)
  public @Nullable String getDisplayName() {
    return _displayName;
  }

  @JsonProperty(BfConsts.PROP_FIELDS)
  public @Nullable Map<String, Field> getFields() {
    return _fields;
  }

  @JsonProperty(BfConsts.PROP_LONG_DESCRIPTION)
  public String getLongDescription() {
    return _longDescription;
  }

  @JsonProperty(BfConsts.PROP_MIN_ELEMENTS)
  public Integer getMinElements() {
    return _minElements;
  }

  @JsonProperty(BfConsts.PROP_MIN_LENGTH)
  public Integer getMinLength() {
    return _minLength;
  }

  @JsonProperty(BfConsts.PROP_OPTIONAL)
  public boolean getOptional() {
    return _optional;
  }

  @JsonProperty(BfConsts.PROP_TYPE)
  public Variable.Type getType() {
    return _type;
  }

  @JsonProperty(BfConsts.PROP_VALUE)
  @JsonInclude(Include.NON_NULL)
  public JsonNode getValue() {
    return _value;
  }

  @JsonProperty(BfConsts.PROP_VALUES)
  public List<AllowedValue> getValues() {
    return _values;
  }

  @JsonProperty(BfConsts.PROP_ALLOWED_VALUES)
  @Deprecated
  public void setAllowedValues(SortedSet<String> allowedValues) {
    if (_values.isEmpty()) {
      _values =
          allowedValues
              .stream()
              .map(v -> new AllowedValue(v, null))
              .collect(ImmutableList.toImmutableList());
    }
  }

  @JsonProperty(BfConsts.PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(BfConsts.PROP_DISPLAY_NAME)
  public void setDisplayName(@Nullable String displayName) {
    _displayName = displayName;
  }

  @JsonProperty(BfConsts.PROP_FIELDS)
  public void setFields(@Nullable Map<String, Field> fields) {
    _fields = fields;
  }

  @JsonProperty(BfConsts.PROP_LONG_DESCRIPTION)
  public void setLongDescription(String longDescription) {
    _longDescription = longDescription;
  }

  @JsonProperty(BfConsts.PROP_MIN_ELEMENTS)
  public void setMinElements(Integer minElements) {
    _minElements = minElements;
  }

  @JsonProperty(BfConsts.PROP_MIN_LENGTH)
  public void setMinLength(Integer minLength) {
    _minLength = minLength;
  }

  @JsonProperty(BfConsts.PROP_OPTIONAL)
  public void setOptional(boolean optional) {
    _optional = optional;
  }

  @JsonProperty(BfConsts.PROP_TYPE)
  public void setType(Variable.Type type) {
    _type = type;
  }

  @JsonProperty(BfConsts.PROP_VALUE)
  public void setValue(JsonNode value) {
    if (value != null && value.isNull()) {
      _value = null;
    } else {
      _value = value;
    }
  }

  @JsonProperty(BfConsts.PROP_VALUES)
  public void setValues(List<AllowedValue> values) {
    _values = values;
  }
}
