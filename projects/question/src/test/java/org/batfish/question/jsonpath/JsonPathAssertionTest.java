package org.batfish.question.jsonpath;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.question.jsonpath.JsonPathQuestionPlugin.JsonPathAnswerer;
import org.batfish.question.jsonpath.JsonPathResult.JsonPathResultEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test JsonPathAssertion functionality */
public class JsonPathAssertionTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static Set<JsonPathResultEntry> computeResults(
      String jsonFile, String path, boolean includeSuffix) {
    Configuration.setDefaults(BatfishJsonPathDefaults.INSTANCE);
    ConfigurationBuilder b = new ConfigurationBuilder();
    Configuration baseConfiguration = b.build();
    String jsonStr = CommonUtil.readResource(jsonFile);
    Object jsonObject = JsonPath.parse(jsonStr, baseConfiguration).json();

    JsonPathQuery query = new JsonPathQuery();
    query.setPath(path);
    query.setSuffix(includeSuffix);

    JsonPathResult result = JsonPathAnswerer.computeResult(jsonObject, query);

    return new HashSet<>(result.getResult().values());
  }

  @Test
  public void testEvaluateCountFalse() {
    Set<JsonPathResultEntry> results =
        computeResults(
            "org/batfish/question/jsonpath/jsonPathAssertionTest.json",
            "$.nodes..interface1.mtu",
            true);
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.count);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("2", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    boolean result = jpAssertion.evaluate(results);
    assertThat(result, equalTo(false));
  }

  @Test
  public void testEvaluateCountTrue() {
    Set<JsonPathResultEntry> results =
        computeResults(
            "org/batfish/question/jsonpath/jsonPathAssertionTest.json", "$.nodes..mtu", true);
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.count);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("2", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    boolean result = jpAssertion.evaluate(results);
    assertThat(result, equalTo(true));
  }

  @Test
  public void testEvaluateNone() {
    Set<JsonPathResultEntry> results =
        computeResults(
            "org/batfish/question/jsonpath/jsonPathAssertionTest.json", "$.nodes..mtu", true);
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.none);
    String errorMessage = "Cannot evaluate assertion type none";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(errorMessage);
    jpAssertion.evaluate(results);
  }

  @Test
  public void testEvaluateEqualsFalse() {
    Set<JsonPathResultEntry> results =
        computeResults(
            "org/batfish/question/jsonpath/jsonPathAssertionTest.json",
            "$.nodes..interface1.mtu",
            false);
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.equals);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect = mapper.readValue("[{\"concretePath\": [\"'kkl'\"]}]", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    boolean result = jpAssertion.evaluate(results);
    assertThat(result, equalTo(false));
  }

  @Test
  public void testEvaluateEqualsTrueWithSuffix() {
    Set<JsonPathResultEntry> results =
        computeResults(
            "org/batfish/question/jsonpath/jsonPathAssertionTest.json", "$..ntpServers", true);
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.equals);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect =
          mapper.readValue(
              "[{"
                  + "\"concretePath\": [\"'nodes'\", \"'node1'\", \"'ntpServers'\"], "
                  + "\"suffix\" : [\"1.2.3.4\", \"5.6.7.8\"]"
                  + "}]",
              JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    boolean result = jpAssertion.evaluate(results);
    assertThat(result, equalTo(true));
  }

  @Test
  public void testEvaluateEqualsTrueWithoutSuffix() {
    Set<JsonPathResultEntry> results =
        computeResults(
            "org/batfish/question/jsonpath/jsonPathAssertionTest.json", "$..node1", false);
    JsonPathAssertion jpAssertion = new JsonPathAssertion();
    jpAssertion.setType(JsonPathAssertionType.equals);
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      JsonNode expect =
          mapper.readValue("[{\"concretePath\": [\"'nodes'\", \"'node1'\"]}]", JsonNode.class);
      jpAssertion.setExpect(expect);
    } catch (IOException e) {
      e.printStackTrace();
    }
    boolean result = jpAssertion.evaluate(results);
    assertThat(result, equalTo(true));
  }
}
