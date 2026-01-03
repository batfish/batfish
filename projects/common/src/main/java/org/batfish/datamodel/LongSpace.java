package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A {@link NumberSpace} of {@link Long}s */
@ParametersAreNonnullByDefault
public final class LongSpace extends NumberSpace<Long, LongSpace, LongSpace.Builder> {

  /** A builder for {@link LongSpace} */
  public static final class Builder extends NumberSpace.Builder<Long, LongSpace, Builder> {

    @JsonCreator
    @VisibleForTesting
    static @Nonnull Builder create(@Nullable String s) {
      Builder builder = new Builder();
      create(builder, s);
      return builder;
    }

    private Builder() {
      super();
    }

    private Builder(LongSpace space) {
      super(space);
    }

    @Override
    protected @Nonnull LongSpace build(RangeSet<Long> rangeSet) {
      return new LongSpace(rangeSet);
    }

    @Override
    protected @Nonnull DiscreteDomain<Long> discreteDomain() {
      return DiscreteDomain.longs();
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    @Override
    protected @Nonnull Range<Long> parse(String s) {
      try {
        long i = Long.parseUnsignedLong(s);
        return (Range.closed(i, i));
      } catch (NumberFormatException e) {
        String[] endpoints = s.split("-");
        checkArgument((endpoints.length == 2), ERROR_MESSAGE_TEMPLATE, s);
        long low = Long.parseUnsignedLong(endpoints[0].trim());
        long high = Long.parseUnsignedLong(endpoints[1].trim());
        checkArgument(low <= high, ERROR_MESSAGE_TEMPLATE, s);
        return Range.closed(low, high);
      }
    }
  }

  /** Empty {@link LongSpace} */
  public static final LongSpace EMPTY = builder().build();

  private static final String ERROR_MESSAGE_TEMPLATE = "Invalid range specification %s";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  @VisibleForTesting
  static @Nonnull LongSpace create(@Nullable String s) {
    return LongSpace.Builder.create(s).build();
  }

  /** Create a new singleton {@link LongSpace} from an long value */
  public static @Nonnull LongSpace of(long value) {
    return builder().including(Range.singleton(value)).build();
  }

  /** Create a new {@link LongSpace} from a {@link Range} */
  public static @Nonnull LongSpace of(Range<Long> range) {
    return builder().including(range).build();
  }

  /** Create a new {@link LongSpace} from a {@link RangeSet} */
  public static @Nonnull LongSpace of(RangeSet<Long> rangeSet) {
    return builder().including(rangeSet).build();
  }

  public static @Nonnull LongSpace parse(String s) {
    return create(s);
  }

  /** Create a new {@link LongSpace} containing the union of the given {@link Range ranges}. */
  public static @Nonnull LongSpace unionOf(Iterable<Range<Long>> ranges) {
    return builder().includingAllRanges(ranges).build();
  }

  /** Create a new {@link LongSpace} containing the union of the given {@link Range ranges}. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static @Nonnull LongSpace unionOf(Range<Long>... ranges) {
    return unionOf(Arrays.asList(ranges));
  }

  protected LongSpace(RangeSet<Long> rangeset) {
    super(rangeset);
  }

  @Override
  protected @Nonnull DiscreteDomain<Long> discreteDomain() {
    return DiscreteDomain.longs();
  }

  @Override
  protected @Nonnull LongSpace empty() {
    return EMPTY;
  }

  /** Return an ordered set of longs described by this space. */
  @Override
  public @Nonnull Set<Long> enumerate() {
    return ImmutableRangeSet.copyOf(_rangeset).asSet(DiscreteDomain.longs());
  }

  @Override
  protected @Nonnull LongSpace getThis() {
    return this;
  }

  /** Returns a stream of the included longs. */
  public @Nonnull LongStream longStream() {
    return stream().mapToLong(Long::longValue);
  }

  @Override
  protected @Nonnull Builder newBuilder() {
    return builder();
  }
}
