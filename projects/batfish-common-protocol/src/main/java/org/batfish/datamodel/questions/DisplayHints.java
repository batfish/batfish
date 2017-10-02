package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;

public class DisplayHints {

  public enum ValueType {
    INT,
    INTLIST,
    FLOW,
    FLOWLIST,
    FLOWTRACE,
    FLOWTRACELIST,
    STRING,
    STRINGLIST;

    public ValueType getBaseType() {
      switch (this) {
      case INT:
      case INTLIST:
        return INT;
      case FLOW:
      case FLOWLIST:
        return FLOW;
      case FLOWTRACE:
      case FLOWTRACELIST:
        return FLOWTRACE;
      case STRING:
      case STRINGLIST:
        return STRING;
      default:
        throw new BatfishException("Unknown ValueType " + this);
      }
    }

    public boolean isListType() {
      switch (this) {
      case INT:
      case STRING:
      case FLOW:
      case FLOWTRACE:
        return false;
      case INTLIST:
      case FLOWLIST:
      case FLOWTRACELIST:
      case STRINGLIST:
        return true;
      default:
        throw new BatfishException("Unknown ValueType " + this);
      }
    }
  }

  public enum EntityType {
    INTERFACE,
    INTERFACELIST,
    IPADDRESS,
    FILELINE,
    FILELINELIST,
    NODE
  }

  public static class EntityConfiguration {

    private static final String PROP_ADDRESS = "address";

    private static final String PROP_ENTITY_TYPE = "entityType";

    private static final String PROP_HOSTNAME = "hostname";

    private static final String PROP_INTERFACE = "interface";

    private static final String PROP_LINE_NUMBER = "lineNumber";

    private String _address;

    private EntityType _entityType;

    private String _hostname;

    private String _interface;

    private String _lineNumber;

    @JsonProperty(PROP_ADDRESS)
    public String getAddress() {
      return _address;
    }

    @JsonProperty(PROP_ENTITY_TYPE)
    public EntityType getEntityType() {
      return _entityType;
    }

    @JsonProperty(PROP_HOSTNAME)
    public String getHostname() {
      return _hostname;
    }

    @JsonProperty(PROP_INTERFACE)
    public String getInterface() {
      return _interface;
    }

    @JsonProperty(PROP_LINE_NUMBER)
    public String getLineNumber() {
      return _lineNumber;
    }

    @JsonIgnore
    public Set<String> getVars() {
      Set<String> retSet = new HashSet<>();
      if (!Strings.isNullOrEmpty(_address)) {
        retSet.add(_address);
      }
      if (!Strings.isNullOrEmpty(_hostname)) {
        retSet.add(_hostname);
      }
      if (!Strings.isNullOrEmpty(_interface)) {
        retSet.add(_interface);
      }
      if (!Strings.isNullOrEmpty(_lineNumber)) {
        retSet.add(_lineNumber);
      }
      return retSet;
    }

    @JsonProperty(PROP_ADDRESS)
    public void setAddress(String address) {
      _address = address;
    }

    @JsonProperty(PROP_ENTITY_TYPE)
    public void setEntityType(EntityType entityType) {
      _entityType = entityType;
    }

    @JsonProperty(PROP_HOSTNAME)
    public void setHostname(String hostname) {
      _hostname = hostname;
    }

    @JsonProperty(PROP_INTERFACE)
    public void setInterface(String interface1) {
      _interface = interface1;
    }

    @JsonProperty(PROP_LINE_NUMBER)
    public void setLineNumber(String lineNumber) {
      _lineNumber = lineNumber;
    }

    public void validate(String entityName) {
      if (_entityType == null) {
        throw new BatfishException("entityType not specified for entity " + entityName);
      }
      // 1. check if all necessary fields are defined for a type
      switch (_entityType) {
        case INTERFACE:
        case INTERFACELIST:
          if (Strings.isNullOrEmpty(_hostname)) {
            throw new BatfishException(
                "hostname not specified for entity of type INTERFACE[LIST]: " + entityName);
          }
          if (Strings.isNullOrEmpty(_interface)) {
            throw new BatfishException(
                "interface not specified for entity of type INTERFACE[LIST]: " + entityName);
          }
          break;
        case IPADDRESS:
          if (Strings.isNullOrEmpty(_address)) {
            throw new BatfishException(
                "address not specified for entity of type IPADDRESS: " + entityName);
          }
          break;
        case FILELINE:
        case FILELINELIST:
          if (Strings.isNullOrEmpty(_lineNumber)) {
            throw new BatfishException(
                "lineNumber not specified for entity of type FILELINE[LIST]: " + entityName);
          }
          break;
        case NODE:
          if (Strings.isNullOrEmpty(_hostname)) {
            throw new BatfishException(
                "hostname not specified for entity of type NODE: " + entityName);
          }
          break;
        default:
          throw new BatfishException("Unknown entity type " + _entityType);
      }

      // 2. check that no spurious fields are defined for a type
      if (!Strings.isNullOrEmpty(_address)) {
        if (_entityType != EntityType.IPADDRESS) {
          throw new BatfishException("address specified for entity of type " + _entityType);
        }
      }
      if (!Strings.isNullOrEmpty(_hostname)) {
        if (_entityType != EntityType.INTERFACE
            && _entityType != EntityType.INTERFACELIST
            && _entityType != EntityType.NODE) {
          throw new BatfishException("hostname specified for entity of type " + _entityType);
        }
      }
      if (!Strings.isNullOrEmpty(_interface)) {
        if (_entityType != EntityType.INTERFACE && _entityType != EntityType.INTERFACELIST) {
          throw new BatfishException("interface specified for entity of type " + _entityType);
        }
      }
      if (!Strings.isNullOrEmpty(_lineNumber)) {
        if (_entityType != EntityType.FILELINE && _entityType != EntityType.FILELINELIST) {
          throw new BatfishException("lineNumber specified for entity of type " + _entityType);
        }
      }
    }
  }

