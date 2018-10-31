package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Assertion.AssertionType;
import org.junit.Before;
import org.junit.Test;

/** Contains tests of {@link Question#configureTemplate(Question, String, String)}. */
public class QuestionTest {

  private Question _question;

  @Before
  public void initializeQuestion() {
    // Set up _question with no exclusions or assertion
    _question =
        new Question() {
          @Override
          public boolean getDataPlane() {
            return false;
          }

          @Override
          public String getName() {
            return "q";
          }
        };
  }

  @Test
  public void testConfigureExclusions() {
    // Add an exclusion and test that it was properly added
    JsonNode exclusion =
        BatfishObjectMapper.mapper().createObjectNode().set("col", new TextNode("val"));
    Question.configureTemplate(_question, getExclusionsStr(exclusion), null);

    assertThat(_question.getExclusions(), hasSize(1));
    assertThat(_question.getExclusions().get(0).getExclusion(), equalTo(exclusion));

    // Change the exclusion and test again
    JsonNode exclusion2 =
        BatfishObjectMapper.mapper().createObjectNode().set("col2", new TextNode("val2"));
    Question.configureTemplate(_question, getExclusionsStr(exclusion2), null);

    assertThat(_question.getExclusions(), hasSize(1));
    assertThat(_question.getExclusions().get(0).getExclusion(), equalTo(exclusion2));
  }

  @Test
  public void testConfigureEmptyExclusions() {
    // Add an exclusion
    JsonNode exclusion =
        BatfishObjectMapper.mapper().createObjectNode().set("col", new TextNode("val"));
    Question.configureTemplate(_question, getExclusionsStr(exclusion), null);
    assertThat(_question.getExclusions(), hasSize(1));

    // Remove the exclusion with empty list string as exclusions parameter
    Question.configureTemplate(_question, "[]", null);
    assertThat(_question.getExclusions(), hasSize(0));
  }

  @Test
  public void testConfigureNullExclusions() {
    // Add an exclusion
    JsonNode exclusion =
        BatfishObjectMapper.mapper().createObjectNode().set("col", new TextNode("val"));
    Question.configureTemplate(_question, getExclusionsStr(exclusion), null);

    // Configure template with null exclusion and make sure exclusion doesn't change
    Question.configureTemplate(_question, null, null);
    assertThat(_question.getExclusions(), hasSize(1));
    assertThat(_question.getExclusions().get(0).getExclusion(), equalTo(exclusion));
  }

  @Test
  public void testConfigureAssertion() {
    // Add an assertion and test that it is correctly configured
    Question.configureTemplate(_question, null, getAssertionStr(0));
    assertThat(
        _question.getAssertion(),
        equalTo(new Assertion(AssertionType.countequals, new IntNode(0))));

    // Change the assertion and test again
    Question.configureTemplate(_question, null, getAssertionStr(1));
    assertThat(
        _question.getAssertion(),
        equalTo(new Assertion(AssertionType.countequals, new IntNode(1))));
  }

  @Test
  public void testConfigureEmptyAssertion() {
    // Add an assertion
    Question.configureTemplate(_question, null, getAssertionStr(0));
    assertThat(_question.getAssertion(), notNullValue());

    // Remove the assertion with empty string as assertion parameter
    Question.configureTemplate(_question, null, "");
    assertThat(_question.getAssertion(), nullValue());

    // Re-add the assertion
    Question.configureTemplate(_question, null, getAssertionStr(0));
    assertThat(_question.getAssertion(), notNullValue());

    // Remove the assertion with empty object string as assertion parameter
    Question.configureTemplate(_question, null, "{}");
    assertThat(_question.getAssertion(), nullValue());
  }

  @Test
  public void testConfigureNullAssertion() {
    // Add an assertion
    Question.configureTemplate(_question, null, getAssertionStr(0));

    // Configure template with null assertion and make sure assertion doesn't change
    Question.configureTemplate(_question, null, null);
    assertThat(
        _question.getAssertion(),
        equalTo(new Assertion(AssertionType.countequals, new IntNode(0))));
  }

  // These methods catch JsonProcessingExceptions so tests don't need to declare they throw them

  private String getAssertionStr(int numExpected) {
    try {
      Assertion assertion = new Assertion(AssertionType.countequals, new IntNode(numExpected));
      return BatfishObjectMapper.writeString(assertion);
    } catch (JsonProcessingException e) {
      throw new BatfishException("failed parsing assertion", e);
    }
  }

  private String getExclusionsStr(JsonNode exclusionNode) {
    try {
      Exclusion exclusion = new Exclusion(null, (ObjectNode) exclusionNode);
      return BatfishObjectMapper.writeString(ImmutableList.of(exclusion));
    } catch (JsonProcessingException e) {
      throw new BatfishException("failed parsing exclusion", e);
    }
  }
}
