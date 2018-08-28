package org.batfish.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class AnalysisAnswerOptionsTest {

  @Test
  public void testEquals() {
    AnalysisAnswerOptions group1Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "1")),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnalysisAnswerOptions group1Elem2 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "1")),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnalysisAnswerOptions group2Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("b"),
            ImmutableList.of(new ColumnFilter("a", "1")),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnalysisAnswerOptions group3Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "2")),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnalysisAnswerOptions group4Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "1")),
            3,
            2,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnalysisAnswerOptions group5Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "1")),
            1,
            4,
            ImmutableList.of(new ColumnSortOption("c", false)),
            false);
    AnalysisAnswerOptions group6Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "1")),
            1,
            2,
            ImmutableList.of(new ColumnSortOption("d", false)),
            false);
    AnalysisAnswerOptions group7Elem1 =
        new AnalysisAnswerOptions(
            ImmutableSet.of("a"),
            ImmutableList.of(new ColumnFilter("a", "1")),
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
