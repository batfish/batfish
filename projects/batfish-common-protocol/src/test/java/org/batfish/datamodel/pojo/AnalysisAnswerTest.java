package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.answers.Answer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link AnalysisAnswer}. */
@RunWith(JUnit4.class)
public class AnalysisAnswerTest {

  @Test
  public void testConstructorAndGetter() {
    Environment environment = new Environment("env", null, null, null, null, null, "");
    List<Answer> answers = Lists.newArrayList(Answer.failureAnswer("failAnswer", null));
    AnalysisAnswer analysisAnswer = new AnalysisAnswer("analysis", environment, answers);
    assertThat(analysisAnswer.getName(), equalTo("analysis"));
    assertThat(analysisAnswer.getEnvironment(), equalTo(environment));
    assertThat(analysisAnswer.getAnswers(), equalTo(answers));
  }

  @Test
  public void testToString() {
    AnalysisAnswer analysisAnswer = new AnalysisAnswer("analysis", null, Lists.newArrayList());
    assertThat(
        analysisAnswer.toString(),
        equalTo("AnalysisAnswer{analysisName=analysis, environment=null, answers=[]}"));
    Environment environment = new Environment("env", null, null, null, null, null, null);
    List<Answer> answers = Lists.newArrayList(Answer.failureAnswer("failAnswer", null));
    analysisAnswer = new AnalysisAnswer("analysis", environment, answers);
    assertThat(
        analysisAnswer.toString(),
        equalTo(
            String.format(
                "AnalysisAnswer{analysisName=analysis, environment=%s, answers=%s}",
                environment, answers)));
  }

  @Test
  public void testEquals() {
    Environment environment = new Environment("env", null, null, null, null, null, "");
    AnalysisAnswer analysisAnswer = new AnalysisAnswer("foo", null, Lists.newArrayList());
    AnalysisAnswer analysisAnswerCopy = new AnalysisAnswer("foo", null, Lists.newArrayList());
    AnalysisAnswer aWithEnvironment = new AnalysisAnswer("foo", environment, Lists.newArrayList());
    AnalysisAnswer aWithAnswers =
        new AnalysisAnswer(
            "foo", null, Lists.newArrayList(Answer.failureAnswer("failureAnswer", null)));
    AnalysisAnswer aOtherName = new AnalysisAnswer("bar", null, Lists.newArrayList());

    new EqualsTester()
        .addEqualityGroup(analysisAnswer, analysisAnswerCopy)
        .addEqualityGroup(aWithEnvironment)
        .addEqualityGroup(aWithAnswers)
        .addEqualityGroup(aOtherName)
        .testEquals();
  }
}
