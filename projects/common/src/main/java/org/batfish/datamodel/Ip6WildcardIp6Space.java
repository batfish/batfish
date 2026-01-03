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

public class Ip6WildcardIp6Space extends Ip6Space {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  private static final LoadingCache<Ip6Wildcard, Ip6WildcardIp6Space> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(CacheLoader.from(Ip6WildcardIp6Space::new));
  private static final String PROP_IP6_WILDCARD = "ip6Wildcard";

  private final Ip6Wildcard _ip6Wildcard;

  @JsonCreator
  static Ip6WildcardIp6Space create(@JsonProperty(PROP_IP6_WILDCARD) Ip6Wildcard ip6Wildcard) {
    return CACHE.getUnchecked(ip6Wildcard);
  }

  private Ip6WildcardIp6Space(Ip6Wildcard ip6Wildcard) {
    _ip6Wildcard = ip6Wildcard;
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> visitor) {
    return visitor.visitIp6WildcardIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return _ip6Wildcard.compareTo(((Ip6WildcardIp6Space) o)._ip6Wildcard);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _ip6Wildcard.equals(((Ip6WildcardIp6Space) o)._ip6Wildcard);
  }

  @JsonProperty(PROP_IP6_WILDCARD)
  public Ip6Wildcard getIp6Wildcard() {
    return _ip6Wildcard;
  }

  @Override
  public int hashCode() {
    return _ip6Wildcard.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_IP6_WILDCARD, _ip6Wildcard).toString();
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(_ip6Wildcard);
  }
}
