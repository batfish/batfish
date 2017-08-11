package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Testrig}.
 */
@RunWith(JUnit4.class)
public class TestrigTest {
  @Test
  public void testToString() {
    Testrig t = Testrig.of("foo", new HashSet<>(), new HashSet<>(), new HashSet<>());
    assertThat(t.toString(),
        equalTo("Testrig{name=foo, configs=[], environments=[], questions=[]}"));
  }

  @Test
  public void testEquals() {
    Testrig t = Testrig.of("foo", new HashSet<>(), new HashSet<>(), new HashSet<>());
    Testrig tCopy = Testrig.of("foo", new HashSet<>(), new HashSet<>(), new HashSet<>());
    Testrig tWithConfigs = Testrig.of("foo",
        Sets.newHashSet(Collections.singleton("cinfigs")),
        new HashSet<>(),
        new HashSet<>());
    Testrig tWithEnv = Testrig.of("foo",
        new HashSet<>(),
        Sets.newHashSet(Collections.singleton("environment")),
        new HashSet<>());
    Testrig tWithQuestions = Testrig.of("foo",
        new HashSet<>(),
        new HashSet<>(),
        Sets.newHashSet(Collections.singleton("questions")));
    Testrig tOtherName = Testrig.of("bar", new HashSet<>(), new HashSet<>(), new HashSet<>());

    new EqualsTester()
        .addEqualityGroup(t, tCopy)
        .addEqualityGroup(tWithConfigs).addEqualityGroup(tWithEnv).addEqualityGroup(tWithQuestions)
        .addEqualityGroup(tOtherName)
        .testEquals();
  }

}