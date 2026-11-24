package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Sets;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/** Tests for {@link Container}. */
class ContainerTest {
  @Test
  void testToString() {
    Container c = Container.of("foo", new TreeSet<>());
    assertThat(c.toString(), equalTo("Container{name=foo, testrigs=[]}"));
  }

  @Test
  void testEquals() {
    Container c = Container.of("foo", new TreeSet<>());
    Container cCopy = Container.of("foo", new TreeSet<>());
    Container cWithTestrig =
        Container.of("foo", Sets.newTreeSet(Collections.singletonList("testrig")));
    Container cOtherName = Container.of("bar", new TreeSet<>());

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithTestrig)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
