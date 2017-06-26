package org.batfish.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Question;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

/**
 * Tests for {@link org.batfish.client.Client}.
 */
public class ClientTest {

   BatfishObjectMapper mapper;

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Before
   public void initMapper() {
      mapper = new BatfishObjectMapper();
   }

   @Test
   public void testNullValue() {
      //Need to check if null input is allowed, otherwise verify before entering
      // method
      JsonNode nullNode = null;
      Question.InstanceData.Variable variable = new Question.InstanceData.Variable();
      thrown.expect(NullPointerException.class);
      Client.validate(nullNode, variable);
   }

   @Test
   public void testUnStringInputWhenExpectString() throws IOException {
      JsonNode numberNode = mapper.readTree("10");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.STRING);
      thrown.expect(BatfishException.class);
      thrown.expectMessage("The parameter value type: NUMBER doesn't match " +
            "the type required for variable type: STRING");
      Client.validate(numberNode, variable);
   }

   @Test
   public void testInvalidBooleanValue() throws IOException {
      JsonNode invalidBooleanNode = mapper.readTree("\"true\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a boolean for variable type: BOOLEAN");
      Client.validate(invalidBooleanNode, variable);
   }

   @Test
   public void testValidBooleanValue() throws IOException {
      JsonNode booleanNode = mapper.readTree("true");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.BOOLEAN);
      try {
         Client.validate(booleanNode, variable);
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
      thrown.expectMessage("Expected a comparator for " +
            "variable type: COMPARATOR");
      Client.validate(invalidComparator, variable);
   }

   @Test
   public void testValidComparatorValue() throws IOException {
      JsonNode comparatorNode = mapper.readTree("\">=\"");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.COMPARATOR);
      try {
         Client.validate(comparatorNode, variable);
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
      thrown.expectMessage("Expected an Integer for " +
            "variable type: INTEGER");
      Client.validate(invalidInteger, variable);
   }

   @Test
   public void testValidIntegerValue() throws IOException {
      JsonNode comparatorNode = mapper.readTree("15");
      Question.InstanceData.Variable variable
            = new Question.InstanceData.Variable();
      variable.setType(Question.InstanceData.Variable.Type.INTEGER);
      try {
         Client.validate(comparatorNode, variable);
      } catch (Exception e) {
         throw new BatfishException("Unexpected exception");
      }
   }

   //Tests for validateJsonPath
   @Test
   public void testNullJsonPath () {
      thrown.expect(BatfishException.class);
      thrown.expectMessage("JsonPath should not be empty or null.");
      Client.validateJsonPath(null);
   }

   @Test
   public void testEmptyJsonPath () {
      thrown.expect(BatfishException.class);
      thrown.expectMessage("JsonPath should not be empty or null.");
      Client.validateJsonPath("");
   }

   @Test
   public void testInvalidJsonPath () {
      String invalidJsonPath = "\"variable\" : \"I am variable\"";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a jsonPath dictionary with " +
            "elements 'path' (string) and optional 'suffix' (boolean)");
      Client.validateJsonPath(invalidJsonPath);
   }

   @Test
   public void testJsonPathNoPathAtrribute () {
      String invalidJsonPath = "{\"variable\" : \"I am variable\"}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Missing 'path' element of jsonPath");
      Client.validateJsonPath(invalidJsonPath);
   }

   @Test
   public void testNotStringPath () {
      String invalidJsonPath = "{\"path\" : 1}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a String for variable type: path");
      Client.validateJsonPath(invalidJsonPath);
   }

   @Test
   public void testNestedContainerPathValue() {
      String invalidJsonPath = "{\"path\" : {\"innerVariable\" : \"content\"}}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a String for variable type: path");
      Client.validateJsonPath(invalidJsonPath);
   }

   @Test
   public void testJsonPathNotBooleanSuffix() {
      String invalidJsonPath = "{\"path\" : \"I am path.\", \"suffix\" : \"I " +
            "am suffix.\"}";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("'suffix' element of jsonPath dictionary should " +
            "be a boolean");
      Client.validateJsonPath(invalidJsonPath);
   }

   @Test
   public void testValidJsonPath() {
      String invalidJsonPath = "{\"path\" : \"I am path.\", \"suffix\" : true}";
      try {
         Client.validateJsonPath(invalidJsonPath);
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
      thrown.expectMessage("Expected jsonPathRegex start with: /");
      Client.validateJsonPathRegex(invalidStart);
   }

   @Test
   public void testPathRegexInvalidEnd () {
      String invalidEnd= "/pathRegex";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a json path regex" +
            "ends in either '/' or '/i'");
      Client.validateJsonPathRegex(invalidEnd);
   }

   @Test
   public void testInvalidInteriorJavaRegex () {
      String invalidJavaRegex = "/...{\\\\Q8\\\\E}/";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Invalid javaRegex at interior "
            + "of jsonPathRegex: " + "...{\\\\Q8\\\\E}");
      Client.validateJsonPathRegex(invalidJavaRegex);
   }

   @Test
   public void testPathRegexWithOnlySlash () {
      //Need to confirm the requirement
      String jsonPathRegex = "/i";
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Expected a json path regex" +
         "ends in either '/' or '/i'");
      Client.validateJsonPathRegex(jsonPathRegex);
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