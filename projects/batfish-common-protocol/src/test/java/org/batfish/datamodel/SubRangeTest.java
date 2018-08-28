package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link org.batfish.datamodel.SubRange} */
public class SubRangeTest {

  @Test
  public void testClosedInterval() {
    SubRange range = new SubRange(0, 100);
    for (int i = 0; i <= 100; i++) {
      assertThat(range.includes(i), equalTo(true));
    }
  }

  @Test
  public void testEquals() {
    SubRange group1Elem1 = new SubRange(0, 100);
    SubRange group1Elem2 = new SubRange(0, 100);

    SubRange group2Elem1 = new SubRange(1, 100);
    SubRange group3Elem1 = new SubRange(0, 99);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }

  @Test
  public void testEmptyRange() {
    SubRange range = new SubRange(3, 1);
    assertThat(range.getStart(), equalTo(3));
    assertThat(range.getEnd(), equalTo(1));
    assertThat(range.includes(1), equalTo(false));
    assertThat(range.includes(2), equalTo(false));
    assertThat(range.includes(3), equalTo(false));
  }
}
