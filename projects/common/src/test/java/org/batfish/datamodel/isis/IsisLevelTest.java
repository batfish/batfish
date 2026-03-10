package org.batfish.datamodel.isis;

import static org.batfish.datamodel.isis.IsisLevel.LEVEL_1;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_1_2;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_2;
import static org.batfish.datamodel.isis.IsisLevel.intersection;
import static org.batfish.datamodel.isis.IsisLevel.union;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class IsisLevelTest {

  @Test
  public void testIncludes() {
    assertThat(LEVEL_1_2.includes(LEVEL_1), is(true));
    assertThat(LEVEL_1_2.includes(LEVEL_2), is(true));
    assertThat(LEVEL_1.includes(LEVEL_1), is(true));
    assertThat(LEVEL_2.includes(LEVEL_1), is(false));
  }

  @Test
  public void testIntersectionForVariableNumberOfLevels() {
    assertThat(intersection(), nullValue());
    assertThat(intersection(LEVEL_1), equalTo(LEVEL_1));
    assertThat(intersection(LEVEL_1, LEVEL_1_2, LEVEL_2), nullValue());
  }

  @Test
  public void testUnionAndIntersectionForTwoLevels() {
    testUnionAndIntersection(null, null, null, null);
    testUnionAndIntersection(null, LEVEL_1, LEVEL_1, null);
    testUnionAndIntersection(null, LEVEL_1_2, LEVEL_1_2, null);
    testUnionAndIntersection(LEVEL_1, LEVEL_1, LEVEL_1, LEVEL_1);
    testUnionAndIntersection(LEVEL_1, LEVEL_2, LEVEL_1_2, null);
    testUnionAndIntersection(LEVEL_1, LEVEL_1_2, LEVEL_1_2, LEVEL_1);
  }

  private void testUnionAndIntersection(
      IsisLevel first, IsisLevel second, IsisLevel expectedUnion, IsisLevel expectedIntersection) {
    assertThat(union(first, second), equalTo(expectedUnion));
    assertThat(union(second, first), equalTo(expectedUnion));
    assertThat(intersection(first, second), equalTo(expectedIntersection));
    assertThat(intersection(second, first), equalTo(expectedIntersection));
  }
}
