package org.batfish.datamodel.answers;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class ColumnAggregationResultTest {

  @Test
  public void testEquals() {
    ColumnAggregationResult group1Elem1 = new ColumnAggregationResult(Aggregation.MAX, "a", 1);
    ColumnAggregationResult group1Elem2 = new ColumnAggregationResult(Aggregation.MAX, "a", 1);
    ColumnAggregationResult group2Elem1 = new ColumnAggregationResult(Aggregation.MAX, "b", 1);
    ColumnAggregationResult group3Elem1 = new ColumnAggregationResult(Aggregation.MAX, "a", 2);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }
}
