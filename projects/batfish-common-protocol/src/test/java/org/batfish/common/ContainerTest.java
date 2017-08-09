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
 * Tests for {@link Container}.
 */
@RunWith(JUnit4.class)
public class ContainerTest {
  @Test
  public void testToString() {
    Container c = Container.of("foo", new HashSet<>(), new HashSet<>());
    assertThat(c.toString(), equalTo("Container{name=foo, testrigs=[], analysis=[]}"));
  }

  @Test
  public void testEquals() {
    Container c = Container.of("foo", new HashSet<>(), new HashSet<>());
    Container cCopy = Container.of("foo", new HashSet<>(), new HashSet<>());
    Container cWithTestrig = Container.of(
        "foo",
        Sets.newHashSet(Collections.singleton("testrig")),
        new HashSet<>());
    Container cWithAnalysis = Container.of(
        "foo",
        new HashSet<>(),
        new HashSet<>(Collections.singleton("analysis")));
    Container cOtherName = Container.of("bar", new HashSet<>(), new HashSet<>());

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithTestrig).addEqualityGroup(cWithAnalysis)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
