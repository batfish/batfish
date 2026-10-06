package org.batfish.client;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.datamodel.questions.Question;

public class QuestionHelperTestQuestion extends Question {

  public static final int DEFAULT_VALUE = 42;

  public static final String PROP_PARAMETER_JSON_OBJECT = "parameterJsonObject";
  public static final String PROP_PARAMETER_MANDATORY = "parameterMandatory";
  public static final String PROP_PARAMETER_OPTIONAL = "parameterOptional";

  private final JsonNode _parameterJsonObject;
  private final int _parameterMandatory;
  private final int _parameterOptional;

  @JsonCreator
  public QuestionHelperTestQuestion(
      @JsonProperty(PROP_PARAMETER_JSON_OBJECT) JsonNode parameterJsonObject,
      @JsonProperty(PROP_PARAMETER_MANDATORY) Integer parameterMandatory,
      @JsonProperty(PROP_PARAMETER_OPTIONAL) Integer parameterOptional) {
    _parameterJsonObject = parameterJsonObject;
    _parameterMandatory = parameterMandatory;
    _parameterOptional = firstNonNull(parameterOptional, DEFAULT_VALUE);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return null;
  }

  @JsonProperty(PROP_PARAMETER_JSON_OBJECT)
  public JsonNode getParameterJsonObject() {
    return _parameterJsonObject;
  }

  @JsonProperty(PROP_PARAMETER_MANDATORY)
  public int getParameterMandatory() {
    return _parameterMandatory;
  }

  @JsonProperty(PROP_PARAMETER_OPTIONAL)
  public int getParameterOptional() {
    return _parameterOptional;
  }
}
