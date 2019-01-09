package org.batfish.datamodel.isis;

import static org.batfish.datamodel.isis.IsisLevel.LEVEL_1;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_1_2;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_2;
import static org.batfish.datamodel.isis.IsisLevel.intersection;
import static org.batfish.datamodel.isis.IsisLevel.union;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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
  public void testIntersection() {
    assertThat(intersection(), nullValue());
    assertThat(intersection(LEVEL_1), equalTo(LEVEL_1));
    assertThat(intersection(LEVEL_1, LEVEL_2), nullValue());
    assertThat(intersection(LEVEL_1, LEVEL_1_2), equalTo(LEVEL_1));
    assertThat(intersection(LEVEL_1, LEVEL_1_2, LEVEL_2), nullValue());
    assertThat(intersection(LEVEL_1, null), nullValue());
  }

  @Test
  public void testUnion() {
    assertThat(union(null, null), nullValue());
    assertThat(union(null, LEVEL_1), equalTo(LEVEL_1));
    assertThat(union(LEVEL_1, LEVEL_2), equalTo(LEVEL_1_2));
  }
}
