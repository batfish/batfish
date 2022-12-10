package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.re2j.Pattern;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Question implements IQuestion {

  /** When diffing tables, whether to ignore keys present in only one table by default */
  private static final boolean DEFAULT_INCLUDE_ONE_TABLE_KEYS = true;

  private Assertion _assertion;

  private boolean _differential;

  protected DisplayHints _displayHints;

  private List<Exclusion> _exclusions;

  private boolean _includeOneTableKeys;

  private InstanceData _instance;

  public Question() {
    _differential = false;
    _includeOneTableKeys = DEFAULT_INCLUDE_ONE_TABLE_KEYS;
  }

  /** Returns {@code true} iff this question requires a computed data plane as input. */
  @JsonIgnore
  public abstract boolean getDataPlane();

  @JsonProperty(BfConsts.PROP_ASSERTION)
  public Assertion getAssertion() {
    return _assertion;
  }

  @JsonProperty(BfConsts.PROP_DIFFERENTIAL)
  public boolean getDifferential() {
    return _differential;
  }

  @JsonProperty(BfConsts.PROP_DISPLAY_HINTS)
  public DisplayHints getDisplayHints() {
    return _displayHints;
  }

  @JsonProperty(BfConsts.PROP_EXCLUSIONS)
  public List<Exclusion> getExclusions() {
    return _exclusions;
  }

  @JsonProperty(BfConsts.PROP_INCLUDE_ONE_TABLE_KEYS)
  public boolean getIncludeOneTableKeys() {
    return _includeOneTableKeys;
  }

  /** Returns {@code true} iff this question does not need the testrig to be properly parsed */
  @JsonIgnore
  public boolean getIndependent() {
    return false;
  }

  @JsonProperty(BfConsts.PROP_INSTANCE)
  public InstanceData getInstance() {
    return _instance;
  }

  /**
   * Returns the short name of this question, used in place of the classname to identify this
   * question.
   */
  @JsonIgnore
  public abstract String getName();

  /**
   * Does this class name belong to a question?
   *
   * @param className Full name of the class
   * @return The result
   */
  private static boolean isQuestionClass(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      return Question.class.isAssignableFrom(clazz);
    } catch (ClassNotFoundException e) {
      throw new BatfishException("'" + className + "' is not a valid Question class");
    }
  }

  public static Question parseQuestion(String rawQuestionText) {
    String questionText = Question.preprocessQuestion(rawQuestionText);
    try {
      return BatfishObjectMapper.mapper().readValue(questionText, Question.class);
    } catch (IOException e) {
      throw new BatfishException("Could not parse JSON question: " + e.getMessage(), e);
    }
  }

  private static String preprocessQuestion(String rawQuestionText) {
    try {
      JSONObject jobj = new JSONObject(rawQuestionText);
      if (jobj.has(BfConsts.PROP_INSTANCE) && !jobj.isNull(BfConsts.PROP_INSTANCE)) {
        String instanceDataStr = jobj.getString(BfConsts.PROP_INSTANCE);
        InstanceData instanceData =
            BatfishObjectMapper.mapper()
                .readValue(instanceDataStr, new TypeReference<InstanceData>() {});
        for (Entry<String, Variable> e : instanceData.getVariables().entrySet()) {
          String varName = e.getKey();
          Variable variable = e.getValue();
          JsonNode value = variable.getValue();
          // Clear optional variables with null values (for now, assume non-optional variables with
          // null values will be handled by later validation)
          if (value == null) {
            if (variable.getOptional()) {
              recursivelyRemoveOptionalVar(jobj, varName);
            }
            continue;
          }
          if (variable.getType() == Variable.Type.QUESTION) {
            if (variable.getMinElements() != null) {
              if (!value.isArray()) {
                throw new IllegalArgumentException("Expecting JSON array for array type");
              }
              JSONArray arr = new JSONArray();
              for (int i = 0; i < value.size(); i++) {
                String valueJsonString = new ObjectMapper().writeValueAsString(value.get(i));
                arr.put(i, new JSONObject(preprocessQuestion(valueJsonString)));
              }
              jobj.put(varName, arr);
            } else {
              String valueJsonString = new ObjectMapper().writeValueAsString(value);
              jobj.put(varName, new JSONObject(preprocessQuestion(valueJsonString)));
            }
          }
        }
        String questionText = jobj.toString();
        for (Entry<String, Variable> e : instanceData.getVariables().entrySet()) {
          String varName = e.getKey();
          Variable variable = e.getValue();
          JsonNode value = variable.getValue();
          String valueJsonString = new ObjectMapper().writeValueAsString(value);
          boolean stringType = variable.getType().getStringType();
          boolean setType = variable.getMinElements() != null;
          if (value != null) {
            String topLevelVarNameRegex = Pattern.quote("\"${" + varName + "}\"");
            String inlineVarNameRegex = Pattern.quote("${" + varName + "}");
            String topLevelReplacement = valueJsonString;
            String inlineReplacement;
            if (stringType && !setType) {
              inlineReplacement = valueJsonString.substring(1, valueJsonString.length() - 1);
            } else {
              String quotedValueJsonString = JSONObject.quote(valueJsonString);
              inlineReplacement =
                  quotedValueJsonString.substring(1, quotedValueJsonString.length() - 1);
            }
            String inlineReplacementRegex = Matcher.quoteReplacement(inlineReplacement);
            String topLevelReplacementRegex = Matcher.quoteReplacement(topLevelReplacement);
            questionText = questionText.replaceAll(topLevelVarNameRegex, topLevelReplacementRegex);
            questionText = questionText.replaceAll(inlineVarNameRegex, inlineReplacementRegex);
          }
        }
        return questionText;
      }
      return rawQuestionText;
    } catch (JSONException | IOException e) {
      throw new BatfishException(
          String.format("Could not convert raw question text [%s] to JSON", rawQuestionText), e);
    }
  }

  /**
   * Recursively look for all key, value pairs and remove keys whose value is "${{@code varName}}".
   * Is this fragile? To be doubly sure, we do this only for keys with a sibling key "class" that is
   * a Question class
   */
  private static void recursivelyRemoveOptionalVar(JSONObject questionObject, String varName)
      throws JSONException {
    Iterator<?> iter = questionObject.keys();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      Object value = questionObject.get(key);
      if (value instanceof String) {
        if (value.equals("${" + varName + "}")) {
          iter.remove();
        }
      } else if (value instanceof JSONObject) {
        JSONObject childObject = (JSONObject) value;
        if (childObject.has("class")) {
          Object classValue = childObject.get("class");
          if (classValue instanceof String && isQuestionClass((String) classValue)) {
            recursivelyRemoveOptionalVar(childObject, varName);
          }
        }
      }
    }
  }

  @JsonProperty(BfConsts.PROP_ASSERTION)
  public void setAssertion(Assertion assertion) {
    _assertion = assertion;
  }

  @JsonProperty(BfConsts.PROP_DIFFERENTIAL)
  public void setDifferential(boolean differential) {
    _differential = differential;
  }

  @JsonProperty(BfConsts.PROP_DISPLAY_HINTS)
  public void setDisplayHints(DisplayHints displayHints) {
    _displayHints = displayHints;
  }

  @JsonProperty(BfConsts.PROP_EXCLUSIONS)
  public void setExclusions(List<Exclusion> exclusions) {
    _exclusions = exclusions;
  }

  @JsonProperty(BfConsts.PROP_INCLUDE_ONE_TABLE_KEYS)
  public void setIncludeOneTableKeys(boolean includeOneTableKeys) {
    _includeOneTableKeys = includeOneTableKeys;
  }

  @JsonProperty(BfConsts.PROP_INSTANCE)
  public void setInstance(InstanceData instance) {
    _instance = instance;
  }

  @Override
  public String toFullJsonString() {
    try {
      return BatfishObjectMapper.verboseWriter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Failed to convert question to full JSON string", e);
    }
  }

  @Override
  public String toJsonString() {
    try {
      return BatfishObjectMapper.writePrettyString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Failed to convert question to JSON string", e);
    }
  }
}
