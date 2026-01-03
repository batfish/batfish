package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BoundType;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NumberSpace} of {@link Integer}s */
@ParametersAreNonnullByDefault
public final class IntegerSpace extends NumberSpace<Integer, IntegerSpace, IntegerSpace.Builder> {

  protected IntegerSpace(RangeSet<Integer> rangeset) {
    super(rangeset);
  }

  /** Empty {@link IntegerSpace} */
  public static final IntegerSpace EMPTY = builder().build();

  /** A range expressing TCP/UDP ports */
  public static final IntegerSpace PORTS = builder().including(Range.closed(0, 65535)).build();

  @Override
  protected @Nonnull DiscreteDomain<Integer> discreteDomain() {
    return DiscreteDomain.integers();
  }

  @Override
  protected @Nonnull IntegerSpace getThis() {
    return this;
  }

  @Override
  protected @Nonnull Builder newBuilder() {
    return builder();
  }

  @JsonCreator
  @VisibleForTesting
  static @Nonnull IntegerSpace create(@Nullable String s) {
    return IntegerSpace.Builder.create(s).build();
  }

  @Override
  protected @Nonnull IntegerSpace empty() {
    return EMPTY;
  }

  public static @Nonnull IntegerSpace parse(String s) {
    return create(s);
  }

  /** Return this space as a set of included {@link SubRange}s */
  public @Nonnull Set<SubRange> getSubRanges() {
    return _rangeset.asRanges().stream()
        .map(
            r -> {
              assert r.lowerBoundType() == BoundType.CLOSED && r.upperBoundType() == BoundType.OPEN;
              return new SubRange(r.lowerEndpoint(), r.upperEndpoint() - 1);
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  /** Return an ordered set of integers described by this space. */
  @Override
  public @Nonnull SortedSet<Integer> enumerate() {
    return ImmutableRangeSet.copyOf(_rangeset).asSet(DiscreteDomain.integers());
  }

  /** Returns a stream of the included integers. */
  public @Nonnull IntStream intStream() {
    return stream().mapToInt(Integer::intValue);
  }

  /** Create a new {@link IntegerSpace} from a {@link SubRange} */
  public static @Nonnull IntegerSpace of(SubRange range) {
    return builder().including(range).build();
  }

  /**
   * Create a new {@link IntegerSpace} containing the union of the given {@link IntegerSpace
   * spaces}.
   */
  public static @Nonnull IntegerSpace unionOf(IntegerSpace... spaces) {
    Builder ret = builder();
    for (IntegerSpace space : spaces) {
      ret.including(space);
    }
    return ret.build();
  }

  /**
   * Create a new {@link IntegerSpace} containing the union of the given {@link SubRange subRanges}.
   */
  public static @Nonnull IntegerSpace unionOf(SubRange... subRanges) {
    return unionOfSubRanges(Arrays.asList(subRanges));
  }

  /** Create a new {@link IntegerSpace} containing the union of the given {@link Range ranges}. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static @Nonnull IntegerSpace unionOf(Range<Integer>... ranges) {
    return unionOf(Arrays.asList(ranges));
  }

  /** Create a new {@link IntegerSpace} containing the union of the given {@link Range ranges}. */
  public static IntegerSpace unionOf(Iterable<Range<Integer>> ranges) {
    return builder().includingAllRanges(ranges).build();
  }

  /**
   * Create a new {@link IntegerSpace} containing the union of the given {@link SubRange subRanges}.
   */
  public static IntegerSpace unionOfSubRanges(Iterable<SubRange> subRanges) {
    return builder().includingAllSubRanges(subRanges).build();
  }

  /** Create a new {@link IntegerSpace} from a {@link Range} */
  public static IntegerSpace of(Range<Integer> range) {
    return builder().including(range).build();
  }

  /** Create a new {@link IntegerSpace} from a {@link RangeSet} */
  public static IntegerSpace of(RangeSet<Integer> rangeSet) {
    return builder().including(rangeSet).build();
  }

  /** Create a new singleton {@link IntegerSpace} from an integer value */
  public static IntegerSpace of(int value) {
    return builder().including(Range.singleton(value)).build();
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  /** A builder for {@link IntegerSpace} */
  public static final class Builder extends NumberSpace.Builder<Integer, IntegerSpace, Builder> {
    /** Include given {@link SubRange subRanges}. */
    public final @Nonnull Builder includingAllSubRanges(Iterable<SubRange> subRanges) {
      subRanges.forEach(this::including);
      return getThis();
    }

    /** Exclude given {@link SubRange subRanges}. */
    public final @Nonnull Builder excludingAllSubRanges(Iterable<SubRange> subRanges) {
      subRanges.forEach(this::excluding);
      return getThis();
    }

    private Builder() {
      super();
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    @Override
    protected @Nonnull IntegerSpace build(RangeSet<Integer> rangeSet) {
      return new IntegerSpace(rangeSet);
    }

    @Override
    protected @Nonnull Range<Integer> parse(String s) {
      try {
        int i = Integer.parseUnsignedInt(s);
        return (Range.closed(i, i));
      } catch (NumberFormatException e) {
        String[] endpoints = s.split("-");
        checkArgument((endpoints.length == 2), ERROR_MESSAGE_TEMPLATE, s);
        int low = Integer.parseUnsignedInt(endpoints[0].trim());
        int high = Integer.parseUnsignedInt(endpoints[1].trim());
        checkArgument(low <= high, ERROR_MESSAGE_TEMPLATE, s);
        return Range.closed(low, high);
      }
    }

    @Override
    protected @Nonnull DiscreteDomain<Integer> discreteDomain() {
      return DiscreteDomain.integers();
    }

    private Builder(IntegerSpace space) {
      super(space);
    }

    /** Exclude a {@link SubRange} */
    public @Nonnull Builder excluding(SubRange range) {
      if (!range.isEmpty()) {
        excluding(
            Range.closed(range.getStart(), range.getEnd()).canonical(DiscreteDomain.integers()));
      }
      return this;
    }

    /** Include a {@link SubRange} */
    public @Nonnull Builder including(SubRange range) {
      if (!range.isEmpty()) {
        including(
            Range.closed(range.getStart(), range.getEnd()).canonical(DiscreteDomain.integers()));
      }
      return this;
    }

    /** Include the given numbers. */
    public @Nonnull Builder including(int... numbers) {
      for (int n : numbers) {
        including(n);
      }
      return this;
    }

    @JsonCreator
    @VisibleForTesting
    static @Nonnull Builder create(@Nullable String s) {
      Builder builder = new Builder();
      NumberSpace.Builder.create(builder, s);
      return builder;
    }
  }
}
