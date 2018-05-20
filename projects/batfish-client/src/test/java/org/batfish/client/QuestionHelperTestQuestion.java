package org.batfish.client;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.questions.Question;

public class QuestionHelperTestQuestion extends Question {

  public static final int DEFAULT_VALUE = 42;

  public static final String PROP_PARAMETER_MANDATORY = "parameterMandatory";
  public static final String PROP_PARAMETER_OPTIONAL = "parameterOptional";

  private final int _parameterMandatory;
  private final int _parameterOptional;

  @JsonCreator
  public QuestionHelperTestQuestion(
      @JsonProperty(PROP_PARAMETER_MANDATORY) Integer parameterMandatory,
      @JsonProperty(PROP_PARAMETER_OPTIONAL) Integer parameterOptional) {
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

  @JsonProperty(PROP_PARAMETER_MANDATORY)
  public int getParameterMandatory() {
    return _parameterMandatory;
  }

  @JsonProperty(PROP_PARAMETER_OPTIONAL)
  public int getParameterOptional() {
    return _parameterOptional;
  }
}
