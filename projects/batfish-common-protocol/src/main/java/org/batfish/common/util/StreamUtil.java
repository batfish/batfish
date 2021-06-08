package org.batfish.common.util;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Utility functions for dealing with streams. */
public final class StreamUtil {
  /** Returns a list of the items in the given stream, but in random order. */
  public static <T> List<T> toListInRandomOrder(Stream<T> in) {
    return toListInRandomOrder(in, ThreadLocalRandom.current());
  }

  /**
   * Returns a list of the items in the given stream, but in random order controlled by the given
   * {@link Random}.
   */
  public static <T> List<T> toListInRandomOrder(Stream<T> in, Random random) {
    List<T> collected = in.collect(Collectors.toList());
    Collections.shuffle(collected, random);
    return ImmutableList.copyOf(collected);
  }

  private StreamUtil() {} // prevent instantiation
}
