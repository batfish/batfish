package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.MoreObjects;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

@ParametersAreNonnullByDefault
public class PrefixIp6Space extends Ip6Space {
  private static final LoadingCache<Prefix6, PrefixIp6Space> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 20).build(PrefixIp6Space::new);

  private static final String PROP_PREFIX = "prefix";
  private final Prefix6 _prefix6;

  @JsonCreator
  private static PrefixIp6Space jsonCreator(@JsonProperty(PROP_PREFIX) @Nullable Prefix6 prefix6) {
    return CACHE.get(prefix6);
  }

  public PrefixIp6Space(Prefix6 prefix6) {
    _prefix6 = prefix6;
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> visitor) {
    return visitor.visitPrefixIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return _prefix6.compareTo(((PrefixIp6Space) o)._prefix6);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _prefix6.equals(((PrefixIp6Space) o)._prefix6);
  }

  @JsonProperty(PROP_PREFIX)
  public Prefix6 getPrefix() {
    return _prefix6;
  }

  @Override
  public int hashCode() {
    return _prefix6.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(PrefixIp6Space.class).add(PROP_PREFIX, _prefix6).toString();
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(_prefix6);
  }
}
