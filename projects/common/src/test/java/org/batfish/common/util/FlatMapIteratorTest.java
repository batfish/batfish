package org.batfish.common.util;

import static org.batfish.common.util.FlatMapIterator.flatMapIterator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

/** Test for {@link FlatMapIterator}. */
final class FlatMapIteratorTest {
  private static <T> Stream<T> toStream(Iterator<T> iter) {
    Iterable<T> iterable = () -> iter;
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  @Test
  void testSimple() {
    // double each integer from 0 to 5 (exclusive)
    List<Integer> actual =
        toStream(
                flatMapIterator(
                    ImmutableList.of(0, 1, 2, 3, 4).iterator(),
                    i -> ImmutableList.of(i, i).iterator()))
            .collect(Collectors.toList());
    assertEquals(ImmutableList.of(0, 0, 1, 1, 2, 2, 3, 3, 4, 4), actual);
  }
}
