package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.answers.Answer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TestrigQuestion}. */
@RunWith(JUnit4.class)
public class TestrigQuestionTest {

  @Test
  public void testConstructorAndGetter() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    Answer answer = Answer.failureAnswer("failureAnswer", null);
    TestrigQuestion trQuestion =
        new TestrigQuestion("trQuestion", "question content", environment, answer);
    assertThat(trQuestion.getName(), equalTo("trQuestion"));
    assertThat(trQuestion.getQuestion(), equalTo("question content"));
    assertThat(trQuestion.getEnvironment(), equalTo(environment));
    assertThat(trQuestion.getAnswer(), equalTo(answer));
  }

  @Test
  public void testToString() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    Answer answer = Answer.failureAnswer("failureAnswer", null);
    TestrigQuestion trQuestion =
        new TestrigQuestion("trQuestion", "questionContent", environment, answer);
    String expected =
        String.format(
            "TestrigQuestion{name=trQuestion, question=questionContent, environment=%s, answer=%s}",
            environment, answer);
    assertThat(trQuestion.toString(), equalTo(expected));
  }

  @Test
  public void testEquals() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    Answer answer = Answer.failureAnswer("failureAnswer", null);
    TestrigQuestion trQuestion = new TestrigQuestion("foo", "", environment, answer);
    TestrigQuestion trQuestionCopy = new TestrigQuestion("foo", "", environment, answer);
    TestrigQuestion tqWithQuestion =
        new TestrigQuestion("foo", "question content", environment, answer);
    TestrigQuestion tqOtherName = new TestrigQuestion("bar", "", environment, answer);

    new EqualsTester()
        .addEqualityGroup(trQuestion, trQuestionCopy)
        .addEqualityGroup(tqWithQuestion)
        .addEqualityGroup(tqOtherName)
        .testEquals();
  }
}
