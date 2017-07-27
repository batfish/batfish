package org.batfish.question;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.common.util.CommonUtil;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

/** Test JsonPath functionality */
public class JsonPathTest {

  private Configuration _baseConfiguration;

  private Object _jsonObject;

  private String _oneNtpServerNodesAnswerStr;

  private String _twoNtpServersNodesAnswerStr;

  private Configuration _prefixConfiguration;

  public JsonPathTest() {
    ConfigurationBuilder b = new ConfigurationBuilder();
    b.jsonProvider(new JacksonJsonNodeJsonProvider());
    _baseConfiguration = b.build();
    _oneNtpServerNodesAnswerStr = CommonUtil.readResource("org/batfish/question/oneNtpServer.json");
    _twoNtpServersNodesAnswerStr =
        CommonUtil.readResource("org/batfish/question/twoNtpServers.json");
    _jsonObject = JsonPath.parse(_oneNtpServerNodesAnswerStr, _baseConfiguration).json();
    ConfigurationBuilder prefixCb = new ConfigurationBuilder();
    prefixCb.mappingProvider(_baseConfiguration.mappingProvider());
    prefixCb.jsonProvider(_baseConfiguration.jsonProvider());
    prefixCb.evaluationListener(_baseConfiguration.getEvaluationListeners());
    prefixCb.options(_baseConfiguration.getOptions());
    prefixCb.options(Option.ALWAYS_RETURN_LIST);
    prefixCb.options(Option.AS_PATH_LIST);
    _prefixConfiguration = prefixCb.build();
  }

  @Test
  public void testOneNtpServerPresent() {
    String path = "$.nodes[*][?(!([\"1.2.3.4\"] subsetof @.ntpServers))].ntpServers";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode prefixes = null;
    try {
      prefixes = jsonPath.read(_jsonObject, _prefixConfiguration);
    } catch (PathNotFoundException e) {
      prefixes = JsonNodeFactory.instance.arrayNode();
    }
    assertThat(prefixes, not(equalTo(nullValue())));
    assertThat(prefixes.size(), not(equalTo(0)));
  }

  @Test
  public void testOneNtpServerPresentViolation() {
    String path = "$.nodes[*][?(!([\"9.9.9.9\"] subsetof @.ntpServers))].ntpServers";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode prefixes = null;
    try {
      prefixes = jsonPath.read(_jsonObject, _prefixConfiguration);
    } catch (PathNotFoundException e) {
      prefixes = JsonNodeFactory.instance.arrayNode();
    }
    assertThat(prefixes, not(equalTo(nullValue())));
    assertThat(prefixes.size(), equalTo(0));
  }

  @Test
  public void testOneNtpServerSanctioned() {
    String path = "$.nodes[*].ntpServers[?(@ nin [\"1.2.3.4\"])]";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode prefixes = null;
    try {
      prefixes = jsonPath.read(_jsonObject, _prefixConfiguration);
    } catch (PathNotFoundException e) {
      prefixes = JsonNodeFactory.instance.arrayNode();
    }
    assertThat(prefixes, not(equalTo(nullValue())));
    assertThat(prefixes.size(), equalTo(0));
  }

  @Test
  public void testOneNtpServerSanctionedViolation() {
    String path = "$.nodes[*].ntpServers[?(@ nin [\"9.9.9.9\"])]";
    JsonPath jsonPath = JsonPath.compile(path);
    ArrayNode prefixes = null;
    try {
      prefixes = jsonPath.read(_jsonObject, _prefixConfiguration);
    } catch (PathNotFoundException e) {
      prefixes = JsonNodeFactory.instance.arrayNode();
    }
    assertThat(prefixes, not(equalTo(nullValue())));
    assertThat(prefixes.size(), not(equalTo(0)));
  }
}
