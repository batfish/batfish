package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class MetricsTest {

  @Test
  public void testEquals() {
    Metrics.Builder builder = Metrics.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(
            builder
                .setAggregations(ImmutableMap.of("a", ImmutableMap.of(Aggregation.MAX, "A")))
                .build())
        .addEqualityGroup(builder.setEmptyColumns(ImmutableSet.of("b")).build())
        .addEqualityGroup(builder.setNumExcludedRows(5).build())
        .addEqualityGroup(builder.setNumRows(3).build())
        .testEquals();
  }
}
