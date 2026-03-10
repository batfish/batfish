package org.batfish.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class AnswerRowsOptionsTest {

  @Test
  public void testEquals() {
    ColumnFilter cf1 = new ColumnFilter("a", "1", false);
    ColumnFilter cf2 = new ColumnFilter("a", "1", true);
    AnswerRowsOptions group1Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf1),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnswerRowsOptions group1Elem2 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf1),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnswerRowsOptions group2Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("b"),
            ImmutableList.of(cf1),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnswerRowsOptions group3Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf2),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnswerRowsOptions group4Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf1),
            3,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnswerRowsOptions group5Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf1),
            1,
            4,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnswerRowsOptions group6Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf1),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("d", false)),
            false);
    AnswerRowsOptions group7Elem1 =
        new AnswerRowsOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(cf1),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            true);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .addEqualityGroup(group4Elem1)
        .addEqualityGroup(group5Elem1)
        .addEqualityGroup(group6Elem1)
        .addEqualityGroup(group7Elem1)
        .testEquals();
  }
}
