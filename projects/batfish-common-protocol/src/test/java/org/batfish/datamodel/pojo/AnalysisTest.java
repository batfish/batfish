package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Analysis}. */
@RunWith(JUnit4.class)
public class AnalysisTest {
  @Test
  public void testToString() {
    Analysis a = Analysis.of("foo", new ArrayList<>());
    assertThat(a.toString(), equalTo("Analysis{name=foo, questions=[]}"));
  }

  @Test
  public void testEquals() {
    Analysis a = Analysis.of("foo", new ArrayList<>());
    Analysis aCopy = Analysis.of("foo", new ArrayList<>());
    Analysis aWithQuestion =
        Analysis.of(
            "foo", Lists.newArrayList(Collections.singletonList(TestrigQuestion.of("question"))));
    Analysis aOtherName = Analysis.of("bar", new ArrayList<>());

    new EqualsTester()
        .addEqualityGroup(a, aCopy)
        .addEqualityGroup(aWithQuestion)
        .addEqualityGroup(aOtherName)
        .testEquals();
  }
}
