package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A finite, closed, not necessarily contiguous space of longs. Long spaces are by design
 * <i>immutable</i>, but can be altered by converting {@link #toBuilder()} and recreated again.
 */
@ParametersAreNonnullByDefault
public final class LongSpace implements Serializable {

  /** A range expressing entire range of valid AS numbers */
  public static final LongSpace ALL_AS_NUMBERS = LongSpace.of(Range.closed(1L, 0xFFFFFFFFL));

  /** Empty long space */
  public static final LongSpace EMPTY = builder().build();

  private static final String ERROR_MESSAGE_TEMPLATE = "Invalid range specification %s";

  /*
   * Invariant: always ensure ranges are stored in canonical form (enforced in builder methods)
   * and immutable (enforced in constructor)
   */
  @Nonnull private final RangeSet<Long> _rangeset;

  private LongSpace(RangeSet<Long> rangeset) {
    _rangeset = ImmutableRangeSet.copyOf(rangeset);
  }

  @JsonCreator
  @VisibleForTesting
  @Nonnull
  static LongSpace create(@Nullable String s) {
    return LongSpace.Builder.create(s).build();
  }

  public static @Nonnull LongSpace parse(String s) {
    return create(s);
  }

  /** This space as a set of included {@link Range}s */
  public Set<Range<Long>> getRanges() {
    return _rangeset.asRanges();
  }

  /** Check that this space contains a given {@code value}. */
  public boolean contains(long value) {
    return _rangeset.contains(value);
  }

  /** Check that this space *fully* contains the {@code other} {@link LongSpace}. */
  public boolean contains(LongSpace other) {
    return _rangeset.enclosesAll(other._rangeset.asRanges());
  }

  /** Return an ordered set of longs described by this space. */
  public Set<Long> enumerate() {
    return ImmutableRangeSet.copyOf(_rangeset).asSet(DiscreteDomain.longs());
  }

  /** Returns true if this space is a contiguous space */
  public boolean isContiguous() {
    return _rangeset.asRanges().size() <= 1;
  }

  /** Return true iff this space is empty (contains no values) */
  public boolean isEmpty() {
    return _rangeset.isEmpty();
  }

  /** Return true iff this space is a singleton (contains exactly one value) */
  public boolean isSingleton() {
    return getRanges().size() == 1 && isSingletonRange(getRanges().iterator().next());
  }

  private static boolean isSingletonRange(Range<Long> range) {
    return range.upperEndpoint() - range.lowerEndpoint() == 1;
  }

  /**
   * Return singleton value if this space is a singleton. Otherwise throws {@link
   * NoSuchElementException}
   */
  public long singletonValue() throws NoSuchElementException {
    if (!isSingleton()) {
      throw new NoSuchElementException();
    }
    return _rangeset.asRanges().iterator().next().lowerEndpoint();
  }

  /** Returns a stream of the included longs. */
  public LongStream stream() {
    return enumerate().stream().mapToLong(Long::intValue);
  }

  /** Longersect two long spaces together. */
  public LongSpace intersection(LongSpace other) {
    return new LongSpace(
        other._rangeset.asRanges().stream()
            .map(_rangeset::subRangeSet) // intersect individual ranges with _rangeset
            .map(RangeSet::asRanges) // flatten each intersection result to set of ranges
            .flatMap(Set::stream) // stream for collection
            .collect(ImmutableRangeSet.toImmutableRangeSet()));
  }

  /** Union two long spaces together */
  public LongSpace union(LongSpace other) {
    Builder builder = this.toBuilder();
    other._rangeset.asRanges().forEach(builder::including);
    return builder.build();
  }

  /** Take the complement of this space, bounded by some other {@link LongSpace} */
  public LongSpace not(LongSpace within) {
    return new LongSpace(_rangeset.complement()).intersection(within);
  }

  /**
   * Take the complement of this space, bounded by existing lower and upper limits of the space.
   * This can be used as a way to represent a set of excluded ranges as a positive space.
   */
  public LongSpace not() {
    if (_rangeset.isEmpty()) {
      return EMPTY;
    }
    return new LongSpace(_rangeset.complement().subRangeSet(_rangeset.span()));
  }

  /** Compute the difference between two long spaces */
  public LongSpace difference(LongSpace other) {
    return this.intersection(other.not(this));
  }

  /** Compute the symmetric difference between two long spaces */
  public LongSpace symmetricDifference(LongSpace other) {
    return this.union(other).difference(intersection(other));
  }

  /** Return a builder initialized with existing long space */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /** Create a new long space containing the union of the given {@link Range ranges}. */
  @SafeVarargs
  public static LongSpace unionOf(Range<Long>... ranges) {
    return unionOf(Arrays.asList(ranges));
  }

  /** Create a new long space containing the union of the given {@link Range ranges}. */
  public static LongSpace unionOf(Iterable<Range<Long>> ranges) {
    Builder b = builder();
    for (Range<Long> range : ranges) {
      b.including(range);
    }
    return b.build();
  }

  /** Create a new long space from a {@link Range} */
  public static LongSpace of(Range<Long> range) {
    return builder().including(range).build();
  }

  /** Create a new long space from a {@link RangeSet} */
  public static LongSpace of(RangeSet<Long> rangeSet) {
    return builder().includingAll(rangeSet).build();
  }

  /** Create a new singleton long space from an long value */
  public static LongSpace of(long value) {
    return builder().including(Range.singleton(value)).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  /** A builder for {@link LongSpace} */
  public static final class Builder {
    private Set<Range<Long>> _including;
    private Set<Range<Long>> _excluding;

    private Builder() {
      _including = new HashSet<>();
      _excluding = new HashSet<>();
    }

    private Builder(LongSpace space) {
      this();
      _including.addAll(space._rangeset.asRanges());
    }

    /** Include given {@link RangeSet}. */
    public Builder includingAll(RangeSet<Long> rangeSet) {
      rangeSet.asRanges().forEach(this::including);
      return this;
    }

    /** Include given {@code longs}. */
    public Builder includingAll(Iterable<Long> longs) {
      longs.forEach(this::including);
      return this;
    }

    /** Include an long. */
    public Builder including(@Nonnull Long range) {
      return including(Range.singleton(range));
    }

    /** Include a range. The {@link Range} must be a finite range. */
    public Builder including(Range<Long> range) {
      checkArgument(
          range.hasLowerBound() && range.hasUpperBound(), "Infinite ranges are not supported");
      if (!range.isEmpty()) {
        _including.add(range.canonical(DiscreteDomain.longs()));
      }
      return this;
    }

    /** Include an {@link LongSpace} */
    public Builder including(LongSpace space) {
      space._rangeset.asRanges().forEach(this::including);
      return this;
    }

    /** Exclude an long. */
    public Builder excluding(@Nonnull Long range) {
      return excluding(Range.singleton(range));
    }

    /** Exclude a range. The {@link Range} must be finite range. */
    public Builder excluding(Range<Long> range) {
      checkArgument(
          range.hasLowerBound() && range.hasUpperBound(), "Infinite ranges are not supported");
      if (!range.isEmpty()) {
        _excluding.add(range.canonical(DiscreteDomain.longs()));
      }
      return this;
    }

    /** Exclude an {@link LongSpace} */
    public Builder excluding(LongSpace space) {
      space._rangeset.asRanges().forEach(this::excluding);
      return this;
    }

    /**
     * Returns true if this builder has exclusions only, no positive space.
     *
     * <p>Serves as utility function to determine if special handling for such negative-only cases
     * is required (otherwise empty spaces will be built)
     */
    public boolean hasExclusionsOnly() {
      return _including.isEmpty() && !_excluding.isEmpty();
    }

    /** Returns a new {@link LongSpace} */
    public LongSpace build() {
      RangeSet<Long> rangeSet = TreeRangeSet.create(_including);
      rangeSet.removeAll(_excluding);
      return new LongSpace(rangeSet);
    }

    @JsonCreator
    @Nonnull
    @VisibleForTesting
    static Builder create(@Nullable String s) {
      if (Strings.isNullOrEmpty(s)) {
        return builder();
      }
      String[] atoms = s.trim().split(",", -1);
      Builder builder = builder();
      Arrays.stream(atoms).forEach(atom -> processStringAtom(atom.trim(), builder));
      return builder;
    }

    private static Range<Long> parse(String s) {
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

    private static void processStringAtom(String s, Builder builder) {
      if (s.startsWith("!")) {
        builder.excluding(parse(s.replaceAll("!", "")));
      } else {
        builder.including(parse(s));
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LongSpace)) {
      return false;
    }
    LongSpace that = (LongSpace) o;
    return _rangeset.equals(that._rangeset);
  }

  @Override
  public int hashCode() {
    return _rangeset.hashCode();
  }

  private static String toRangeString(Range<Long> r) {
    long lower = r.lowerEndpoint();
    long upper = r.upperEndpoint() - 1;
    if (lower == upper) {
      return Long.toString(lower);
    }
    return lower + "-" + upper;
  }

  @JsonValue
  @Override
  public String toString() {
    return getRanges().stream().map(LongSpace::toRangeString).collect(Collectors.joining(","));
  }

  private static final long serialVersionUID = 1L;

  /**
   * Return least value.
   *
   * @throws NoSuchElementException if space is empty
   */
  public @Nonnull Long least() {
    return _rangeset.span().lowerEndpoint();
  }
}
