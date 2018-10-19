package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Collection;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

/** An immutable class representing a set of AS numbers. */
@ParametersAreNonnullByDefault
public class AsSet implements Serializable, Comparable<AsSet> {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (set(long)), would be 1 MiB total).
  private static final Cache<ImmutableSortedSet<Long>, AsSet> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 16).build();

  private static final long serialVersionUID = 1L;

  private final ImmutableSortedSet<Long> _value;

  private AsSet(ImmutableSortedSet<Long> value) {
    _value = value;
  }

  /** Create a new empty {@link AsSet}. */
  public static AsSet empty() {
    return of(ImmutableSortedSet.of());
  }

  /** Create a new {@link AsSet} containing only the given ASN. */
  public static AsSet of(Long value) {
    return of(ImmutableSortedSet.of(value));
  }

  /** Create a new {@link AsSet} containing only the given ASN. */
  public static AsSet of(Long... value) {
    return of(ImmutableSortedSet.copyOf(value));
  }

  /** Create a new {@link AsSet} that is an immutable copy of {@code value}. */
  public static AsSet of(Collection<Long> value) {
    ImmutableSortedSet<Long> immutableValues = ImmutableSortedSet.copyOf(value);
    try {
      return CACHE.get(immutableValues, () -> new AsSet(immutableValues));
    } catch (ExecutionException e) {
      // This shouldn't happen, but handle anyway.
      return new AsSet(immutableValues);
    }
  }

  @JsonCreator
  private static AsSet jsonCreator(@Nullable ImmutableSortedSet<Long> value) {
    return AsSet.of(firstNonNull(value, ImmutableSortedSet.of()));
  }

  @Override
  public int compareTo(AsSet o) {
    return Comparators.lexicographical(Ordering.<Long>natural()).compare(_value, o._value);
  }

  public boolean containsAs(Long asn) {
    return _value.contains(asn);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof AsSet)) {
      return false;
    }
    return _value.equals(((AsSet) o)._value);
  }

  @JsonValue
  public SortedSet<Long> getAsns() {
    return _value;
  }

  @Override
  public int hashCode() {
    return _value.hashCode();
  }

  public boolean isEmpty() {
    return _value.isEmpty();
  }

  /** Returns a new {@link AsSet} that consists of this set with any private ASNs removed. */
  public AsSet removePrivateAs() {
    return AsSet.of(
        _value.stream().filter(asn -> !AsPath.isPrivateAs(asn)).collect(toImmutableList()));
  }

  public int size() {
    return _value.size();
  }

  @Override
  public String toString() {
    if (_value.size() == 1) {
      return _value.first().toString();
    }
    return "{" + StringUtils.join(_value, ",") + "}";
  }
}
