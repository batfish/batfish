package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import javax.annotation.Nonnull;
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
      Caffeine.newBuilder().softValues().maximumSize(1 << 16).build(x -> x);
  private static final String PROP_ASNS = "asns";
  private static final String PROP_CONFEDERATION = "confederation";

  private final long[] _value;
  private final boolean _confederation;
  @LazyInit private transient int _hashCode;

  private AsSet(long[] value, boolean confederation) {
    Arrays.sort(value);
    _value = value;
    _confederation = confederation;
  }

  /** Create a new empty {@link AsSet}. */
  public static AsSet empty() {
    return of();
  }

  /** Create a new {@link AsSet} that is for a single ASN. */
  public static AsSet of(long value) {
    long[] asArray = new long[] {value};
    return of(asArray);
  }

  /**
   * Create a new {@link AsSet} that is an immutable copy of {@code value}.
   *
   * <p>Note: this {@link AsSet} will take ownership of the given {@code long[]}.
   */
  public static AsSet of(long... value) {
    AsSet set = new AsSet(value, false);
    return CACHE.get(set);
  }

  /** Create a new empty confederation {@link AsSet}. */
  public static AsSet confedEmpty() {
    return confed();
  }

  /** Create a new confederation {@link AsSet} that is an immutable copy of {@code value}. */
  public static AsSet confed(long... value) {
    AsSet set = new AsSet(value, true);
    return CACHE.get(set);
  }

  @JsonCreator
  private static AsSet jsonCreator(
      // Keep backwards compatibility in deserialization
      @Nullable JsonNode data) {
    if (data == null) {
      return AsSet.empty();
    }
    if (data.isArray()) {
      // Old array format: treat as regular as set
      return AsSet.of(getValues(data));
    } else if (data.isObject()) {
      JsonNode propConfed = data.get(PROP_CONFEDERATION);
      if (propConfed == null || !propConfed.asBoolean(Boolean.FALSE)) {
        return AsSet.of(getValues(data.get(PROP_ASNS)));
      } else {
        return AsSet.confed(getValues(data.get(PROP_ASNS)));
      }
    } else if (data.canConvertToLong()) {
      return AsSet.of(data.asLong());
    } else {
      throw new IllegalArgumentException(
          String.format("Cannot deserialize %s from %s", AsSet.class, data));
    }
  }

  /** Convert JsonNode to an array of longs */
  private static long[] getValues(JsonNode data) {
    long[] values = new long[data.size()];
    int i = 0;
    Iterator<JsonNode> iterator = data.elements();
    while (iterator.hasNext()) {
      JsonNode v = iterator.next();
      assert v.isNumber();
      values[i++] = v.asLong();
    }
    return values;
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
    return Arrays.equals(_value, ((AsSet) o)._value)
        && _confederation == ((AsSet) o)._confederation;
  }

  /** Expensive. */
  @VisibleForTesting
  @JsonProperty(PROP_ASNS)
  public SortedSet<Long> getAsns() {
    return Arrays.stream(_value)
        .boxed()
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = Arrays.hashCode(_value) * 31 + Boolean.hashCode(_confederation);
      _hashCode = h;
    }
    return h;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return size() == 0;
  }

  /** Returns true if this AsSet is of type {@code AS_CONFED_SEQUENCE} or {@code AS_CONFED_SET} */
  @JsonProperty(PROP_CONFEDERATION)
  public boolean isConfederationAsSet() {
    return _confederation;
  }

  /** Returns a new {@link AsSet} that consists of this set with any private ASNs removed. */
  public AsSet removePrivateAs() {
    return AsSet.of(Arrays.stream(_value).filter(asn -> !AsPath.isPrivateAs(asn)).toArray());
  }

  /** Returns a new {@link AsSet} that consists of this set with the provided ASNs removed. */
  public @Nonnull AsSet removeASNs(Collection<Long> asns) {
    return AsSet.of(Arrays.stream(_value).filter(asn -> !asns.contains(asn)).toArray());
  }

  public int size() {
    return _value.length;
  }

  @Override
  public String toString() {
    if (_value.length == 1) {
      return Long.toString(_value[0]);
    }
    return "{" + StringUtils.join(_value, ',') + "}";
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
