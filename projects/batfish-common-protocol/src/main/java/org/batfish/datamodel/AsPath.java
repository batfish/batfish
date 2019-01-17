package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;

@ParametersAreNonnullByDefault
public class AsPath implements Serializable, Comparable<AsPath> {

  private static final long serialVersionUID = 1L;

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

  private final List<AsSet> _asSets;

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^16: Just some upper bound on cache size, well less than GiB.
  //   (24 bytes seems smallest possible entry (list(set(long)), would be 1.5 MiB total).
  private static final Cache<List<AsSet>, AsPath> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 16).build();

  private AsPath(ImmutableList<AsSet> asSets) {
    _asSets = asSets;
  }

  @JsonCreator
  private static AsPath jsonCreator(@Nullable ImmutableList<AsSet> value) {
    return of(firstNonNull(value, ImmutableList.of()));
  }

  /** Create and return a new empty {@link AsPath}. */
  public static AsPath empty() {
    return AsPath.of(ImmutableList.of());
  }

  /** Create and return a new {@link AsPath} of length 1 using the given {@link AsSet}. */
  public static AsPath of(AsSet asSet) {
    return AsPath.of(ImmutableList.of(asSet));
  }

  /** Create and return a new {@link AsPath} of the given {@link AsSet AsSets}. */
  public static AsPath of(List<AsSet> asSets) {
    ImmutableList<AsSet> immutableValue = ImmutableList.copyOf(asSets);
    try {
      return CACHE.get(immutableValue, () -> new AsPath(immutableValue));
    } catch (ExecutionException e) {
      // This shouldn't happen, but handle anyway.
      return new AsPath(immutableValue);
    }
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

  @Override
  public int compareTo(AsPath rhs) {
    return Comparators.lexicographical(Ordering.<AsSet>natural()).compare(_asSets, rhs._asSets);
  }

  public boolean containsAs(Long as) {
    return _asSets.stream().anyMatch(a -> a.containsAs(as));
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

  @Override
  public String toString() {
    return _asSets.toString();
  }
}
