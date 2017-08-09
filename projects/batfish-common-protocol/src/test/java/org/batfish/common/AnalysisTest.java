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
 * Tests for {@link Analysis}.
 */
@RunWith(JUnit4.class)
public class AnalysisTest {
  @Test public void testToString() {
    Analysis a = Analysis.of("foo", new HashSet<>());
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions=[]}"));
  }

  @Test public void testEquals() {
    Analysis a = Analysis.of("foo", new HashSet<>());
    Analysis aCopy = Analysis.of("foo", new HashSet<>());
    Analysis aWithQuestion = Analysis.of("foo",
        Sets.newHashSet(Collections.singleton("questions")));
    Analysis aOtherName = Analysis.of("bar", new HashSet<>());

    new EqualsTester().addEqualityGroup(a, aCopy)
        .addEqualityGroup(aWithQuestion)
        .addEqualityGroup(aOtherName)
        .testEquals();
  }

}