package org.batfish.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Question;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.IOException;

/**
 * Tests for {@link org.batfish.client.Client}.
 */
public class ClientTest {

   BatfishObjectMapper mapper;
   String errorMessage;

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Before
   public void initMapper() {
      mapper = new BatfishObjectMapper();
   }

   public String generateErrorMessage (String value, JsonNodeType inputType,
          Question.InstanceData.Variable.Type expectedType) {
      String errorMessage
            = String.format("The parameter value: %s is a type of %s doesn't " +
                        "match expected variable type: %s",
                        value, inputType, expectedType);
      return errorMessage;
   }

   @Test
   public void testNullValue() {
      JsonNode nullNode = null;
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      thrown.expect(InvalidStateException.class);
      thrown.expectMessage("The parameter value should not be null");
      Client.validateType(nullNode, variable);
   }

   @Test
   public void testUnStringInputWhenExpectString() throws IOException {
      JsonNode numberNode = mapper.readTree("10");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.STRING);
      thrown.expect(BatfishException.class);
      errorMessage = generateErrorMessage("10", numberNode.getNodeType(),
            variable.getType());
      thrown.expectMessage(errorMessage);
      Client.validateType(numberNode, variable);
   }

   @Test
   public void testInvalidBooleanValue() throws IOException {
      JsonNode invalidBooleanNode = mapper.readTree("\"true\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      thrown.expect(BatfishException.class);
      errorMessage = generateErrorMessage("\"true\"",
            invalidBooleanNode.getNodeType(), variable.getType());
      thrown.expectMessage(errorMessage);
      Client.validateType(invalidBooleanNode, variable);
   }

   @Test
   public void testValidBooleanValue() throws IOException {
      JsonNode booleanNode = mapper.readTree("true");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      try {
         Client.validateType(booleanNode, variable);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   @Test
   public void testInvalidComparatorValue() throws IOException {
      JsonNode invalidComparator = mapper.readTree("\"=>\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.COMPARATOR);
      thrown.expect(BatfishException.class);
      errorMessage = generateErrorMessage("\"=>\"",
            invalidComparator.getNodeType(), variable.getType());
      thrown.expectMessage(errorMessage);
      Client.validateType(invalidComparator, variable);
   }

   @Test
   public void testValidComparatorValue() throws IOException {
      JsonNode comparatorNode = mapper.readTree("\">=\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.COMPARATOR);
      try {
         Client.validateType(comparatorNode, variable);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   @Test
   public void testInvalidIntegerValue() throws IOException {
      JsonNode invalidInteger = mapper.readTree("1.5");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.INTEGER);
      thrown.expect(BatfishException.class);
      errorMessage = generateErrorMessage("1.5",
            invalidInteger.getNodeType(), variable.getType());
      thrown.expectMessage(errorMessage);
      Client.validateType(invalidInteger, variable);
   }

   @Test
   public void testValidIntegerValue() throws IOException {
      JsonNode comparatorNode = mapper.readTree("15");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.INTEGER);
      try {
         Client.validateType(comparatorNode, variable);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   @Test
   public void testInvalidIpValue() throws IOException {
      JsonNode invalidInteger = mapper.readTree("\"0.0.0\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.IP);
      thrown.expect(BatfishException.class);
      thrown.expectMessage(
            "The input: 0.0.0 is not a valid ip address.");
      Client.validateType(invalidInteger, variable);
   }

   @Test
   public void testValidIpValue() throws IOException {
      JsonNode comparatorNode = mapper.readTree("\"0.0.0.0\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.IP);
      try {
         Client.validateType(comparatorNode, variable);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   //Tests for validateJsonPath
   @Test
   public void testEmptyJsonPath () throws IOException {
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a jsonPath dictionary with " +
            "elements 'path' (string) and optional 'suffix' (boolean)");
      Client.validateJsonPath(mapper.readTree("\"\""));
   }

   @Test
   public void testInvalidJsonPath () throws IOException {
      String invalidJsonPath = "\"variable\" : \"I am variable\"";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a jsonPath dictionary with " +
            "elements 'path' (string) and optional 'suffix' (boolean)");
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testJsonPathNoPathAtrribute () throws IOException {
      String invalidJsonPath = "{\"variable\" : \"I am variable\"}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Missing 'path' element of jsonPath");
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testNotStringPath () throws IOException {
      String invalidJsonPath = "{\"path\" : 1}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a String for variable type: path");
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testNestedContainerPathValue() throws IOException {
      String invalidJsonPath = "{\"path\" : {\"innerVariable\" : \"content\"}}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a String for variable type: path");
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testJsonPathNotBooleanSuffix() throws IOException {
      String invalidJsonPath = "{\"path\" : \"I am path.\", \"suffix\" : \"I " +
            "am suffix.\"}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("'suffix' element of jsonPath dictionary should " +
            "be a boolean");
      Client.validateJsonPath(mapper.readTree(invalidJsonPath));
   }

   @Test
   public void testValidJsonPath() {
      String validJsonPath = "{\"path\" : \"I am path.\", \"suffix\" : true}";
      try {
         Client.validateJsonPath(mapper.readTree(validJsonPath));
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   //Tests for validateJsonPathRegex
   @Test
   public void testNullJsonPathRegex () {
      thrown.expect(BatfishException.class);
      thrown.expectMessage("JsonPathRegex should not be empty or null.");
      Client.validateJsonPathRegex(null);
   }

   @Test
   public void testEmptyJsonPathRegex () {
      thrown.expect(BatfishException.class);
      thrown.expectMessage("JsonPathRegex should not be empty or null.");
      Client.validateJsonPathRegex("");
   }

   @Test
   public void testPathRegexInvalidStart () {
      String invalidStart = "pathRegex";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a jsonPathRegex starts with: /");
      Client.validateJsonPathRegex(invalidStart);
   }

   @Test
   public void testPathRegexInvalidEnd () {
      String invalidEnd= "/pathRegex";
      thrown.expect(BatfishException.class);
      thrown.expectMessage(
            "Expected a jsonPathRegex ends in either '/' or '/i'");
      Client.validateJsonPathRegex(invalidEnd);
   }

   @Test
   public void testInvalidInteriorJavaRegex () {
      String invalidJavaRegex = "/...{\\\\Q8\\\\E}/";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Invalid javaRegex at interior of jsonPathRegex:" +
            " ...{\\\\Q8\\\\E}");
      Client.validateJsonPathRegex(invalidJavaRegex);
   }

   @Test
   public void testPathRegexWithOnlySlash () {
      String jsonPathRegex = "/";
      try {
         Client.validateJsonPathRegex(jsonPathRegex);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   @Test
   public void testValidPathRegex () {
      String jsonPathRegex = "/.*/";
      try {
         Client.validateJsonPathRegex(jsonPathRegex);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }
}