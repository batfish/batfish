package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Testrig}. */
@RunWith(JUnit4.class)
public class TestrigTest {
  @Test
  public void testToString() {
    Testrig t = Testrig.of("foo", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    assertThat(
        t.toString(), equalTo("Testrig{name=foo, configs=[], environments=[], questions=[]}"));
  }

  @Test
  public void testEquals() {
    Testrig t = Testrig.of("foo", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Testrig tCopy = Testrig.of("foo", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Testrig tWithConfigs =
        Testrig.of("foo", Lists.newArrayList("configs"), new ArrayList<>(), new ArrayList<>());
    Testrig tWithenvs =
        Testrig.of("foo", new ArrayList<>(), Lists.newArrayList("environments"), new ArrayList<>());
    Testrig tWithQuestions =
        Testrig.of(
            "foo",
            new ArrayList<>(),
            new ArrayList<>(),
            Lists.newArrayList(TestrigQuestion.of("question")));
    Testrig tOtherName = Testrig.of("bar", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    new EqualsTester()
        .addEqualityGroup(t, tCopy)
        .addEqualityGroup(tWithConfigs)
        .addEqualityGroup(tWithenvs)
        .addEqualityGroup(tWithQuestions)
        .addEqualityGroup(tOtherName)
        .testEquals();
  }
}
