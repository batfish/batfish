package org.batfish.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.node.IntNode;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QuestionHelperTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void fillTemplate() throws JSONException, IOException {
    JSONObject template =
        new JSONObject(readResource("org/batfish/client/goodTemplate.json", UTF_8));
    JSONObject filledTempate =
        QuestionHelper.fillTemplate(
            template, ImmutableSortedMap.of("parameter1", new IntNode(2)), "qname");
    QuestionHelperTestQuestion question =
        (QuestionHelperTestQuestion) Question.parseQuestion(filledTempate.toString());

    // the mandatory parameter should get the value we gave, and the optional one should get default
    assertThat(question.getParameterMandatory(), equalTo(2));
    assertThat(question.getParameterOptional(), equalTo(QuestionHelperTestQuestion.DEFAULT_VALUE));
  }

  @Test
  public void validateTemplateExtraParameter() throws JSONException, IOException {
    JSONObject template =
        new JSONObject(readResource("org/batfish/client/extraParameter.json", UTF_8));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Unrecognized field");

    QuestionHelper.validateTemplate(
        template,
        ImmutableSortedMap.of("parameter1", new IntNode(2), "parameter2", new IntNode(2)));
  }

  @Test
  public void validateTemplateExtraVariable() throws JSONException, IOException {
    JSONObject template =
        new JSONObject(readResource("org/batfish/client/extraVariable.json", UTF_8));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Unused variable");

    QuestionHelper.validateTemplate(
        template,
        ImmutableSortedMap.of("parameter1", new IntNode(1), "parameter2EXTRA", new IntNode(2)));
  }

  @Test
  public void validateTemplateSuccess() throws JSONException, IOException {
    JSONObject template =
        new JSONObject(readResource("org/batfish/client/goodTemplate.json", UTF_8));

    QuestionHelperTestQuestion question =
        (QuestionHelperTestQuestion)
            QuestionHelper.validateTemplate(
                template,
                ImmutableSortedMap.of("parameter1", new IntNode(1), "parameter2", new IntNode(3)));

    assertThat(question.getParameterMandatory(), equalTo(1));
    assertThat(question.getParameterOptional(), equalTo(3));
  }

  @Test
  public void validateTemplateUnexercisedVariable() throws JSONException, IOException {
    JSONObject template =
        new JSONObject(readResource("org/batfish/client/goodTemplate.json", UTF_8));

    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Template validation should exercise all variables");

    QuestionHelper.validateTemplate(template, ImmutableSortedMap.of("parameter1", new IntNode(1)));
  }
}
