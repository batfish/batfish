package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

/** An {@link Ip6Space} that contains a single IPv6 address. */
public class Ip6Ip6Space extends Ip6Space {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  private static final LoadingCache<Ip6, Ip6Ip6Space> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(CacheLoader.from(Ip6Ip6Space::new));
  private static final String PROP_IP6 = "ip6";

  private final Ip6 _ip6;

  @JsonCreator
  static Ip6Ip6Space create(@JsonProperty(PROP_IP6) Ip6 ip6) {
    return CACHE.getUnchecked(ip6);
  }

  private Ip6Ip6Space(Ip6 ip6) {
    _ip6 = ip6;
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> visitor) {
    return visitor.visitIp6Ip6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return _ip6.compareTo(((Ip6Ip6Space) o)._ip6);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _ip6.equals(((Ip6Ip6Space) o)._ip6);
  }

  @JsonProperty(PROP_IP6)
  public Ip6 getIp6() {
    return _ip6;
  }

  @Override
  public int hashCode() {
    return _ip6.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(Ip6Ip6Space.class).add(PROP_IP6, _ip6).toString();
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(_ip6);
  }
}
