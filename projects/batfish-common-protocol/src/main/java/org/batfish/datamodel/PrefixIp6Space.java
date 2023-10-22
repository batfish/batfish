package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

@ParametersAreNonnullByDefault
public class PrefixIp6Space extends Ip6Space {
  private static final String PROP_PREFIX = "prefix";
  private final Prefix6 _prefix6;

  private PrefixIp6Space(Prefix6 prefix6) {
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
  public String toString() {
    return MoreObjects.toStringHelper(PrefixIp6Space.class).add(PROP_PREFIX, _prefix6).toString();
  }
}
