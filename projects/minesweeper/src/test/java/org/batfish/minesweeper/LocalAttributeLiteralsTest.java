package org.batfish.minesweeper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

/** Tests for {@link LocalAttributeLiterals}. */
class LocalAttributeLiteralsTest {

  @Test
  void testCreateSortsAndRemovesDuplicates() {
    LocalAttributeLiterals literals =
        LocalAttributeLiterals.create(
            ImmutableList.of(7L, 5L, 7L), ImmutableList.of(3L), ImmutableList.of(2L, 1L));

    assertEquals(ImmutableList.of(5L, 7L), literals.tag2s());
    assertEquals(ImmutableList.of(3L), literals.colors());
    assertEquals(ImmutableList.of(1L, 2L), literals.color2s());
  }

  @Test
  void testCreateRemovesZero() {
    LocalAttributeLiterals literals =
        LocalAttributeLiterals.create(
            ImmutableList.of(0L, 5L), ImmutableList.of(0L), ImmutableList.of());

    assertEquals(ImmutableList.of(5L), literals.tag2s());
    assertEquals(ImmutableList.of(), literals.colors());
  }

  @Test
  void testEmpty() {
    LocalAttributeLiterals empty = LocalAttributeLiterals.empty();

    assertEquals(ImmutableList.of(), empty.tag2s());
    assertEquals(ImmutableList.of(), empty.colors());
    assertEquals(ImmutableList.of(), empty.color2s());
  }
}
