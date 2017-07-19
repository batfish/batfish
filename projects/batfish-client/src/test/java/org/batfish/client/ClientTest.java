package org.batfish.client;

import static org.batfish.client.Command.ADD_ANALYSIS_QUESTIONS;
import static org.batfish.client.Command.ADD_BATFISH_OPTION;
import static org.batfish.client.Command.ANSWER;
import static org.batfish.client.Command.ANSWER_DELTA;
import static org.batfish.client.Command.CAT;
import static org.batfish.client.Command.CHECK_API_KEY;
import static org.batfish.client.Command.CLEAR_SCREEN;
import static org.batfish.client.Command.DEL_ANALYSIS;
import static org.batfish.client.Command.DEL_ANALYSIS_QUESTIONS;
import static org.batfish.client.Command.DEL_BATFISH_OPTION;
import static org.batfish.client.Command.DEL_CONTAINER;
import static org.batfish.client.Command.DEL_ENVIRONMENT;
import static org.batfish.client.Command.DEL_QUESTION;
import static org.batfish.client.Command.DEL_TESTRIG;
import static org.batfish.client.Command.DIR;
import static org.batfish.client.Command.EXIT;
import static org.batfish.client.Command.GEN_DELTA_DP;
import static org.batfish.client.Command.GEN_DP;
import static org.batfish.client.Command.GET;
import static org.batfish.client.Command.GET_ANALYSIS_ANSWERS;
import static org.batfish.client.Command.GET_ANALYSIS_ANSWERS_DELTA;
import static org.batfish.client.Command.GET_ANALYSIS_ANSWERS_DIFFERENTIAL;
import static org.batfish.client.Command.GET_ANSWER;
import static org.batfish.client.Command.GET_ANSWER_DELTA;
import static org.batfish.client.Command.GET_ANSWER_DIFFERENTIAL;
import static org.batfish.client.Command.GET_DELTA;
import static org.batfish.client.Command.GET_QUESTION;
import static org.batfish.client.Command.HELP;
import static org.batfish.client.Command.INIT_ANALYSIS;
import static org.batfish.client.Command.INIT_CONTAINER;
import static org.batfish.client.Command.INIT_DELTA_ENV;
import static org.batfish.client.Command.INIT_DELTA_TESTRIG;
import static org.batfish.client.Command.INIT_TESTRIG;
import static org.batfish.client.Command.LIST_ANALYSES;
import static org.batfish.client.Command.LIST_CONTAINERS;
import static org.batfish.client.Command.LIST_ENVIRONMENTS;
import static org.batfish.client.Command.LIST_QUESTIONS;
import static org.batfish.client.Command.LIST_TESTRIGS;
import static org.batfish.client.Command.LOAD_QUESTIONS;
import static org.batfish.client.Command.PROMPT;
import static org.batfish.client.Command.PWD;
import static org.batfish.client.Command.REINIT_DELTA_TESTRIG;
import static org.batfish.client.Command.REINIT_TESTRIG;
import static org.batfish.client.Command.RUN_ANALYSIS;
import static org.batfish.client.Command.RUN_ANALYSIS_DELTA;
import static org.batfish.client.Command.RUN_ANALYSIS_DIFFERENTIAL;
import static org.batfish.client.Command.SET_BATFISH_LOGLEVEL;
import static org.batfish.client.Command.SET_CONTAINER;
import static org.batfish.client.Command.SET_DELTA_ENV;
import static org.batfish.client.Command.SET_DELTA_TESTRIG;
import static org.batfish.client.Command.SET_ENV;
import static org.batfish.client.Command.SET_LOGLEVEL;
import static org.batfish.client.Command.SET_PRETTY_PRINT;
import static org.batfish.client.Command.SET_TESTRIG;
import static org.batfish.client.Command.SHOW_API_KEY;
import static org.batfish.client.Command.SHOW_BATFISH_LOGLEVEL;
import static org.batfish.client.Command.SHOW_BATFISH_OPTIONS;
import static org.batfish.client.Command.SHOW_CONTAINER;
import static org.batfish.client.Command.SHOW_COORDINATOR_HOST;
import static org.batfish.client.Command.SHOW_DELTA_TESTRIG;
import static org.batfish.client.Command.SHOW_LOGLEVEL;
import static org.batfish.client.Command.SHOW_TESTRIG;
import static org.batfish.client.Command.TEST;
import static org.batfish.client.Command.UPLOAD_CUSTOM_OBJECT;
import static org.batfish.common.CoordConsts.DEFAULT_API_KEY;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.IP;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.IP_PROTOCOL;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.IP_WILDCARD;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.PREFIX;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.PREFIX_RANGE;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.PROTOCOL;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.STRING;
import static org.batfish.datamodel.questions.Question.InstanceData.Variable.Type.SUBRANGE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Pair;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.Question;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link org.batfish.client.Client}.
 */
public class ClientTest {

   private BatfishObjectMapper mapper;

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Before
   public void initMapper() {
      mapper = new BatfishObjectMapper();
   }

