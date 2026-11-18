package org.batfish.datamodel.answers;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

final class ColumnAggregationTest {

  @Test
  void testEquals() {
    ColumnAggregation a1 = new ColumnAggregation(Aggregation.MAX, "a");
    ColumnAggregation a2 = new ColumnAggregation(Aggregation.MAX, "a");
    ColumnAggregation b1 = new ColumnAggregation(Aggregation.MAX, "b");

    new EqualsTester().addEqualityGroup(a1, a2).addEqualityGroup(b1).testEquals();
  }
}
