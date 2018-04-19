package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class PrefixIpSpace extends IpSpace {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_PREFIX = "prefix";

  private final Prefix _prefix;

  @JsonCreator
  PrefixIpSpace(@JsonProperty(PROP_PREFIX) Prefix prefix) {
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
  public IpSpace complement() {
    return AclIpSpace.builder()
        .thenRejecting(this)
        .thenPermitting(UniverseIpSpace.INSTANCE)
        .build();
  }

  @Override
  public boolean containsIp(Ip ip) {
    return _prefix.containsIp(ip);
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
    return MoreObjects.toStringHelper(getClass()).add(PROP_PREFIX, _prefix).toString();
  }
}