   //Tests for validateJsonPathRegex method
   @Test
   public void testEmptyJsonPathRegex() {
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "A Batfish %s must start with \"/\"",
                  Question.InstanceData.Variable.Type.JSON_PATH_REGEX.getName())));
      Client.validateJsonPathRegex("");
   }

   @Test
   public void testPathRegexInvalidStart() {
      String invalidStart = "pathRegex";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "A Batfish %s must start with \"/\"",
                  Question.InstanceData.Variable.Type.JSON_PATH_REGEX.getName())));
      Client.validateJsonPathRegex(invalidStart);
   }

   @Test
   public void testPathRegexInvalidEnd() {
      String invalidEnd = "/pathRegex";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "A Batfish %s must end in either \"/\" or \"/i\"",
                  Question.InstanceData.Variable.Type.JSON_PATH_REGEX.getName())));
      Client.validateJsonPathRegex(invalidEnd);
   }

   @Test
   public void testInvalidInteriorJavaRegex() {
      String invalidJavaRegex = "/...{\\\\Q8\\\\E}/";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "Invalid %s at interior of %s",
                  Question.InstanceData.Variable.Type.JAVA_REGEX.getName(),
                  Question.InstanceData.Variable.Type.JSON_PATH_REGEX.getName())));
      Client.validateJsonPathRegex(invalidJavaRegex);
   }

   @Test
   public void testPathRegexWithOnlySlash() {
      String jsonPathRegex = "/";
      Client.validateJsonPathRegex(jsonPathRegex);
   }

   @Test
   public void testValidPathRegex() {
      String jsonPathRegex = "/.*/";
      Client.validateJsonPathRegex(jsonPathRegex);
   }

   //Tests for validateJsonPath method
   @Test
   public void testEmptyJsonPath() throws IOException {
      JsonNode emptyPath = mapper.readTree("\"\"");
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "Expecting a JSON dictionary for a Batfish %s",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName())));
      Client.validateJsonPath(emptyPath);
   }

   @Test
   public void testInvalidJsonPath() throws IOException {
      JsonNode invalidPath
            = mapper.readTree("\"variable\" : \"I am variable\"");
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "Expecting a JSON dictionary for a Batfish %s",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName())));
      Client.validateJsonPath(invalidPath);
   }

   @Test
   public void testJsonPathNoPathAttribute() throws IOException {
      String invalidJsonPath = "{\"variable\" : \"I am variable\"}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "Missing 'path' element of %s",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName())));
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testNotStringPath() throws IOException {
      String invalidJsonPath = "{\"path\" : 1}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "'path' element of %s must be a JSON string",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName())));
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testNestedContainerPathValue() throws IOException {
      String invalidJsonPath = "{\"path\" : {\"innerVariable\" : \"content\"}}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "'path' element of %s must be a JSON string",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName())));
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testJsonPathNotBooleanSuffix() throws IOException {
      String invalidJsonPath = "{\"path\" : \"I am path.\", \"suffix\" : \"I " +
            "am suffix.\"}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format(
                  "'suffix' element of %s must be a JSON boolean",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName())));
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testValidJsonPath() throws IOException {
      String validJsonPath = "{\"path\" : \"I am path.\", \"suffix\" : true}";
      Client.validateJsonPath(mapper.readTree(validJsonPath));
   }

// ratul commented out this test
//     parseParaValue should allow strings like "I am not valid" to parse.
//     i can't think of what that new function will not parse
//       TODO: remove test or give it some teeth
//   // Tests for parseParaValue method
//   @Test
//   public void testParseInvalidJsonContent() {
//      String invalidJsonContent = "I am not valid";
//      thrown.expect(BatfishException.class);
//      thrown.expectMessage(equalTo(
//            String.format(
//                  "Variable value \"%s\" is not valid JSON",
//                  invalidJsonContent)));
//      Client.parseParaValue("content", invalidJsonContent);
//   }

   @Test
   public void testParseValidJsonContent() {
      String validJsonContent = "true";
      JsonNode node = Client.parseParaValue("boolean", validJsonContent);
      assertThat(node.getNodeType(), is(JsonNodeType.BOOLEAN));
      assertThat(node.asBoolean(), is(equalTo(true)));
   }

   // Tests for validateNde method
   @Test
   public void testValidateInvalidNode() throws IOException {
      String parameterName = "boolean";
      JsonNode invalidNode = mapper.readTree("\"I am string\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            String.format("Invalid value for parameter %s: %s",
                  parameterName, invalidNode)));
      Client.validateNode(invalidNode, variable, parameterName);
   }

   @Test
   public void testValidateValidNode() throws IOException {
      String parameterName = "boolean";
      JsonNode invalidNode = mapper.readTree("false");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      Client.validateNode(invalidNode, variable, parameterName);
   }

   // Tests for validateType method
   private void validateTypeWithInvalidInput(
         String input, String
         expectedMessage, Question.InstanceData.Variable.Type type)
         throws IOException {
      JsonNode node = mapper.readTree(input);
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(type);
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(expectedMessage));
      Client.validateType(node, variable);
   }

   @Test
   public void testUnStringInputWhenExpectString() throws IOException {
      String input = "10";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.STRING;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string",
                  Question.InstanceData.Variable.Type.STRING.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidBooleanValue() throws IOException {
      String input = "\"true\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.BOOLEAN;
      String expectedMessage = String
            .format(
                  "It is not a valid JSON %s value",
                  Question.InstanceData.Variable.Type.BOOLEAN.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidBooleanValue() throws IOException {
      JsonNode booleanNode = mapper.readTree("true");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      Client.validateType(booleanNode, variable);
   }

   @Test
   public void testInvalidComparatorValue() throws IOException {
      String input = "\"=>\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.COMPARATOR;
      String expectedMessage = "It is not a known comparator. Valid options " +
            "are: [==, <=, !=, <, >, >=]";
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidComparatorValue() throws IOException {
      JsonNode comparatorNode = mapper.readTree("\">=\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.COMPARATOR);
      Client.validateType(comparatorNode, variable);
   }

   @Test
   public void testInvalidIntegerValue() throws IOException {
      String input = "1.5";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.INTEGER;
      String expectedMessage = String
            .format(
                  "It is not a valid JSON %s value",
                  Question.InstanceData.Variable.Type.INTEGER.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidIntegerValue() throws IOException {
      JsonNode integerNode = mapper.readTree("15");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.INTEGER);
      Client.validateType(integerNode, variable);
   }

   @Test
   public void testInvalidDoubleValue() throws IOException {
      String input = "\"string\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.DOUBLE;
      String expectedMessage = String
            .format(
                  "It is not a valid JSON %s value",
                  Question.InstanceData.Variable.Type.DOUBLE.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidDoubleValue() throws IOException {
      JsonNode doubleNode = mapper.readTree("15.0");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.DOUBLE);
      Client.validateType(doubleNode, variable);
   }

   @Test
   public void testInvalidFloatValue() throws IOException {
      String input = "\"string\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.FLOAT;
      String expectedMessage = String
            .format(
                  "It is not a valid JSON %s value",
                  Question.InstanceData.Variable.Type.FLOAT.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidFloatValue() throws IOException {
      Float floatValue = 15.0f;
      JsonNode floatNode = mapper.valueToTree(floatValue);
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.FLOAT);
      Client.validateType(floatNode, variable);
   }

   @Test
   public void testInvalidLongValue() throws IOException {
      String input = "\"string\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.LONG;
      String expectedMessage = String
            .format(
                  "It is not a valid JSON %s value",
                  Question.InstanceData.Variable.Type.LONG.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidLongValue() throws IOException {
      Long longValue = 15L;
      JsonNode floatNode = mapper.valueToTree(longValue);
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.LONG);
      Client.validateType(floatNode, variable);
   }

   @Test
   public void testInvalidIPValue() throws IOException {
      String input = "\"0.0.0\"";
      Question.InstanceData.Variable.Type expectedType = IP;
      String expectedMessage = String.format("Invalid ip string: %s", input);
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidIPValue() throws IOException {
      JsonNode IPNode = mapper.readTree("\"0.0.0.0\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(IP);
      Client.validateType(IPNode, variable);
   }

   @Test
   public void testNonStringIpProtocolValue() throws IOException {
      String input = "10";
      Question.InstanceData.Variable.Type expectedType = IP_PROTOCOL;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string",
                  expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidIpProtocolValue() throws IOException {
      String input = "\"invalid\"";
      Question.InstanceData.Variable.Type expectedType = IP_PROTOCOL;
      String expectedMessage = String
            .format("Unknown %s string", expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidIpProtocolValue() throws IOException {
      JsonNode IpProtocolNode = mapper.readTree("\"visa\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(IP_PROTOCOL);
      Client.validateType(IpProtocolNode, variable);
   }

   @Test
   public void testNonStringIpWildcardValue() throws IOException {
      String input = "10";
      Question.InstanceData.Variable.Type expectedType = IP_WILDCARD;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string",
                  expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidIpWildcardValue() throws IOException {
      String input = "\"10.168.5.5:10.168.100.$\"";
      Question.InstanceData.Variable.Type expectedType = IP_WILDCARD;
      String expectedMessage = "Invalid ip segment: \"$\" in ip string: " +
            "\"10.168.100.$\"";
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidIpWildcardValue() throws IOException {
      JsonNode IpWildcardNode = mapper
            .readTree("\"10.168.5.5:10.168.100.100\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(IP_WILDCARD);
      Client.validateType(IpWildcardNode, variable);
   }

   @Test
   public void testNonStringPrefixValue() throws IOException {
      String input = "10";
      Question.InstanceData.Variable.Type expectedType = PREFIX;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string",
                  expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidPrefixValue() throws IOException {
      String input = "\"10.168.5.5/30/20\"";
      Question.InstanceData.Variable.Type expectedType = PREFIX;
      String expectedMessage = String
            .format("Invalid prefix string: %s", input);
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidPrefixValue() throws IOException {
      JsonNode prefixNode = mapper
            .readTree("\"10.168.5.5/30\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(PREFIX);
      Client.validateType(prefixNode, variable);
   }

   @Test
   public void testNonStringPrefixRangelValue() throws IOException {
      String input = "10";
      Question.InstanceData.Variable.Type expectedType = PREFIX_RANGE;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string",
                  expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidPrefixRangeValue() throws IOException {
      String input = "\"10.168.5.5/30:s10-50\"";
      Question.InstanceData.Variable.Type expectedType = PREFIX_RANGE;
      String expectedMessage = "Invalid subrange start: \"s10\"";
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidPrefixRangeValue() throws IOException {
      JsonNode prefixRangeNode = mapper
            .readTree("\"10.168.5.5/30:10-50\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(PREFIX_RANGE);
      Client.validateType(prefixRangeNode, variable);
   }

   @Test
   public void testNonStringOrIntSubRangeValue() throws IOException {
      String input = "false";
      Question.InstanceData.Variable.Type expectedType = SUBRANGE;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string or integer",
                  expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidSubRangeValue() throws IOException {
      String input = "\"10-s50\"";
      Question.InstanceData.Variable.Type expectedType = SUBRANGE;
      String expectedMessage = "Invalid subrange end: \"s50\"";
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidSubRangeStringValue() throws IOException {
      JsonNode subRangeNode = mapper
            .readTree("\"10-50\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(SUBRANGE);
      Client.validateType(subRangeNode, variable);
   }

   @Test
   public void testValidSubRangeIntegerValue() throws IOException {
      JsonNode subRangeNode = mapper
            .readTree("10");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(SUBRANGE);
      Client.validateType(subRangeNode, variable);
   }

   @Test
   public void testNonStringProtocolValue() throws IOException {
      String input = "10";
      Question.InstanceData.Variable.Type expectedType = PROTOCOL;
      String expectedMessage = String
            .format(
                  "A Batfish %s must be a JSON string",
                  expectedType.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testInvalidProtocolValue() throws IOException {
      String input = "\"missing\"";
      Question.InstanceData.Variable.Type expectedType = PROTOCOL;
      String expectedMessage = String
            .format("No %s with name: '%s'", Protocol.class.getSimpleName(),
                  mapper.readTree(input).textValue());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidProtocolValue() throws IOException {
      JsonNode prefixRangeNode = mapper
            .readTree("\"tcp\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(PROTOCOL);
      Client.validateType(prefixRangeNode, variable);
   }

   @Test
   public void testInvalidJavaRegexValue() throws IOException {
      String invalidJavaRegex = "\"...{\\\\Q8\\\\E}\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.JAVA_REGEX;
      String expectedMessage = "It is not a valid Java regular expression";
      validateTypeWithInvalidInput(invalidJavaRegex, expectedMessage,
            expectedType);
   }

   @Test
   public void testValidJavaRegexValue() throws IOException {
      JsonNode inputNode = mapper.readTree("\".*\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.JAVA_REGEX);
      Client.validateType(inputNode, variable);
   }

   @Test
   public void testInvalidJsonPathValue() throws IOException {
      String input = "{\"variable\" : \"I am variable\"}";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.JSON_PATH;
      String expectedMessage = String
            .format(
                  "Missing 'path' element of %s",
                  Question.InstanceData.Variable.Type.JSON_PATH.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidJsonPathValue() throws IOException {
      JsonNode JsonPathNode = mapper
            .readTree("{\"path\" : \"I am path.\", \"suffix\" : true}");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.JSON_PATH);
      Client.validateType(JsonPathNode, variable);
   }

   @Test
   public void testInvalidJsonPathRegexValue() throws IOException {
      String input = "\"/pathRegex\"";
      Question.InstanceData.Variable.Type expectedType
            = Question.InstanceData.Variable.Type.JSON_PATH_REGEX;
      String expectedMessage = String
            .format(
                  "A Batfish %s must end in either \"/\" or \"/i\"",
                  Question.InstanceData.Variable.Type.JSON_PATH_REGEX.getName());
      validateTypeWithInvalidInput(input, expectedMessage, expectedType);
   }

   @Test
   public void testValidJsonPathRegexValue() throws IOException {
      JsonNode JsonPathRegexNode = mapper.readTree("\"/.*/\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.JSON_PATH_REGEX);
      Client.validateType(JsonPathRegexNode, variable);
   }

   @Test
   public void testUnsatisfiedMinLengthValue() throws IOException {
      String shortString = "\"short\"";
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setMinLength(8);
      thrown.expect(BatfishException.class);
      thrown.expectMessage(equalTo(
            "Must be at least 8 characters in length"));
      Client.validateType(mapper.readTree(shortString), variable);
   }

   @Test
   public void testSatisfiedMinLengthValue() throws IOException {
      String longString = "\"long enough\"";
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setMinLength(8);
      variable.setType(STRING);
      Client.validateType(mapper.readTree(longString), variable);
   }

   // Tests for validateAndSet method
   @Test
   public void testValidateWithNullVariableInput() {
      Map<String, String> parameters = new HashMap<>();
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      parameters.put("integer", "10");
      variables.put("integer", null);
      thrown.expect(BatfishException.class);
      String errorMessage = "No variable named: 'integer' in supplied " +
            "question template";
      thrown.expectMessage(equalTo(errorMessage));
      Client.validateAndSet(parameters, variables);
   }

   @Test
   public void testValidateWithInvalidInput() {
      Map<String, String> parameters = new HashMap<>();
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      parameters.put("integer", "10");
      Question.InstanceData.Variable integerVariable
            = new Question.InstanceData.Variable();
      integerVariable.setType(Question.InstanceData.Variable.Type.INTEGER);
      variables.put("integer", integerVariable);
      parameters.put("boolean", "\"true\"");
      Question.InstanceData.Variable booleanVariable
            = new Question.InstanceData.Variable();
      booleanVariable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      variables.put("boolean", booleanVariable);
      thrown.expect(BatfishException.class);
      String errorMessage = "Invalid value for parameter boolean: \"true\"";
      thrown.expectMessage(equalTo(errorMessage));
      Client.validateAndSet(parameters, variables);
   }

   @Test
   public void testValidateWithValidInput() {
      Map<String, String> parameters = new HashMap<>();
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      parameters.put("integer", "10");
      Question.InstanceData.Variable integerVariable
            = new Question.InstanceData.Variable();
      integerVariable.setType(Question.InstanceData.Variable.Type.INTEGER);
      variables.put("integer", integerVariable);
      parameters.put("boolean", "true");
      Question.InstanceData.Variable booleanVariable
            = new Question.InstanceData.Variable();
      booleanVariable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      variables.put("boolean", booleanVariable);
      Client.validateAndSet(parameters, variables);
   }

   @Test
   public void testUnsatisfiedMinElementInput() {
      Map<String, String> parameters = new HashMap<>();
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      String jsonArray = "[\"action1\", \"action2\"]";
      parameters.put("actions", jsonArray);
      Question.InstanceData.Variable actionsVariable
            = new Question.InstanceData.Variable();
      actionsVariable.setType(Question.InstanceData.Variable.Type.STRING);
      actionsVariable.setMinElements(5);
      variables.put("actions", actionsVariable);
      thrown.expect(BatfishException.class);
      String errorMessage = String
            .format("Invalid value for parameter actions: %s. Expecting a " +
                  "JSON array of at least 5 elements", jsonArray);
      thrown.expectMessage(equalTo(errorMessage));
      Client.validateAndSet(parameters, variables);
   }

   @Test
   public void testSatisfiedMinElementInput() {
      Map<String, String> parameters = new HashMap<>();
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      String jsonArray = "[\"action1\", \"action2\", \"action3\", " +
            "\"action4\", \"action5\", \"action6\"]";
      parameters.put("actions", jsonArray);
      Question.InstanceData.Variable actionsVariable
            = new Question.InstanceData.Variable();
      actionsVariable.setType(Question.InstanceData.Variable.Type.STRING);
      actionsVariable.setMinElements(5);
      variables.put("actions", actionsVariable);
      Client.validateAndSet(parameters, variables);
   }

   // Tests for checkRequiredPara method
   @Test
   public void testMissingNonOptionalParameterNoValue() {
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      Question.InstanceData.Variable integerVariable
            = new Question.InstanceData.Variable();
      variables.put("integer", integerVariable);
      thrown.expect(BatfishException.class);
      String errorMessage = "Missing parameter: integer";
      thrown.expectMessage(equalTo(errorMessage));
      Client.checkVariableState(variables);
   }

   @Test
   public void testMissingOptionalParameterNoValue() {
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      Question.InstanceData.Variable integerVariable
            = new Question.InstanceData.Variable();
      integerVariable.setOptional(true);
      Client.checkVariableState(variables);
   }

   @Test
   public void testProvideNonOptionalParameterWithValue() throws IOException {
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      Question.InstanceData.Variable integerVariable
            = new Question.InstanceData.Variable();
      integerVariable.setValue(mapper.readTree("3"));
      variables.put("integer", integerVariable);
      Client.checkVariableState(variables);
   }

   @Test
   public void testProvideOptionalParameterWithValue() throws IOException {
      Map<String, Question.InstanceData.Variable> variables = new HashMap<>();
      Question.InstanceData.Variable integerVariable
            = new Question.InstanceData.Variable();
      integerVariable.setOptional(true);
      integerVariable.setValue(mapper.readTree("3"));
      variables.put("integer", integerVariable);
      Client.checkVariableState(variables);
   }

   // Tests for processCommand method
   private void checkProcessCommandErrorMessage(Command command, String[] parameters, String expected)
         throws Exception {
      Client client = new Client(new String[]{"-runmode", "gendatamodel"});
      File tempFile = folder.newFile("writer");
      FileWriter writer = new FileWriter(tempFile);
      client._logger = new BatfishLogger("output", false);
      String[] args = (String[])ArrayUtils.addAll(new String[]{command.commandName()}, parameters);
      assertFalse(client.processCommand(args, writer));
      assertThat(client.getLogger().getHistory().toString(500), equalTo(expected));
      writer.close();
   }

   private void testProcessCommandWithValidInput(Command command, String[] parameters,
         String expected) throws Exception {
      Client client = new Client(new String[]{"-runmode", "gendatamodel"});
      File tempFile = folder.newFile("writer");
      FileWriter writer = new FileWriter(tempFile);
      String[] args = (String[])ArrayUtils.addAll(new String[]{command.commandName()}, parameters);
      client._logger = new BatfishLogger("output", false);
      assertTrue(client.processCommand(args, writer));
      assertThat(client.getLogger().getHistory().toString(500), equalTo(expected));
      writer.close();
   }

   private void testInvalidInput(Command command, String[] parameters)
         throws Exception {
      Pair<String, String> usage = Command.getUsageMap().get(command);
      String expected = String
            .format("Invalid arguments: [] %s\n%s %s\n\t%s\n\n", Arrays.toString(parameters),
                  command.commandName(), usage.getFirst(), usage.getSecond());
      checkProcessCommandErrorMessage(command, parameters, expected);
   }

   private final String TESTRIG_NOT_SET = "Active testrig is not set.\nSpecify testrig on"
         + " command line (-testrigdir <testrigdir>) or use command (INIT_TESTRIG <testrigdir>)\n";

   private final String CONTAINER_NOT_SET = "Active container is not set\n";

   @Test
   public void testAddAnalysisQuestionInvalidParas() throws Exception {
      Command command = ADD_ANALYSIS_QUESTIONS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testInitAnalysisQuestionInvalidParas() throws Exception {
      Command command = INIT_ANALYSIS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testAddBatfishOptionInvalidParas() throws Exception {
      Command command = ADD_BATFISH_OPTION;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testAddBatfishOptionValidParas() throws Exception {
      Command command = ADD_BATFISH_OPTION;
      String[] parameters = new String[]{"parameter1"};
      testProcessCommandWithValidInput(command, parameters, "");
   }

   @Test
   public void testAnswerInvalidParas() throws Exception {
      Command command = ANSWER;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testAnswerValidParas() throws Exception {
      Command command = ANSWER;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testAnswerDeltaInvalidParas() throws Exception {
      Command command = ANSWER_DELTA;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testAnswerDeltaValidParas() throws Exception {
      Command command = ANSWER_DELTA;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testCatInvalidParas() throws Exception {
      Command command = CAT;
      String[] args = new String[]{command.commandName()};
      Pair<String, String> usage = Command.getUsageMap().get(command);
      String expected = String
            .format("Invalid arguments: %s\n%s %s\n\t%s\n\n", Arrays.toString(args),
                  command.commandName(), usage.getFirst(), usage.getSecond());
      checkProcessCommandErrorMessage(command, new String[]{}, expected);
   }

   @Test
   public void testCatValidParas() throws Exception {
      Command command = CAT;
      Path tempFilePath = folder.newFile("temp").toPath();
      String[] parameters = new String[]{tempFilePath.toString()};
      testProcessCommandWithValidInput(command, parameters, "");
   }

   @Test
   public void testCheckApiKeyInvalidParas() throws Exception {
      Command command = CHECK_API_KEY;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testClearScreenInvalidParas() throws Exception {
      Command command = CLEAR_SCREEN;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testDelAnalysisInvalidParas() throws Exception {
      Command command = DEL_ANALYSIS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelAnalysisValidParas() throws Exception {
      Command command = DEL_ANALYSIS;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testDelAnalysisQuestionInvalidParas() throws Exception {
      Command command = DEL_ANALYSIS_QUESTIONS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelAnalysisQuestionValidParas() throws Exception {
      Command command = DEL_ANALYSIS_QUESTIONS;
      String[] parameters = new String[]{"parameter1", "parameter2"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testDelBatfishOptionInvalidParas() throws Exception {
      Command command = DEL_BATFISH_OPTION;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelBatfishOptionValidParas() throws Exception {
      Command command = DEL_BATFISH_OPTION;
      String[] parameters = new String[]{"parameter1"};
      String expected = "Batfish option parameter1 does not exist\n";
      checkProcessCommandErrorMessage(command, parameters, expected);
   }

   @Test
   public void testDelContainerInvalidParas() throws Exception {
      Command command = DEL_CONTAINER;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelEnvironmentInvalidParas() throws Exception {
      Command command = DEL_ENVIRONMENT;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelEnvironmentValidParas() throws Exception {
      Command command = DEL_ENVIRONMENT;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testDelQuestionInvalidParas() throws Exception {
      Command command = DEL_QUESTION;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelQuestionValidParas() throws Exception {
      Command command = DEL_QUESTION;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testDelTestrigInvalidParas() throws Exception {
      Command command = DEL_TESTRIG;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testDelTestrigValidParas() throws Exception {
      Command command = DEL_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testDirInvalidParas() throws Exception {
      Command command = DIR;
      String[] parameters = new String[]{"parameter1", "parameter2"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testDirValidParas() throws Exception {
      Command command = DIR;
      Path tempFilePath = folder.newFolder("temp").toPath();
      String[] parameters = new String[]{tempFilePath.toString()};
      testProcessCommandWithValidInput(command, parameters, "");
   }

   @Test
   public void testGenerateDataplaneInvalidParas() throws Exception {
      Command command = GEN_DP;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testGenerateDataplaneValidParas() throws Exception {
      Command command = GEN_DP;
      checkProcessCommandErrorMessage(command, new String[]{}, TESTRIG_NOT_SET);
   }

   @Test
   public void testGenerateDataplaneDeltaInvalidParas() throws Exception {
      Command command = GEN_DELTA_DP;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testGenerateDataplaneDeltaValidParas() throws Exception {
      Command command = GEN_DELTA_DP;
      checkProcessCommandErrorMessage(command, new String[]{},
            "Active delta testrig is not set\n");
   }

   @Test
   public void testGetInvalidParas() throws Exception {
      Command command = GET;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testGetValidParas() throws Exception {
      Command command = GET;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetDeltaInvalidParas() throws Exception {
      Command command = GET_DELTA;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testGetDeltaValidParas() throws Exception {
      Command command = GET_DELTA;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetAnalysisAnswersInvalidParas() throws Exception {
      Command command = GET_ANALYSIS_ANSWERS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testGetAnalysisAnswersValidParas() throws Exception {
      Command command = GET_ANALYSIS_ANSWERS;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetAnalysisAnswersDeltaValidParas() throws Exception {
      Command command = GET_ANALYSIS_ANSWERS_DELTA;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetAnalysisAnswersDifferentialValidParas() throws Exception {
      Command command = GET_ANALYSIS_ANSWERS_DIFFERENTIAL;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetAnswersInvalidParas() throws Exception {
      Command command = GET_ANSWER;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testGetAnswersValidParas() throws Exception {
      Command command = GET_ANSWER;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetAnswersDeltaValidParas() throws Exception {
      Command command = GET_ANSWER_DELTA;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetAnswersDifferentialValidParas() throws Exception {
      Command command = GET_ANSWER_DIFFERENTIAL;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testGetQuestionInvalidParas() throws Exception {
      Command command = GET_QUESTION;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testGetQuestionValidParas() throws Exception {
      Command command = GET_QUESTION;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testHelpInvalidParas() throws Exception {
      Command command = HELP;
      String[] parameters = new String[]{"-option1"};
      Pair<String, String> usage = Command.getUsageMap().get(command);
      String expected = String
            .format("Invalid arguments: %s []\n%s %s\n\t%s\n\n", Arrays.toString(parameters),
                  command.commandName(), usage.getFirst(), usage.getSecond());
      checkProcessCommandErrorMessage(command, parameters, expected);
   }

   @Test
   public void testHelpValidParas() throws Exception {
      Command command = HELP;
      String[] parameters = new String[]{"get"};
      Pair<String, String> usage = Command.getUsageMap().get(GET);
      String expected = String.format("%s %s\n\t%s\n\n", GET.commandName(),
            usage.getFirst(), usage.getSecond());
      testProcessCommandWithValidInput(command, parameters, expected);
   }

   @Test
   public void testInitContainerInvalidParas() throws Exception {
      Command command = INIT_CONTAINER;
      String[] args = new String[]{command.commandName(),
            "parameter1", "parameter2", "parameter3"};
      Pair<String, String> usage = Command.getUsageMap().get(command);
      String expected = String
            .format("Invalid arguments: %s\n%s %s\n\t%s\n\n", Arrays.toString(args),
                  command.commandName(), usage.getFirst(), usage.getSecond());
      String[] parameters = new String[]{"parameter1", "parameter2", "parameter3"};
      checkProcessCommandErrorMessage(command, parameters, expected);
   }

   @Test
   public void testInitDeltaEnvInvalidParas() throws Exception {
      Command command = INIT_DELTA_ENV;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testInitDeltaEnvValidParas() throws Exception {
      Command command = INIT_DELTA_ENV;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testInitTestrigInvalidParas() throws Exception {
      Command command = INIT_TESTRIG;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testInitTestrigDeltaInvalidParas() throws Exception {
      Command command = INIT_DELTA_TESTRIG;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testListAnalysisInvalidParas() throws Exception {
      Command command = LIST_ANALYSES;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testListAnalysisValidParas() throws Exception {
      Command command = LIST_ANALYSES;
      String[] parameters = new String[]{};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testListContainersInvalidParas() throws Exception {
      Command command = LIST_CONTAINERS;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testListEnvironmentsInvalidParas() throws Exception {
      Command command = LIST_ENVIRONMENTS;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testListEnvironmentsValidParas() throws Exception {
      Command command = LIST_ENVIRONMENTS;
      checkProcessCommandErrorMessage(command, new String[]{}, TESTRIG_NOT_SET);
   }

   @Test
   public void testListQuestionsInvalidParas() throws Exception {
      Command command = LIST_QUESTIONS;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testListQuestionsValidParas() throws Exception {
      Command command = LIST_QUESTIONS;
      String[] parameters = new String[]{};
      checkProcessCommandErrorMessage(command, new String[]{}, TESTRIG_NOT_SET);
   }

   @Test
   public void testListTestrigsInvalidParas() throws Exception {
      Command command = LIST_TESTRIGS;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testLoadQuestionsInvalidParas() throws Exception {
      Command command = LOAD_QUESTIONS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testLoadQuestionsValidParas() throws Exception {
      Command command = LOAD_QUESTIONS;
      Path tempFilePath = folder.newFolder("temp").toPath();
      String[] parameters = new String[]{tempFilePath.toString()};
      testProcessCommandWithValidInput(command, parameters, "");
   }

   @Test
   public void testPromptInvalidParas() throws Exception {
      Command command = PROMPT;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testPromtValidParas() throws Exception {
      Command command = PROMPT;
      testProcessCommandWithValidInput(command, new String[]{}, "");
   }

   @Test
   public void testPwdInvalidParas() throws Exception {
      Command command = PWD;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testPwdValidParas() throws Exception {
      Command command = PWD;
      testProcessCommandWithValidInput(command, new String[]{},
            String.format("working directory = %s\n", System.getProperty("user.dir")));
   }

   @Test
   public void testReinitTestrigInvalidParas() throws Exception {
      Command command = REINIT_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testReinitTestrigDeltaInvalidParas() throws Exception {
      Command command = REINIT_DELTA_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testRunAnalysisInvalidParas() throws Exception {
      Command command = RUN_ANALYSIS;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testRunAnalysisValidParas() throws Exception {
      Command command = RUN_ANALYSIS;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testRunAnalysisDeltaValidParas() throws Exception {
      Command command = RUN_ANALYSIS_DELTA;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testRunAnalysisDifferentialValidParas() throws Exception {
      Command command = RUN_ANALYSIS_DIFFERENTIAL;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testSetBatfishLogLevelInvalidParas() throws Exception {
      Command command = SET_BATFISH_LOGLEVEL;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetBatfishLogLevelValidParas() throws Exception {
      Command command = SET_BATFISH_LOGLEVEL;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters,
            "Undefined loglevel value: parameter1\n");
   }

   @Test
   public void testSetContainerInvalidParas() throws Exception {
      Command command = SET_CONTAINER;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetContainerValidParas() throws Exception {
      Command command = SET_CONTAINER;
      String[] parameters = new String[]{"parameter1"};
      testProcessCommandWithValidInput(command, parameters,
            String.format("Active container is now set to %s\n", parameters[0]));
   }

   @Test
   public void testSetDeltaEnvInvalidParas() throws Exception {
      Command command = SET_DELTA_ENV;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetDeltaEnvValidParas() throws Exception {
      Command command = SET_DELTA_ENV;
      String[] parameters = new String[]{"parameter1"};
      testProcessCommandWithValidInput(command, parameters,
            String.format("Active delta testrig->environment is now null->%s\n", parameters[0]));
   }

   @Test
   public void testSetEnvInvalidParas() throws Exception {
      Command command = SET_ENV;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetEnvValidParas() throws Exception {
      Command command = SET_ENV;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testSetDeltaTestrigInvalidParas() throws Exception {
      Command command = SET_DELTA_TESTRIG;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetDeltaTestrigValidParas() throws Exception {
      Command command = SET_DELTA_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      testProcessCommandWithValidInput(command, parameters,
            String.format("Delta testrig->env is now %s->env_default\n", parameters[0]));
   }

   @Test
   public void testSetLogLevelInvalidParas() throws Exception {
      Command command = SET_LOGLEVEL;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetLogLevelValidParas() throws Exception {
      Command command = SET_LOGLEVEL;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters,
            "Undefined loglevel value: parameter1\n");
   }

   @Test
   public void testSetPrettyPrintInvalidParas() throws Exception {
      Command command = SET_PRETTY_PRINT;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetPrettyPrintValidParas() throws Exception {
      Command command = SET_PRETTY_PRINT;
      String[] parameters = new String[]{"true"};
      testProcessCommandWithValidInput(command, parameters,
            "Set pretty printing answers to true\n");
   }

   @Test
   public void testSetTestrigInvalidParas() throws Exception {
      Command command = SET_TESTRIG;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testSetTestrigValidParas() throws Exception {
      Command command = SET_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      checkProcessCommandErrorMessage(command, parameters, CONTAINER_NOT_SET);
   }

   @Test
   public void testShowApiKeyInvalidParas() throws Exception {
      Command command = SHOW_API_KEY;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowApiKeyValidParas() throws Exception {
      Command command = SHOW_API_KEY;
      testProcessCommandWithValidInput(command, new String[]{},
            String.format("Current API Key is %s\n", DEFAULT_API_KEY));
   }

   @Test
   public void testShowBatfishLogLevelInvalidParas() throws Exception {
      Command command = SHOW_BATFISH_LOGLEVEL;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowBatfishLogLevelValidParas() throws Exception {
      Command command = SHOW_BATFISH_LOGLEVEL;
      testProcessCommandWithValidInput(command, new String[]{},
            "Current batfish log level is warn\n");
   }

   @Test
   public void testShowBatfishOptionsInvalidParas() throws Exception {
      Command command = SHOW_BATFISH_OPTIONS;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowBatfishOptionsValidParas() throws Exception {
      Command command = SHOW_BATFISH_OPTIONS;
      testProcessCommandWithValidInput(command, new String[]{},
            "There are 0 additional batfish options\n");
   }

   @Test
   public void testShowContainerInvalidParas() throws Exception {
      Command command = SHOW_CONTAINER;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowConrainerValidParas() throws Exception {
      Command command = SHOW_CONTAINER;
      testProcessCommandWithValidInput(command, new String[]{},
            "Current container is null\n");
   }

   @Test
   public void testShowCoordinatorHostInvalidParas() throws Exception {
      Command command = SHOW_COORDINATOR_HOST;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowCoordinatorHostValidParas() throws Exception {
      Command command = SHOW_COORDINATOR_HOST;
      testProcessCommandWithValidInput(command, new String[]{},
            "Current coordinator host is localhost\n");
   }

   @Test
   public void testShowDeltaTestrigInvalidParas() throws Exception {
      Command command = SHOW_DELTA_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowDeltaTestrigValidParas() throws Exception {
      Command command = SHOW_DELTA_TESTRIG;
      checkProcessCommandErrorMessage(command, new String[]{},
            "Active delta testrig is not set\n");
   }

   @Test
   public void testShowLogLevelInvalidParas() throws Exception {
      Command command = SHOW_LOGLEVEL;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowLogLevelValidParas() throws Exception {
      Command command = SHOW_LOGLEVEL;
      testProcessCommandWithValidInput(command, new String[]{},
            "Current client log level is output\n");
   }

   @Test
   public void testShowTestrigInvalidParas() throws Exception {
      Command command = SHOW_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testShowTestrigValidParas() throws Exception {
      Command command = SHOW_TESTRIG;
      String[] parameters = new String[]{};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testShowVersionInvalidParas() throws Exception {
      Command command = SHOW_TESTRIG;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void checkTestInvalidParas() throws Exception {
      Command command = TEST;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void checkTestValidParas() throws Exception {
      Command command = TEST;
      Path tempFilePath = folder.newFolder("temp").toPath();
      String[] parameters = new String[]{tempFilePath.toString(), GET.commandName()};
      Pair<String, String> usage = Command.getUsageMap().get(GET);
      String expected = String
            .format("Invalid arguments: [] []\n%s %s\n\t%s\n\n",
                  GET.commandName(), usage.getFirst(), usage.getSecond());
      String additionalMessage = String.
            format("Test: 'get' matches %s: Fail\nCopied output to %s.testout\n",
                  tempFilePath.toString(), tempFilePath.toString());
      testProcessCommandWithValidInput(command, parameters,
            expected + additionalMessage);
   }

   @Test
   public void testUploadCustomObjectInvalidParas() throws Exception {
      Command command = UPLOAD_CUSTOM_OBJECT;
      testInvalidInput(command, new String[]{});
   }

   @Test
   public void testUploadCustomObjectValidParas() throws Exception {
      Command command = UPLOAD_CUSTOM_OBJECT;
      String[] parameters = new String[]{"parameter1", "parameter2"};
      checkProcessCommandErrorMessage(command, parameters, TESTRIG_NOT_SET);
   }

   @Test
   public void testExitInvalidParas() throws Exception {
      Command command = EXIT;
      String[] parameters = new String[]{"parameter1"};
      testInvalidInput(command, parameters);
   }

   @Test
   public void testExitValidParas() throws Exception {
      Command command = EXIT;
      testProcessCommandWithValidInput(command, new String[]{}, "");
   }

   @Test
   public void testDefaultCase() throws Exception {
      Client client = new Client(new String[]{"-runmode", "gendatamodel"});
      File tempFile = folder.newFile("writer");
      FileWriter writer = new FileWriter(tempFile);
      client._logger = new BatfishLogger("output", false);
      String[] args = new String[]{"non-exist command"};
      String expected = "Command failed: Not a valid command: \"non-exist command\"\n";
      assertFalse(client.processCommand(args, writer));
      assertThat(client.getLogger().getHistory().toString(500), equalTo(expected));
   }
}