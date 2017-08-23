package org.batfish.question;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.question.jsonpath.BatfishJsonPathDefaults;
import org.batfish.question.jsonpath.JsonPathAssertion;
import org.batfish.question.jsonpath.JsonPathAssertionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test JsonPathAssertion functionality */
public class JsonPathAssertionTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Configuration _baseConfiguration;

  private Object _jsonPathAssertionTestJsonObject;

  private String _jsonPathAssertionTestStr;

  private Configuration _suffixConfiguration;

  public JsonPathAssertionTest() {
    Configuration.setDefaults(BatfishJsonPathDefaults.INSTANCE);
    ConfigurationBuilder b = new ConfigurationBuilder();
    _baseConfiguration = b.build();
    _jsonPathAssertionTestStr = CommonUtil
        .readResource("org/batfish/question/jsonPathAssertionTest.json");
    _jsonPathAssertionTestJsonObject =
        JsonPath.parse(_jsonPathAssertionTestStr, _baseConfiguration).json();
    ConfigurationBuilder suffixCb = new ConfigurationBuilder();
    suffixCb.mappingProvider(_baseConfiguration.mappingProvider());
    suffixCb.jsonProvider(_baseConfiguration.jsonProvider());
    suffixCb.evaluationListener(_baseConfiguration.getEvaluationListeners());
    suffixCb.options(_baseConfiguration.getOptions());
    suffixCb.options(Option.ALWAYS_RETURN_LIST);
    _suffixConfiguration = suffixCb.build();
  }

  @Test
  public void testEvaluateCountFalse() {
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.count);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("2", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String path = "$.nodes..interface1.mtu";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode suffixes = null;
    try {
      suffixes = jsonPath.read(_jsonPathAssertionTestJsonObject, _suffixConfiguration);
    } catch (PathNotFoundException e) {
      suffixes = JsonNodeFactory.instance.arrayNode();
    }
    boolean result = jpAssertion.evaluate(suffixes);
    assertThat(result, equalTo(false));
  }

  @Test
  public void testEvaluateCountTrue() {
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.count);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("2", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String path = "$.nodes..mtu";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode suffixes = null;
    try {
      suffixes = jsonPath.read(_jsonPathAssertionTestJsonObject, _suffixConfiguration);
    } catch (PathNotFoundException e) {
      suffixes = JsonNodeFactory.instance.arrayNode();
    }
    boolean result = jpAssertion.evaluate(suffixes);
    assertThat(result, equalTo(true));
  }

  @Test
  public void testEvaluateNone() {
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.none);
    String path = "$.nodes..mtu";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode suffixes = null;
    try {
      suffixes = jsonPath.read(_jsonPathAssertionTestJsonObject, _suffixConfiguration);
    } catch (PathNotFoundException e) {
      suffixes = JsonNodeFactory.instance.arrayNode();
    }
    String errorMessage = "Cannot evaluate assertion type none";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(errorMessage);
    jpAssertion.evaluate(suffixes);
  }

  @Test
  public void testEvaluateSuffixEqualsFalse() {
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.suffixEquals);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("[1500, 1600]", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String path = "$.nodes..interface1.mtu";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode suffixes = null;
    try {
      suffixes = jsonPath.read(_jsonPathAssertionTestJsonObject, _suffixConfiguration);
    } catch (PathNotFoundException e) {
      suffixes = JsonNodeFactory.instance.arrayNode();
    }
    boolean result = jpAssertion.evaluate(suffixes);
    assertThat(result, equalTo(false));
  }

  @Test
  public void testEvaluateSuffixEqualTrue() {
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.suffixEquals);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("[1500, 1600]", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String path = "$.nodes..mtu";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode suffixes = null;
    try {
      suffixes = jsonPath.read(_jsonPathAssertionTestJsonObject, _suffixConfiguration);
    } catch (PathNotFoundException e) {
      suffixes = JsonNodeFactory.instance.arrayNode();
    }
    boolean result = jpAssertion.evaluate(suffixes);
    assertThat(result, equalTo(true));
  }

}
