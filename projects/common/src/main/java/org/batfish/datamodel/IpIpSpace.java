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

public class IpIpSpace extends IpSpace {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (12 bytes seems smallest possible entry (long + int), would be 12 MiB total).
  private static final LoadingCache<Ip, IpIpSpace> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(CacheLoader.from(IpIpSpace::new));
  private static final String PROP_IP = "ip";

  private final Ip _ip;

  @JsonCreator
  static IpIpSpace create(@JsonProperty(PROP_IP) Ip ip) {
    return CACHE.getUnchecked(ip);
  }

  private IpIpSpace(Ip ip) {
    _ip = ip;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitIpIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return _ip.compareTo(((IpIpSpace) o)._ip);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _ip.equals(((IpIpSpace) o)._ip);
  }

  @JsonProperty(PROP_IP)
  public Ip getIp() {
    return _ip;
  }

  @Override
  public int hashCode() {
    return _ip.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(IpIpSpace.class).add(PROP_IP, _ip).toString();
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(_ip);
  }
}
