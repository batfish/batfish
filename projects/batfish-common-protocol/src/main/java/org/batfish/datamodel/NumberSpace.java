package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.BoundType;
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
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A finite, closed, not necessarily contiguous space of numbers. {@link NumberSpace}s are by design
 * <i>immutable</i>, but can be altered by converting {@link #toBuilder()} and rebuilding.
 */
@ParametersAreNonnullByDefault
public abstract class NumberSpace<
        T extends Number & Comparable<T>,
        S extends NumberSpace<T, S, B>,
        B extends NumberSpace.Builder<T, S, B>>
    implements Serializable {

  /** A builder for {@link NumberSpace} */
  public abstract static class Builder<
      T extends Number & Comparable<T>,
      S extends NumberSpace<T, S, B>,
      B extends Builder<T, S, B>> {
    /**
     * Helper method to populate {@code builder} to be used within subclass {@link JsonCreator}
     * method
     */
    @Nonnull
    @VisibleForTesting
    static <
            T extends Number & Comparable<T>,
            S extends NumberSpace<T, S, B>,
            B extends Builder<T, S, B>>
        void create(B builder, @Nullable String s) {
      if (Strings.isNullOrEmpty(s)) {
        return;
      }
      String[] atoms = s.trim().split(",", -1);
      Arrays.stream(atoms).forEach(atom -> builder.processStringAtom(atom.trim()));
    }

    private Set<Range<T>> _excluding;
    private Set<Range<T>> _including;

    protected Builder() {
      _including = new HashSet<>();
      _excluding = new HashSet<>();
    }

    protected Builder(S space) {
      this();
      _including.addAll(space._rangeset.asRanges());
    }

    /** Returns a new {@link NumberSpace} */
    public final S build() {
      RangeSet<T> rangeSet = TreeRangeSet.<T>create(_including);
      rangeSet.removeAll(_excluding);
      return build(rangeSet);
    }

    protected abstract S build(RangeSet<T> rangeSet);

    protected abstract DiscreteDomain<T> discreteDomain();

    /** Exclude a range. The {@link Range} must be finite. */
    public final B excluding(Range<T> range) {
      checkArgument(
          range.hasLowerBound() && range.hasUpperBound(), "Infinite ranges are not supported");
      if (!range.isEmpty()) {
        _excluding.add(range.canonical(discreteDomain()));
      }
      return getThis();
    }

    /** Exclude an {@link NumberSpace} */
    public final B excluding(S space) {
      space._rangeset.asRanges().forEach(this::excluding);
      return getThis();
    }

    /** Exclude a number. */
    public final B excluding(T range) {
      return excluding(Range.singleton(range));
    }

    protected abstract B getThis();

    /**
     * Returns true if this builder has exclusions only, no positive space.
     *
     * <p>Serves as utility function to determine if special handling for such negative-only cases
     * is required (otherwise empty spaces will be built)
     */
    public final boolean hasExclusionsOnly() {
      return _including.isEmpty() && !_excluding.isEmpty();
    }

    /** Include a range. The {@link Range} must be a finite range. */
    public final B including(Range<T> range) {
      checkArgument(
          range.hasLowerBound() && range.hasUpperBound(), "Infinite ranges are not supported");
      if (!range.isEmpty()) {
        _including.add(range.canonical(discreteDomain()));
      }
      return getThis();
    }

    /** Include an {@link NumberSpace} */
    public final B including(S space) {
      space._rangeset.asRanges().forEach(this::including);
      return getThis();
    }

    /** Include a number. */
    public final B including(@Nonnull T range) {
      return including(Range.singleton(range));
    }

    /** Include given {@link RangeSet}. */
    public final B including(RangeSet<T> rangeSet) {
      rangeSet.asRanges().forEach(this::including);
      return getThis();
    }

    /** Include given {@code points}. */
    public final B includingAll(Iterable<T> points) {
      points.forEach(this::including);
      return getThis();
    }

    /** Include given {@code ranges}. */
    public final B includingAllRanges(Iterable<Range<T>> ranges) {
      ranges.forEach(this::including);
      return getThis();
    }

    protected abstract @Nonnull Range<T> parse(String s);

    void processStringAtom(String s) {
      if (s.startsWith("!")) {
        excluding(parse(s.replaceAll("!", "")));
      } else {
        including(parse(s));
      }
    }
  }

  protected static final String ERROR_MESSAGE_TEMPLATE = "Invalid range specification %s";

  /*
   * Invariant: always ensure ranges are stored in canonical form (enforced in builder methods)
   * and immutable (enforced in constructor)
   */
  @Nonnull final RangeSet<T> _rangeset;

  protected NumberSpace(RangeSet<T> rangeset) {
    _rangeset = ImmutableRangeSet.copyOf(rangeset);
  }

  /** Check that this space *fully* contains the {@code other} {@link NumberSpace}. */
  public final boolean contains(S other) {
    return _rangeset.enclosesAll(other._rangeset.asRanges());
  }

  /** Check that this space contains a given {@code value}. */
  public final boolean contains(@Nonnull T value) {
    return _rangeset.contains(value);
  }

  /** Compute the difference between two {@link NumberSpace}s */
  public final S difference(S other) {
    return this.intersection(other.not(getThis()));
  }

  protected abstract DiscreteDomain<T> discreteDomain();

  /** Should be overridden with implementation that always returns same instance. */
  protected abstract @Nonnull S empty();

  /** Return an ordered set of numbers described by this space. */
  public abstract Set<T> enumerate();

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(getClass().isInstance(o))) {
      return false;
    }
    // safe cast since we just checked
    @SuppressWarnings("unchecked")
    S that = (S) o;
    return _rangeset.equals(that._rangeset);
  }

  @Override
  public final int hashCode() {
    return _rangeset.hashCode();
  }

  /** This space as a set of included {@link Range}s */
  public final Set<Range<T>> getRanges() {
    return _rangeset.asRanges();
  }

  protected abstract S getThis();

  /** Intersect two number spaces together. */
  public final S intersection(S other) {
    return newBuilder()
        .including(
            other._rangeset.asRanges().stream()
                .map(_rangeset::subRangeSet) // intersect individual ranges with _rangeset
                .map(RangeSet::asRanges) // flatten each intersection result to set of ranges
                .flatMap(Set::stream) // stream for collection
                .collect(ImmutableRangeSet.toImmutableRangeSet()))
        .build();
  }

  /** Returns true if this space is a contiguous space */
  public final boolean isContiguous() {
    return _rangeset.asRanges().size() <= 1;
  }

  /** Return true iff this space is empty (contains no values) */
  public final boolean isEmpty() {
    return _rangeset.isEmpty();
  }

  /** Return true iff this space is a singleton (contains exactly one value) */
  public final boolean isSingleton() {
    return getRanges().size() == 1 && isSingletonRange(getRanges().iterator().next());
  }

  private boolean isSingletonRange(Range<T> range) {
    // note that argument is guaranteed to be closedOpen Range
    return range.upperEndpoint().equals(discreteDomain().next(range.lowerEndpoint()));
  }

  /**
   * Return greatest value.
   *
   * @throws NoSuchElementException if space is empty
   */
  public final @Nonnull T greatest() {
    Range<T> span = _rangeset.span();
    T upperEndpoint = span.upperEndpoint();
    return span.upperBoundType() == BoundType.CLOSED
        ? upperEndpoint
        : discreteDomain().previous(upperEndpoint);
  }

  /**
   * Return least value.
   *
   * @throws NoSuchElementException if space is empty
   */
  public final @Nonnull T least() {
    Range<T> span = _rangeset.span();
    T lowerEndpoint = span.lowerEndpoint();
    return span.lowerBoundType() == BoundType.CLOSED
        ? lowerEndpoint
        : discreteDomain().next(lowerEndpoint);
  }

  protected abstract B newBuilder();

  /**
   * Take the complement of this space, bounded by existing lower and upper limits of the space.
   * This can be used as a way to represent a set of excluded ranges as a positive space.
   */
  public final S not() {
    if (_rangeset.isEmpty()) {
      return empty();
    }
    return newBuilder().including(_rangeset.complement().subRangeSet(_rangeset.span())).build();
  }

  /** Take the complement of this space, bounded by some other {@link NumberSpace} */
  public final S not(S within) {
    return newBuilder().build(_rangeset.complement()).intersection(within);
  }

  /**
   * Return singleton value if this space is a singleton. Otherwise throws {@link
   * NoSuchElementException}
   */
  public final @Nonnull T singletonValue() throws NoSuchElementException {
    if (!isSingleton()) {
      throw new NoSuchElementException();
    }
    return _rangeset.asRanges().iterator().next().lowerEndpoint();
  }

  /** Returns a stream of the included numbers. */
  public final Stream<T> stream() {
    return enumerate().stream();
  }

  /** Compute the symmetric difference between two {@link NumberSpace}s */
  public final S symmetricDifference(S other) {
    return this.union(other).difference(intersection(other));
  }

  /** Return a builder initialized with existing {@link NumberSpace} */
  public final B toBuilder() {
    return newBuilder().including(getThis());
  }

  private @Nonnull String toRangeString(Range<T> r) {
    return isSingletonRange(r)
        ? r.lowerEndpoint().toString()
        : String.format("%s-%s", r.lowerEndpoint(), discreteDomain().previous(r.upperEndpoint()));
  }

  @JsonValue
  @Override
  public final @Nonnull String toString() {
    return getRanges().stream().map(this::toRangeString).collect(Collectors.joining(","));
  }

  /** Union two {@link NumberSpace}s together */
  public final S union(S other) {
    B builder = toBuilder();
    other._rangeset.asRanges().forEach(builder::including);
    return builder.build();
  }

  /**
   * Returns a string representation of this NumberSpace as a subset of the given {@code
   * completeSpace}. Differs from {@link #toString()} in that this method can return {@code "all"}
   * (if this space equals the {@code completeSpace} or {@code "none"} (if this space is empty).
   */
  public String toStringAsSubsetOf(NumberSpace<?, ?, ?> completeSpace) {
    return isEmpty() ? "none" : equals(completeSpace) ? "all" : toString();
  }
}
