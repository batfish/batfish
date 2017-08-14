package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TestrigQuestion}. */
@RunWith(JUnit4.class)
public class TestrigQuestionTest {
  @Test
  public void testToString() {
    TestrigQuestion tq = TestrigQuestion.of("foo", null);
    assertThat(tq.toString(), equalTo("TestrigQuestion{name=foo, question=null}"));
  }

  @Test
  public void testEquals() {
    TestrigQuestion tq = TestrigQuestion.of("foo", null);
    TestrigQuestion tqCopy = TestrigQuestion.of("foo", null);
    TestrigQuestion tqOtherName = TestrigQuestion.of("bar", null);

    new EqualsTester().addEqualityGroup(tq, tqCopy).addEqualityGroup(tqOtherName).testEquals();
  }
}
