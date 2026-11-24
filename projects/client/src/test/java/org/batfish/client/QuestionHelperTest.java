package org.batfish.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Question;
import org.junit.jupiter.api.Test;

class QuestionHelperTest {

  @Test
  void fillTemplate() throws IOException {
    ObjectNode template =
        (ObjectNode)
            BatfishObjectMapper.mapper()
                .readTree(readResource("org/batfish/client/goodTemplate.json", UTF_8));
    ObjectNode filledTempate =
        QuestionHelper.fillTemplate(
            template, ImmutableSortedMap.of("parameter1", new IntNode(2)), "qname");
    QuestionHelperTestQuestion question =
        (QuestionHelperTestQuestion) Question.parseQuestion(filledTempate.toString());

    // the mandatory parameter should get the value we gave, and the optional one should get default
    assertThat(question.getParameterMandatory(), equalTo(2));
    assertThat(question.getParameterOptional(), equalTo(QuestionHelperTestQuestion.DEFAULT_VALUE));
  }

  @Test
  void fillTemplateJsonObject() throws IOException {
    ObjectNode template =
        (ObjectNode)
            BatfishObjectMapper.mapper()
                .readTree(readResource("org/batfish/client/jsonObjectTemplate.json", UTF_8));
    ObjectNode jsonObject = BatfishObjectMapper.mapper().createObjectNode();
    ArrayNode array = jsonObject.put("string", "value").putObject("nested").putArray("array");
    array.add(1).add(true).addNull();

    ObjectNode filledTemplate =
        QuestionHelper.fillTemplate(
            template, ImmutableSortedMap.of("parameterJsonObject", jsonObject), "qname");
    QuestionHelperTestQuestion question =
        (QuestionHelperTestQuestion) Question.parseQuestion(filledTemplate.toString());

    assertThat(question.getParameterJsonObject(), equalTo(jsonObject));
  }

  @Test
  void validateTemplateExtraParameter() throws IOException {
    ObjectNode template =
        (ObjectNode)
            BatfishObjectMapper.mapper()
                .readTree(readResource("org/batfish/client/extraParameter.json", UTF_8));

    Throwable exception =
        assertThrows(
            BatfishException.class,
            () ->
                QuestionHelper.validateTemplate(
                    template,
                    ImmutableSortedMap.of(
                        "parameter1", new IntNode(2), "parameter2", new IntNode(2))));
    assertThat(exception.getMessage(), containsString("Unrecognized field"));
  }

  @Test
  void validateTemplateExtraVariable() throws IOException {
    ObjectNode template =
        (ObjectNode)
            BatfishObjectMapper.mapper()
                .readTree(readResource("org/batfish/client/extraVariable.json", UTF_8));

    Throwable exception =
        assertThrows(
            BatfishException.class,
            () ->
                QuestionHelper.validateTemplate(
                    template,
                    ImmutableSortedMap.of(
                        "parameter1", new IntNode(1), "parameter2EXTRA", new IntNode(2))));
    assertThat(exception.getMessage(), containsString("Unused variable"));
  }

  @Test
  void validateTemplateSuccess() throws IOException {
    ObjectNode template =
        (ObjectNode)
            BatfishObjectMapper.mapper()
                .readTree(readResource("org/batfish/client/goodTemplate.json", UTF_8));

    QuestionHelperTestQuestion question =
        (QuestionHelperTestQuestion)
            QuestionHelper.validateTemplate(
                template,
                ImmutableSortedMap.of("parameter1", new IntNode(1), "parameter2", new IntNode(3)));

    assertThat(question.getParameterMandatory(), equalTo(1));
    assertThat(question.getParameterOptional(), equalTo(3));
  }

  @Test
  void validateTemplateUnexercisedVariable() throws IOException {
    ObjectNode template =
        (ObjectNode)
            BatfishObjectMapper.mapper()
                .readTree(readResource("org/batfish/client/goodTemplate.json", UTF_8));

    Throwable exception =
        assertThrows(
            BatfishException.class,
            () ->
                QuestionHelper.validateTemplate(
                    template, ImmutableSortedMap.of("parameter1", new IntNode(1))));
    assertThat(
        exception.getMessage(),
        containsString("Template validation should exercise all variables"));
  }
}
