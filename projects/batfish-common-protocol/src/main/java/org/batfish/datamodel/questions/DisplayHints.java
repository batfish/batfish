package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.batfish.common.BatfishException;

public class DisplayHints {

  public static enum ValueType {
    INT, INTLIST, STRING, STRINGLIST;

    public ValueType getBaseType() {
      switch (this) {
      case INT:
      case INTLIST:
        return INT;
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
        return false;
      case INTLIST:
      case STRINGLIST:
        return true;
      default:
        throw new BatfishException("Unknown ValueType " + this);
      }
    }
  }

  public static enum EntityType {
    INTERFACE,
    INTERFACELIST,
    IPADDRESS,
    NODE
  }

  public static class EntityConfiguration {

    private static final String PROP_ADDRESS = "address";

    private static final String PROP_ENTITY_TYPE = "entityType";

    private static final String PROP_HOSTNAME = "hostname";

    private static final String PROP_INTERFACE = "interface";

    private String _address;

    private EntityType _entityType;

    private String _hostname;

    private String _interface;

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
  }

  public static class ExtractionHint {
    private static final String PROP_HINTS = "hints";

    private static final String PROP_VALUE_TYPE = "valueType";

    private Map<String, JsonNode> _hints;

    private ValueType _valueType;

    @JsonCreator public ExtractionHint(
        @JsonProperty(PROP_HINTS) Map<String, JsonNode> hints,
        @JsonProperty(PROP_VALUE_TYPE) ValueType type
        ) {
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
  }

  private static final String PROP_ENTITIES = "entities";

  private static final String PROP_EXTRACTION_HINTS = "extractionHints";

  private static final String PROP_TEXT_DESC = "textDesc";

  private Map<String, EntityConfiguration> _entities;

  private Map<String, ExtractionHint> _extractionHints;

  private String _textDesc;

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
