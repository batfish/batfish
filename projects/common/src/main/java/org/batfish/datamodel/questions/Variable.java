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
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.datamodel.answers.AutocompleteSuggestion.CompletionType;

public class Variable {

  public enum Type {
    // deprecated -- left for now for backward compatibility
    ADDRESS_GROUP_AND_BOOK("addressGroupAndBook", true),
    ADDRESS_GROUP_NAME("addressGroupName", true),
    ANSWER_ELEMENT("answerElement", true),
    APPLICATION_SPEC("applicationSpec", true),
    BGP_PEER_PROPERTY_SPEC("bgpPeerPropertySpec", true),
    BGP_PROCESS_PROPERTY_SPEC("bgpProcessPropertySpec", true),
    BGP_ROUTES("bgpRoutes", false),
    BGP_ROUTE_CONSTRAINTS("bgpRouteConstraints", false),
    BGP_ROUTE_STATUS_SPEC("bgpRouteStatusSpec", true),
    BGP_SESSION_COMPAT_STATUS_SPEC("bgpSessionCompatStatusSpec", true),
    BGP_SESSION_PROPERTIES("bgpSessionProperties", false),
    BGP_SESSION_STATUS_SPEC("bgpSessionStatusSpec", true),
    BGP_SESSION_TYPE_SPEC("bgpSessionTypeSpec", true),
    BOOLEAN("boolean", false),
    COMPARATOR("comparator", true),
    DOUBLE("double", false),
    DISPOSITION_SPEC("dispositionSpec", true),
    FILTER("filter", true),
    FILTER_NAME("filterName", true),
    FILTER_SPEC("filterSpec", true),
    FLOAT("float", false),
    HEADER_CONSTRAINT("headerConstraint", false),
    INTEGER("integer", false),
    INTEGER_SPACE("integerSpace", true),
    INTERFACE("interface", true),
    // deprecated -- left for now for backward compatibility
    INTERFACE_GROUP_AND_BOOK("interfaceGroupAndBook", true),
    INTERFACE_GROUP_NAME("interfaceGroupName", true),
    INTERFACE_NAME("interfaceName", true),
    INTERFACE_PROPERTY_SPEC("interfacePropertySpec", true),
    INTERFACES_SPEC("interfacesSpec", true),
    INTERFACE_TYPE("interfaceType", true),
    IP("ip", true),
    IP_PROTOCOL("ipProtocol", true),
    IP_PROTOCOL_SPEC("ipProtocolSpec", true),
    IP_SPACE_SPEC("ipSpaceSpec", true),
    IP_WILDCARD("ipWildcard", true),
    IPSEC_SESSION_STATUS_SPEC("ipsecSessionStatusSpec", true),
    JAVA_REGEX("javaRegex", true),
    JSON_PATH("jsonPath", true),
    JSON_PATH_REGEX("jsonPathRegex", true),
    LOCATION_SPEC("locationSpec", true),
    LONG("long", false),
    MLAG_ID("mlagId", true),
    MLAG_ID_SPEC("mlagIdSpec", true),
    NAMED_STRUCTURE_SPEC("namedStructureSpec", true),
    NODE_PROPERTY_SPEC("nodePropertySpec", true),
    NODE_NAME("nodeName", true),
    // deprecated -- left for now for backward compatibility
    NODE_ROLE_AND_DIMENSION("nodeRoleAndDimension", true),
    // deprecated -- left for now for backward compatibility
    NODE_ROLE_DIMENSION("nodeRoleDimension", true),
    NODE_ROLE_DIMENSION_NAME("nodeRoleDimensionName", true),
    NODE_ROLE_NAME("nodeRoleName", true),
    NODE_SPEC("nodeSpec", true),
    OSPF_INTERFACE_PROPERTY_SPEC("ospfInterfacePropertySpec", true),
    OSPF_PROCESS_PROPERTY_SPEC("ospfProcessPropertySpec", true),
    OSPF_SESSION_STATUS_SPEC("ospfSessionStatusSpec", true),
    PATH_CONSTRAINT("pathConstraint", true),
    PREFIX("prefix", true),
    PREFIX_RANGE("prefixRange", true),
    PROTOCOL("protocol", true),
    QUESTION("question", true),
    REFERENCE_BOOK_NAME("referenceBookName", true),
    ROUTING_POLICY_NAME("routingPolicyName", true),
    ROUTING_POLICY_SPEC("routingPolicySpec", true),
    ROUTING_PROTOCOL_SPEC("routingProtocolSpec", true),
    SINGLE_APPLICATION_SPEC("singleApplicationSpec", true),
    SOURCE_LOCATION("sourceLocation", true),
    STRING("string", true),
    STRUCTURE_NAME("structureName", true),
    SUBRANGE("subrange", true),
    TRACEROUTE_SOURCE_LOCATION("tracerouteSourceLocation", true),
    VRF("vrf", true),
    VXLAN_VNI_PROPERTY_SPEC("vxlanVniPropertySpec", true),
    ZONE("zone", true),
    DEPRECATED_FLOW_STATE("flowState", false);

