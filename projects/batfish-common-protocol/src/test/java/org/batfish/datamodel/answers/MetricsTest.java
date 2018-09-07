package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class MetricsTest {

  @Test
  public void testEquals() {
    Metrics group1Elem1 =
        new Metrics(
            ImmutableMap.of("a", ImmutableMap.of(Aggregation.MAX, "A")), ImmutableSet.of(), 1);
    Metrics group1Elem2 =
        new Metrics(
            ImmutableMap.of("a", ImmutableMap.of(Aggregation.MAX, "A")), ImmutableSet.of(), 1);
    Metrics group2Elem1 =
        new Metrics(
            ImmutableMap.of("b", ImmutableMap.of(Aggregation.MAX, "A")), ImmutableSet.of(), 1);
    Metrics group3Elem1 =
        new Metrics(
            ImmutableMap.of("a", ImmutableMap.of(Aggregation.MAX, "A")), ImmutableSet.of(), 2);
    Metrics group4Elem1 =
        new Metrics(
            ImmutableMap.of("a", ImmutableMap.of(Aggregation.MAX, "A")),
            ImmutableSet.of("blah"),
            1);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .addEqualityGroup(group4Elem1)
        .testEquals();
  }
}
