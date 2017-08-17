package org.batfish.datamodel.pojo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Analysis}. */
@RunWith(JUnit4.class)
public class AnalysisTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorWithName() {
    Analysis a = new Analysis("analysis");
    assertThat(a.getName(), equalTo("analysis"));
    assertTrue(a.getQuestions().isEmpty());
  }

  @Test
  public void testBasicFunctions() {
    Map<String, String> questions = Collections.singletonMap("question", "questionContent");
    Analysis a = new Analysis("analysis", questions);
    assertThat(a.getName(), equalTo("analysis"));
    assertThat(a.getQuestions(), equalTo(questions));
    a.setName("other-analysis");
    assertThat(a.getName(), equalTo("other-analysis"));
    Map<String, String> otherQuestions =
        Collections.singletonMap("other-question", "other question content");
    a.setQuestions(otherQuestions);
    assertThat(a.getQuestions(), equalTo(otherQuestions));
  }

  @Test
  public void testAddQuestion() {
    Analysis a = new Analysis("analysis");
    a.addQuestion("question", "questionContent");
    assertThat(a.getQuestions().get("question"), equalTo("questionContent"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question 'question' already exists for analysis 'analysis'"));
    a.addQuestion("question", "questionContent");
  }

  @Test
  public void testDeleteQuestion() {
    Map<String, String> questions = new HashMap<>();
    questions.put("question", "questionContent");
    Analysis a = new Analysis("analysis", questions);
    a.deleteQuestion("question");
    assertThat(a.getQuestions().size(), is(0));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question 'question' does not exist for analysis 'analysis'"));
    a.deleteQuestion("question");
  }

  @Test
  public void testToString() {
    Analysis a = new Analysis("foo", new HashMap<>());
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions={}}"));
    a.addQuestion("question", "questionContent");
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions={question=questionContent}}"));
  }

  @Test
  public void testEquals() {
    Analysis a = new Analysis("foo", new HashMap<>());
    Analysis aCopy = new Analysis("foo", new HashMap<>());
    Analysis aWithQuestion =
        new Analysis("foo", Collections.singletonMap("question", "questionContent"));
    Analysis aOtherName = new Analysis("bar", new HashMap<>());

    new EqualsTester()
        .addEqualityGroup(a, aCopy)
        .addEqualityGroup(aWithQuestion)
        .addEqualityGroup(aOtherName)
        .testEquals();
  }
}
