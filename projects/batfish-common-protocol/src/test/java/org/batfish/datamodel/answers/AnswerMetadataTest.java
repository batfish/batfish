package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class AnswerMetadataTest {

  @Test
  public void testEquals() {
    AnswerMetadata group1Elem1 =
        new AnswerMetadata(
            new Metrics(ImmutableMap.of("A", ImmutableMap.of(Aggregation.MAX, 1)), 2),
            AnswerStatus.SUCCESS);
    AnswerMetadata group1Elem2 =
        new AnswerMetadata(
            new Metrics(ImmutableMap.of("A", ImmutableMap.of(Aggregation.MAX, 1)), 2),
            AnswerStatus.SUCCESS);
    AnswerMetadata group2Elem1 =
        new AnswerMetadata(
            new Metrics(ImmutableMap.of("B", ImmutableMap.of(Aggregation.MAX, 1)), 2),
            AnswerStatus.SUCCESS);
    AnswerMetadata group3Elem1 =
        new AnswerMetadata(
            new Metrics(ImmutableMap.of("A", ImmutableMap.of(Aggregation.MAX, 1)), 2),
            AnswerStatus.FAILURE);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }
}
