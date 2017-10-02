package org.batfish.question.jsonpath;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.DisplayHints.ExtractionHint;
import org.batfish.question.jsonpath.JsonPathQuestionPlugin.JsonPathAnswerer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JsonPathDisplayHintsTest {

  @Rule
  public ExpectedException _thrown = ExpectedException.none();

  DisplayHints _displayHints;

  @Before
  public void readDisplayHints() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/question/jsonpath/jsonPathDisplayHintsTestHints.json");
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    _displayHints = mapper.readValue(text, DisplayHints.class);
  }

  private static JsonPathResult computeJsonPathResult(
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

    return result;
  }

  private DisplayHints getDisplayHints(String displayVariable) {
    DisplayHints displayHints = new DisplayHints();
    Map<String,ExtractionHint> extractionHints = new HashMap<>();
    extractionHints.put(displayVariable, _displayHints.getExtractionHints().get(displayVariable));
    displayHints.setExtractionHints(extractionHints);
    return displayHints;
  }

  @Test
  public void jsonPathDisplayHintInterfaceListFromPrefixOfSuffixTest() throws IOException {
    String displayVariable = "interfaceListFromPrefixOfSuffix";
    JsonPathResult result =
        computeJsonPathResult(
            "org/batfish/question/jsonpath/jsonPathDisplayHintsTestObject.json",
            "$.nodes[*].interfaces", true);

    Map<String, Map<String, JsonNode>> displayValues =
        result.extractDisplayValues(getDisplayHints(displayVariable));

    String key1 = "'nodes'->'node1'->'interfaces'";
    String key2 = "'nodes'->'node2'->'interfaces'";

    assertThat(displayValues.size(), equalTo(2));
    assertThat(displayValues.containsKey(key1), equalTo(true));
    assertThat(displayValues.containsKey(key2), equalTo(true));

    assertThat(displayValues.get(key1).get(displayVariable).isArray(), equalTo(true));
    ArrayNode node1 = (ArrayNode) displayValues.get(key1).get(displayVariable);
    assertThat(node1.size(), equalTo(2));
    assertThat(node1.get(0).asText(), equalTo("interface1"));
    assertThat(node1.get(1).asText(), equalTo("interface2"));

    assertThat(displayValues.get(key2).get(displayVariable).isArray(), equalTo(true));
    ArrayNode node2 = (ArrayNode) displayValues.get(key2).get(displayVariable);
    assertThat(node2.size(), equalTo(1));
    assertThat(node2.get(0).asText(), equalTo("interface1"));
  }

  @Test
  public void jsonPathDisplayHintMissingSuffixTest() throws IOException {
    String displayVariable = "mtuFromSuffixOfSuffix";
    // because includeSuffix is 'false' this should fail
    JsonPathResult result =
        computeJsonPathResult(
            "org/batfish/question/jsonpath/jsonPathDisplayHintsTestObject.json",
            "$.nodes[*].interfaces[*][?(@.mtu < 1600)].mtu", false);

    String errorMessage = "Cannot compute suffix-based display values with null suffix. "
        + "(Was suffix set to True in the original JsonPath Query?)";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(errorMessage);

    result.extractDisplayValues(getDisplayHints(displayVariable));
  }

  @Test
  public void jsonPathDisplayHintMtuFromSuffixOfSuffixTest() throws IOException {
    String displayVariable = "mtuFromSuffixOfSuffix";
    JsonPathResult result =
        computeJsonPathResult(
            "org/batfish/question/jsonpath/jsonPathDisplayHintsTestObject.json",
            "$.nodes[*].interfaces[*][?(@.mtu < 1600)].mtu", true);

    Map<String, Map<String, JsonNode>> displayValues =
        result.extractDisplayValues(getDisplayHints(displayVariable));

    String key1 = "'nodes'->'node1'->'interfaces'->'interface1'->'mtu'";
    String key2 = "'nodes'->'node2'->'interfaces'->'interface1'->'mtu'";

    assertThat(displayValues.size(), equalTo(2));
    assertThat(displayValues.containsKey(key1), equalTo(true));
    assertThat(displayValues.containsKey(key2), equalTo(true));

    assertThat(displayValues.get(key1).get(displayVariable).asInt(), equalTo(1500));
    assertThat(displayValues.get(key2).get(displayVariable).asInt(), equalTo(1550));
  }

  @Test
  public void jsonPathDisplayHintAddressCountFromFuncOfSuffixTest() throws IOException {
    String displayVariable = "addressCountFromFuncOfSuffix";
    JsonPathResult result =
        computeJsonPathResult(
            "org/batfish/question/jsonpath/jsonPathDisplayHintsTestObject.json",
            "$.nodes[*].interfaces[*][?(@.mtu < 1600)]", true);

    Map<String, Map<String, JsonNode>> displayValues =
        result.extractDisplayValues(getDisplayHints(displayVariable));

    String key1 = "'nodes'->'node1'->'interfaces'->'interface1'";
    String key2 = "'nodes'->'node2'->'interfaces'->'interface1'";

    assertThat(displayValues.size(), equalTo(2));
    assertThat(displayValues.containsKey(key1), equalTo(true));
    assertThat(displayValues.containsKey(key2), equalTo(true));

    assertThat(displayValues.get(key1).get(displayVariable).asInt(), equalTo(3));
    assertThat(displayValues.get(key2).get(displayVariable).asInt(), equalTo(2));
  }

  @Test
  public void jsonPathDisplayHintNodeFromPrefixTest() throws IOException {
    String displayVariable = "nodeFromPrefix";
    JsonPathResult result =
        computeJsonPathResult(
            "org/batfish/question/jsonpath/jsonPathDisplayHintsTestObject.json",
            "$.nodes[*].interfaces[*][?(@.mtu < 1600)].mtu", false);

    Map<String, Map<String, JsonNode>> displayValues =
        result.extractDisplayValues(getDisplayHints(displayVariable));

    String key1 = "'nodes'->'node1'->'interfaces'->'interface1'->'mtu'";
    String key2 = "'nodes'->'node2'->'interfaces'->'interface1'->'mtu'";

    assertThat(displayValues.size(), equalTo(2));
    assertThat(displayValues.containsKey(key1), equalTo(true));
    assertThat(displayValues.containsKey(key2), equalTo(true));

    assertThat(displayValues.get(key1).get(displayVariable).asText(), equalTo("node1"));
    assertThat(displayValues.get(key2).get(displayVariable).asText(), equalTo("node2"));
  }
}
