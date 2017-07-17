package org.batfish.client;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.Question;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link org.batfish.client.Client}.
 */
public class ClientTest {

   private BatfishObjectMapper mapper;

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

}