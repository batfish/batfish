package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

/** An immutable class representing a set of AS numbers. */
@ParametersAreNonnullByDefault
public class AsSet implements Serializable, Comparable<AsSet> {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (set(long)), would be 1 MiB total).
  private static final LoadingCache<AsSet, AsSet> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 16).build(CacheLoader.from(x -> x));

  private static final long serialVersionUID = 1L;

  private final long[] _value;

  private AsSet(long[] value) {
    Arrays.sort(value);
    _value = value;
  }

  /** Create a new empty {@link AsSet}. */
  public static AsSet empty() {
    return of();
  }

  /** Create a new {@link AsSet} that is for a single ASN. */
  public static AsSet of(long value) {
    return of(new long[] {value});
  }

  /**
   * Create a new {@link AsSet} that is an immutable copy of {@code value}.
   *
   * <p>Note: this {@link AsSet} will take ownership of the given {@code long[]}.
   */
  public static AsSet of(long... value) {
    AsSet set = new AsSet(value);
    return CACHE.getUnchecked(set);
  }

  @JsonCreator
  private static AsSet jsonCreator(@Nullable long[] value) {
    return AsSet.of(firstNonNull(value, new long[] {}));
  }

  @Override
  public int compareTo(AsSet o) {
    return Longs.lexicographicalComparator().compare(_value, o._value);
  }

  public boolean containsAs(long asn) {
    for (long l : _value) {
      if (l == asn) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof AsSet)) {
      return false;
    }
    return Arrays.equals(_value, ((AsSet) o)._value);
  }

  /** Expensive. */
  @VisibleForTesting
  @JsonValue
  public SortedSet<Long> getAsns() {
    return Arrays.stream(_value)
        .boxed()
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_value);
  }

  public boolean isEmpty() {
    return _value.length == 0;
  }

  /** Returns a new {@link AsSet} that consists of this set with any private ASNs removed. */
  public AsSet removePrivateAs() {
    return AsSet.of(Arrays.stream(_value).filter(asn -> !AsPath.isPrivateAs(asn)).toArray());
  }

  public int size() {
    return _value.length;
  }

  @Override
  public String toString() {
    if (_value.length == 1) {
      return Long.toString(_value[0]);
    }
    return "{" + StringUtils.join(_value, ",") + "}";
  }
}
