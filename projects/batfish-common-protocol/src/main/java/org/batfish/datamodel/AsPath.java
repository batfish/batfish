package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Predicates;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

@ParametersAreNonnullByDefault
public final class AsPath implements Serializable, Comparable<AsPath> {

  private static final AsPath EMPTY = new AsPath(ImmutableList.of());

  /**
   * Returns true iff the provided AS number is reserved for private use by RFC 6696:
   * https://tools.ietf.org/html/rfc6996#section-5
   */
  public static boolean isPrivateAs(long as) {
    return (as >= 64512L && as <= 65534L) || (as >= 4200000000L && as <= 4294967294L);
  }

  public static AsPath ofSingletonAsSets(Long... asNums) {
    return ofSingletonAsSets(Arrays.asList(asNums));
  }

  public static AsPath ofSingletonAsSets(List<Long> asNums) {
    return of(asNums.stream().map(AsSet::of).collect(ImmutableList.toImmutableList()));
  }

  public static AsPath ofAsSets(AsSet... asSets) {
    return of(Arrays.asList(asSets));
  }

  private final List<AsSet> _asSets;

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  //   (24 bytes seems smallest possible entry (list(set(long)), would be 1.5 MiB total).
  private static final LoadingCache<ImmutableList<AsSet>, AsPath> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 16)
          .build(CacheLoader.from(AsPath::new));

  private AsPath(ImmutableList<AsSet> asSets) {
    _asSets = asSets;
  }

  @JsonCreator
  private static AsPath jsonCreator(@Nullable ImmutableList<AsSet> value) {
    return of(firstNonNull(value, ImmutableList.of()));
  }

  /** Create and return a new empty {@link AsPath}. */
  public static AsPath empty() {
    return EMPTY;
  }

  /** Create and return a new {@link AsPath} of length 1 using the given {@link AsSet}. */
  public static AsPath of(AsSet asSet) {
    return AsPath.of(ImmutableList.of(asSet));
  }

  /** Create and return a new {@link AsPath} of the given {@link AsSet AsSets}. */
  public static AsPath of(List<AsSet> asSets) {
    if (asSets.isEmpty()) {
      return empty();
    }
    ImmutableList<AsSet> immutableValue = ImmutableList.copyOf(asSets);
    return CACHE.getUnchecked(immutableValue);
  }

  /**
   * Returns a new {@link AsPath} with all the private ASNs removed. Any {@link AsSet} in that path
   * that consists only of private ASNs will be dropped from the path entirely.
   */
  public AsPath removePrivateAs() {
    return AsPath.of(
        _asSets.stream()
            .map(AsSet::removePrivateAs)
            .filter(asSet -> !asSet.isEmpty())
            .collect(ImmutableList.toImmutableList()));
  }

  /** Returns a new {@link AsPath} with all confederation {@link AsSet AS sets} removed */
  public AsPath removeConfederations() {
    return AsPath.of(
        _asSets.stream()
            .filter(asSet -> !asSet.isConfederationAsSet())
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Returns a new {@link AsPath} with all specified ASNs removed form any {@link AsSet} in the
   * path.
   */
  public @Nonnull AsPath removeASNs(Collection<Long> asns) {
    return AsPath.of(
        _asSets.stream()
            .map(asSet -> asSet.removeASNs(asns))
            .filter(asSet -> !asSet.isEmpty())
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public int compareTo(AsPath rhs) {
    return Comparators.lexicographical(Ordering.<AsSet>natural()).compare(_asSets, rhs._asSets);
  }

  public boolean containsAs(Long as) {
    for (AsSet a : _asSets) {
      if (a.containsAs(as)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof AsPath)) {
      return false;
    }
    AsPath other = (AsPath) obj;
    return _asSets.equals(other._asSets);
  }

  public String getAsPathString() {
    return StringUtils.join(_asSets, " ");
  }

  @JsonValue
  public List<AsSet> getAsSets() {
    return _asSets;
  }

  @Override
  public int hashCode() {
    return _asSets.hashCode();
  }

  public int size() {
    return _asSets.size();
  }

  /** Return the length for this AS path as required for BGP path selection algorithm */
  public int length() {
    return (int) _asSets.stream().filter(Predicates.not(AsSet::isConfederationAsSet)).count();
  }

  @Override
  public String toString() {
    return _asSets.toString();
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return of(_asSets);
  }
}
