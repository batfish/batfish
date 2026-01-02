package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpWildcardIpSpace extends IpSpace {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (12 bytes seems smallest possible entry (long + int), would be 12 MiB total).
  private static final LoadingCache<IpWildcard, IpWildcardIpSpace> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(CacheLoader.from(IpWildcardIpSpace::new));
  private static final String PROP_IP_WILDCARD = "ipWildcard";

  private final IpWildcard _ipWildcard;

  @JsonCreator
  static IpWildcardIpSpace create(@JsonProperty(PROP_IP_WILDCARD) IpWildcard ipWildcard) {
    return CACHE.getUnchecked(ipWildcard);
  }

  private IpWildcardIpSpace(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitIpWildcardIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return _ipWildcard.compareTo(((IpWildcardIpSpace) o)._ipWildcard);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _ipWildcard.equals(((IpWildcardIpSpace) o)._ipWildcard);
  }

  @JsonProperty(PROP_IP_WILDCARD)
  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public int hashCode() {
    return _ipWildcard.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_IP_WILDCARD, _ipWildcard).toString();
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(_ipWildcard);
  }
}