    private static final Map<String, Variable.Type> MAP = initMap();

    // map from deprecated CompletionTypes to corresponding Variable.Type used for backwards
    // compatibility
    private static final Map<CompletionType, Variable.Type> COMPLETION_TYPE_MAP =
        ImmutableMap.<CompletionType, Type>builder()
            .put(CompletionType.BGP_PEER_PROPERTY, BGP_PEER_PROPERTY_SPEC)
            .put(CompletionType.BGP_PROCESS_PROPERTY, BGP_PROCESS_PROPERTY_SPEC)
            .put(CompletionType.INTERFACE_PROPERTY, INTERFACE_PROPERTY_SPEC)
            .put(CompletionType.NAMED_STRUCTURE, NAMED_STRUCTURE_SPEC)
            .put(CompletionType.NODE, NODE_SPEC)
            .put(CompletionType.NODE_PROPERTY, NODE_PROPERTY_SPEC)
            .put(CompletionType.OSPF_PROPERTY, OSPF_PROCESS_PROPERTY_SPEC)
            .build();

    @JsonCreator
    public static Variable.Type fromString(String name) {
      Variable.Type value = MAP.get(name.toLowerCase());
      if (value != null) {
        return value;
      }

      // no variable type of that name, check if it's an old CompletionType
      try {
        CompletionType completionType = CompletionType.valueOf(name.toUpperCase());
        value = COMPLETION_TYPE_MAP.get(completionType);
      } catch (IllegalArgumentException e) {
        // neither a Variable.Type or CompletionType
        value = null;
      }

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
      /** Synonyms for backward compatibility (July 9, 2019) */
      map.put("bgpSessionStatus".toLowerCase(), BGP_SESSION_STATUS_SPEC);
      map.put("bgpSessionType".toLowerCase(), BGP_SESSION_TYPE_SPEC);
      map.put("ipsecSessionStatus".toLowerCase(), IPSEC_SESSION_STATUS_SPEC);
      map.put("ospfPropertySpec".toLowerCase(), OSPF_PROCESS_PROPERTY_SPEC);
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
    return _values.stream()
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Variable variable = (Variable) o;
    return _optional == variable._optional
        && Objects.equals(_description, variable._description)
        && Objects.equals(_displayName, variable._displayName)
        && Objects.equals(_fields, variable._fields)
        && Objects.equals(_longDescription, variable._longDescription)
        && Objects.equals(_minElements, variable._minElements)
        && Objects.equals(_minLength, variable._minLength)
        && _type == variable._type
        && Objects.equals(_value, variable._value)
        && Objects.equals(_values, variable._values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _description,
        _displayName,
        _fields,
        _longDescription,
        _minElements,
        _minLength,
        _optional,
        _type.ordinal(),
        _value,
        _values);
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
          allowedValues.stream()
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
