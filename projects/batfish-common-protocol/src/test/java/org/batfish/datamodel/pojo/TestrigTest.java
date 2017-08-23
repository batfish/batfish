package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.answers.Answer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Testrig}. */
@RunWith(JUnit4.class)
public class TestrigTest {

  @Test
  public void testConstructorAndGetter() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    List<AnalysisAnswer> analysisAnswers =
        Lists.newArrayList(new AnalysisAnswer("analysis", environment, null));
    List<Environment> environments = Lists.newArrayList(environment);
    List<TestrigQuestion> questions =
        Lists.newArrayList(
            new TestrigQuestion("question", "questionContent", environment, new Answer()));
    Map<String, String> configurations = Collections.singletonMap("config", "configContent");
    Testrig testrig =
        new Testrig("testrig", analysisAnswers, environments, questions, configurations);
    assertThat(testrig.getName(), equalTo("testrig"));
    assertThat(testrig.getAnalysisAnswers(), equalTo(analysisAnswers));
    assertThat(testrig.getEnvironments(), equalTo(environments));
    assertThat(testrig.getQuestions(), equalTo(questions));
    assertThat(testrig.getConfigurations(), equalTo(configurations));
  }

  @Test
  public void testToString() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    List<AnalysisAnswer> analysisAnswers =
        Lists.newArrayList(new AnalysisAnswer("analysis", environment, null));
    List<Environment> environments = Lists.newArrayList(environment);
    List<TestrigQuestion> questions =
        Lists.newArrayList(
            new TestrigQuestion("question", "questionContent", environment, new Answer()));
    Map<String, String> configurations = Collections.singletonMap("config", "configContent");
    Testrig testrig =
        new Testrig("testrig", analysisAnswers, environments, questions, configurations);
    String expected =
        String.format(
            "Testrig{name=testrig, analysisAnswers=%s, environments=%s, questions=%s, configurations=%s}",
            analysisAnswers, environments, questions, configurations);
    assertThat(testrig.toString(), equalTo(expected));
  }

  @Test
  public void testEquals() {
    Environment environment = new Environment("environment", null, null, null, null, null, null);
    List<AnalysisAnswer> analysisAnswers =
        Lists.newArrayList(new AnalysisAnswer("analysis", environment, null));
    List<Environment> environments = Lists.newArrayList(environment);
    List<TestrigQuestion> questions =
        Lists.newArrayList(
            new TestrigQuestion("question", "questionContent", environment, new Answer()));
    Map<String, String> configurations = Collections.singletonMap("config", "configContent");
    Testrig t =
        new Testrig(
            "foo",
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            Maps.newHashMap());
    Testrig tCopy =
        new Testrig(
            "foo",
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            Maps.newHashMap());
    Testrig tWithAnalysisAnswers =
        new Testrig(
            "foo", analysisAnswers, Lists.newArrayList(), Lists.newArrayList(), Maps.newHashMap());
    Testrig tWithEnvironments =
        new Testrig(
            "foo", Lists.newArrayList(), environments, Lists.newArrayList(), Maps.newHashMap());
    Testrig tWithQuestions =
        new Testrig(
            "foo", Lists.newArrayList(), Lists.newArrayList(), questions, Maps.newHashMap());
    Testrig tWithConfigurations =
        new Testrig(
            "foo",
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            configurations);
    Testrig tOtherName =
        new Testrig(
            "bar",
            Lists.newArrayList(),
            Lists.newArrayList(),
            Lists.newArrayList(),
            Maps.newHashMap());

    new EqualsTester()
        .addEqualityGroup(t, tCopy)
        .addEqualityGroup(tWithAnalysisAnswers)
        .addEqualityGroup(tWithEnvironments)
        .addEqualityGroup(tWithQuestions)
        .addEqualityGroup(tWithConfigurations)
        .addEqualityGroup(tOtherName)
        .testEquals();
  }
}
