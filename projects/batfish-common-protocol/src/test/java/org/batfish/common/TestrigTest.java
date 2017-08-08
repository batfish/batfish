package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.Collections;
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
    Testrig t = Testrig.of("foo", new ArrayList<>());
    assertThat(t.toString(), equalTo("Testrig{name=foo, configs=[]}"));
  }

  @Test
  public void testEquals() {
    Testrig t = Testrig.of("foo", new ArrayList<>());
    Testrig tCopy = Testrig.of("foo", new ArrayList<>());
    Testrig tWithConfigs =
        Testrig.of("foo", Lists.newArrayList(Collections.singletonList("testrig")));
    Testrig tOtherName = Testrig.of("bar", new ArrayList<>());

    new EqualsTester()
        .addEqualityGroup(t, tCopy)
        .addEqualityGroup(tWithConfigs)
        .addEqualityGroup(tOtherName)
        .testEquals();
  }

}