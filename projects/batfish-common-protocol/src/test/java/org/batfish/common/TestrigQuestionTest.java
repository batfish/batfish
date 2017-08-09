package org.batfish.common;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestrigQuestionTest {
  /**
   * Tests for {@link TestrigQuestion}.
   */

  @Test public void testToString() {
    TestrigQuestion t = TestrigQuestion.of("foo", "env", null);
    assertThat(t.toString(), equalTo("TestrigQuestion{name=foo, environment=env, question=null}"));
  }

  @Test public void testEquals() {
    TestrigQuestion t = TestrigQuestion.of("foo", "bar" , null);
    TestrigQuestion tCopy = TestrigQuestion.of("foo", "bar" , null);
    TestrigQuestion tWithoutEnv = TestrigQuestion.of("foo", "", null);
    TestrigQuestion tOtherName = TestrigQuestion.of("bar", "bar", null);

    new EqualsTester().addEqualityGroup(t, tCopy)
        .addEqualityGroup(tWithoutEnv)
        .addEqualityGroup(tOtherName)
        .testEquals();
  }
}