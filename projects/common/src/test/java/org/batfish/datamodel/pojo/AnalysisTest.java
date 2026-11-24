package org.batfish.datamodel.pojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Maps;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.junit.jupiter.api.Test;

/** Tests for {@link Analysis}. */
class AnalysisTest {

  @Test
  void testConstructorAndGetter() {
    Map<String, String> questions = Collections.singletonMap("question", "questionContent");
    Analysis a = new Analysis("analysis", questions);
    assertThat(a.getName(), equalTo("analysis"));
    assertThat(a.getQuestions(), equalTo(questions));
  }

  @Test
  void testAddQuestion() {
    Analysis a = new Analysis("analysis", Maps.newHashMap());
    a.addQuestion("question", "questionContent");
    assertThat(a.getQuestions().get("question"), equalTo("questionContent"));
    Throwable exception =
        assertThrows(BatfishException.class, () -> a.addQuestion("question", "questionContent"));
    assertThat(
        exception.getMessage(), equalTo("Question question already exists for analysis analysis"));
  }

  @Test
  void testDeleteQuestion() {
    Analysis a =
        new Analysis(
            "analysis", Maps.newHashMap(Collections.singletonMap("question", "questionContent")));
    a.deleteQuestion("question");
    assertThat(a.getQuestions().size(), is(0));
    Throwable exception = assertThrows(BatfishException.class, () -> a.deleteQuestion("question"));
    assertThat(
        exception.getMessage(), equalTo("Question question does not exist for analysis analysis"));
  }

  @Test
  void testToString() {
    Analysis a = new Analysis("foo", Maps.newHashMap());
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions={}}"));
    a.addQuestion("question", "questionContent");
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions={question=questionContent}}"));
  }

  @Test
  void testEquals() {
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
