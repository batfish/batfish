package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import com.google.common.testing.EqualsTester;
import java.util.Collections;
import java.util.TreeSet;
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
    Container c = Container.makeContainer("foo", "sample uri");
    assertThat(c.toString(), equalTo("Container{name=foo, testrigsUri=sample uri}"));
  }

  @Test
  public void testEquals() {
    Container c = Container.makeContainer("foo", "sample uri");
    Container cCopy = Container.makeContainer("foo", "sample uri");
    Container cWithTestrig =
        Container.makeContainer("foo", "testrig");
    Container cOtherName = Container.makeContainer("bar", "sample uri");

    new EqualsTester()
        .addEqualityGroup(c, cCopy)
        .addEqualityGroup(cWithTestrig)
        .addEqualityGroup(cOtherName)
        .testEquals();
  }
}
