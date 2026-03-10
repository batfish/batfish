package org.batfish.datamodel.pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
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
  public void testConstructorAndGetter() {
    Map<String, String> questions = Collections.singletonMap("question", "questionContent");
    Analysis a = new Analysis("analysis", questions);
    assertThat(a.getName(), equalTo("analysis"));
    assertThat(a.getQuestions(), equalTo(questions));
  }

  @Test
  public void testAddQuestion() {
    Analysis a = new Analysis("analysis", Maps.newHashMap());
    a.addQuestion("question", "questionContent");
    assertThat(a.getQuestions().get("question"), equalTo("questionContent"));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question question already exists for analysis analysis"));
    a.addQuestion("question", "questionContent");
  }

  @Test
  public void testDeleteQuestion() {
    Analysis a =
        new Analysis(
            "analysis", Maps.newHashMap(Collections.singletonMap("question", "questionContent")));
    a.deleteQuestion("question");
    assertThat(a.getQuestions().size(), is(0));
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(equalTo("Question question does not exist for analysis analysis"));
    a.deleteQuestion("question");
  }

  @Test
  public void testToString() {
    Analysis a = new Analysis("foo", Maps.newHashMap());
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions={}}"));
    a.addQuestion("question", "questionContent");
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions={question=questionContent}}"));
  }

  @Test
  public void testEquals() {
    Analysis a = new Analysis("foo", Maps.newHashMap());
    Analysis aCopy = new Analysis("foo", Maps.newHashMap());
    Analysis aWithQuestion =
        new Analysis("foo", Collections.singletonMap("question", "questionContent"));
    Analysis aOtherName = new Analysis("bar", Maps.newHashMap());

    new EqualsTester()
        .addEqualityGroup(a, aCopy)
        .addEqualityGroup(aWithQuestion)
        .addEqualityGroup(aOtherName)
        .testEquals();
  }
}
