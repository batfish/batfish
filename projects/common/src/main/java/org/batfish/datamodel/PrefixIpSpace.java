package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

@ParametersAreNonnullByDefault
public final class PrefixIpSpace extends IpSpace {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (12 bytes seems smallest possible entry (long + int), would be 12 MiB total).
  private static final LoadingCache<Prefix, PrefixIpSpace> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(CacheLoader.from(PrefixIpSpace::new));

  private static final String PROP_PREFIX = "prefix";

  private final Prefix _prefix;

  @JsonCreator
  static PrefixIpSpace create(@JsonProperty(PROP_PREFIX) Prefix prefix) {
    return CACHE.getUnchecked(prefix);
  }

  private PrefixIpSpace(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitPrefixIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return _prefix.compareTo(((PrefixIpSpace) o)._prefix);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _prefix.equals(((PrefixIpSpace) o)._prefix);
  }

  @JsonProperty(PROP_PREFIX)
  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return _prefix.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(PrefixIpSpace.class).add(PROP_PREFIX, _prefix).toString();
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(_prefix);
  }
}