  public static class ExtractionHint {
    private static final String PROP_HINTS = "hints";

    private static final String PROP_VALUE_TYPE = "valueType";

    private Map<String, JsonNode> _hints;

    private ValueType _valueType;

    @JsonCreator
    public ExtractionHint(
        @JsonProperty(PROP_HINTS) Map<String, JsonNode> hints,
        @JsonProperty(PROP_VALUE_TYPE) ValueType type) {
      _hints = hints;
      _valueType = type;
    }

    @JsonProperty(PROP_HINTS)
    public Map<String, JsonNode> getHints() {
      return _hints;
    }

    @JsonProperty(PROP_VALUE_TYPE)
    public ValueType getValueType() {
      return _valueType;
    }

    @JsonProperty(PROP_HINTS)
    public void setHints(Map<String, JsonNode> hints) {
      _hints = hints;
    }

    @JsonProperty(PROP_VALUE_TYPE)
    public void setValueType(ValueType valueType) {
      _valueType = valueType;
    }

    public void validate(String varName) {
      if (_valueType == null) {
        throw new BatfishException("valueType not specified for variable " + varName);
      }
      if (_hints == null || _hints.isEmpty()) {
        throw new BatfishException("hints not specified for variable " + varName);
      }
    }
  }

  private static final String PROP_ENTITIES = "entities";

  private static final String PROP_EXTRACTION_HINTS = "extractionHints";

  private static final String PROP_TEXT_DESC = "textDesc";

  private Map<String, EntityConfiguration> _entities;

  private Map<String, ExtractionHint> _extractionHints;

  private String _textDesc;

  public DisplayHints() {}

  @JsonCreator
  public DisplayHints(
      @JsonProperty(PROP_ENTITIES) Map<String, EntityConfiguration> entities,
      @JsonProperty(PROP_EXTRACTION_HINTS) Map<String, ExtractionHint> extractionHints,
      @JsonProperty(PROP_TEXT_DESC) String textDesc) {

    if (entities == null) {
      entities = new HashMap<>();
    }
    if (extractionHints == null) {
      extractionHints = new HashMap<>();
    }
    if (textDesc == null) {
      textDesc = "";
    }

    Set<String> varsInEntities = new HashSet<>();
    for (Entry<String, EntityConfiguration> entry : entities.entrySet()) {
      entry.getValue().validate(entry.getKey());
      varsInEntities.addAll(entry.getValue().getVars());
    }

    for (Entry<String, ExtractionHint> entry : extractionHints.entrySet()) {
      entry.getValue().validate(entry.getKey());
    }

    // all extraction vars mentioned in entity configuration should have extraction hints
    Set<String> varsInExtractionHints = extractionHints.keySet();
    SetView<String> missingExtractionVars = Sets.difference(varsInEntities, varsInExtractionHints);
    if (!missingExtractionVars.isEmpty()) {
      throw new BatfishException(
          "entities refer to variables that are not in extraction hints: " + missingExtractionVars);
    }

    // the names of entities and extraction vars should have no overlap
    Set<String> commonNames = Sets.intersection(varsInExtractionHints, entities.keySet());
    if (!commonNames.isEmpty()) {
      throw new BatfishException(
          "entities and extraction vars should not have common names: " + commonNames);
    }

    // names in text description should correspond to those of entities or extraction vars
    Set<String> namesInTextDesc = new HashSet<>();
    Matcher matcher = Pattern.compile("\\$\\{([^\\}]+)\\}").matcher(textDesc);
    while (matcher.find()) {
      namesInTextDesc.add(matcher.group(1));
    }
    SetView<String> missingEntities =
        Sets.difference(namesInTextDesc, Sets.union(entities.keySet(), extractionHints.keySet()));
    if (!missingEntities.isEmpty()) {
      throw new BatfishException(
          "textDesc has names that are neither entities nor extractions: " + missingEntities);
    }

    _entities = entities;
    _extractionHints = extractionHints;
    _textDesc = textDesc;
  }

  @JsonProperty(PROP_ENTITIES)
  public Map<String, EntityConfiguration> getEntities() {
    return _entities;
  }

  @JsonProperty(PROP_EXTRACTION_HINTS)
  public Map<String, ExtractionHint> getExtractionHints() {
    return _extractionHints;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public String getTextDesc() {
    return _textDesc;
  }

  @JsonProperty(PROP_ENTITIES)
  public void setEntities(Map<String, EntityConfiguration> entities) {
    _entities = entities;
  }

  @JsonProperty(PROP_EXTRACTION_HINTS)
  public void setExtractionHints(Map<String, ExtractionHint> extractionHints) {
    _extractionHints = extractionHints;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public void setTextDesc(String textDesc) {
    _textDesc = textDesc;
  }
}
