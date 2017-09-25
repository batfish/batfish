package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public class DisplayHints {

  public static enum ValueType {
    INT("int"),
    STRING("string");

    private static final Map<String, ValueType> MAP = initMap();

    @JsonCreator
    public static ValueType fromString(String name) {
      ValueType value = MAP.get(name.toLowerCase());
      if (value == null) {
        throw new BatfishException(
            "No " + ValueType.class.getSimpleName() + " with name: '" + name + "'");
      }
      return value;
    }

    private static synchronized Map<String, ValueType> initMap() {
      Map<String, ValueType> map = new HashMap<>();
      for (ValueType value : ValueType.values()) {
        String name = value._name.toLowerCase();
        map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
    }

    private final String _name;

    private ValueType(String name) {
      _name = name;
    }

    @JsonValue
    public String getName() {
      return _name;
    }
  }

  public static class ExtractionHint {
    private static final String PROP_HINTS = "hints";

    private static final String PROP_IS_LIST = "isList";

    private static final String PROP_VALUE_TYPE = "valuetype";

    private Map<String, JsonNode> _hints;

    private boolean _isList;

    private ValueType _valueType;

    @JsonCreator public ExtractionHint(
        @JsonProperty(PROP_HINTS) Map<String, JsonNode> hints,
        @JsonProperty(PROP_IS_LIST) Boolean isList,
        @JsonProperty(PROP_VALUE_TYPE) ValueType type
        ) {
      _hints = hints;
      if (isList == null) {
        _isList = false;    //default value
      } else {
        _isList = isList.booleanValue();
      }
      _valueType = type;
    }

    @JsonProperty(PROP_HINTS)
    public Map<String, JsonNode> getHints() {
      return _hints;
    }

    @JsonProperty(PROP_IS_LIST)
    public boolean getIsList() {
      return _isList;
    }

    @JsonProperty(PROP_VALUE_TYPE)
    public ValueType getValueType() {
      return _valueType;
    }

    @JsonProperty(PROP_HINTS)
    public void setHints(Map<String, JsonNode> hints) {
      _hints = hints;
    }

    @JsonProperty(PROP_IS_LIST)
    public void setIsList(boolean isList) {
      _isList = isList;
    }

    @JsonProperty(PROP_VALUE_TYPE)
    public void setValueType(ValueType valueType) {
      _valueType = valueType;
    }
  }

  private static final String PROP_ENTITIES = "entities";

  private static final String PROP_EXTRACTION_HINTS = "extractionHints";

  private static final String PROP_TEXT_DESC = "textDesc";

  private Map<String, JsonNode> _entities;

  private Map<String, ExtractionHint> _extractionHints;

  private String _textDesc;

  @JsonProperty(PROP_ENTITIES)
  public Map<String, JsonNode> getEntities() {
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
  public void setEntities(Map<String, JsonNode> entities) {
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
