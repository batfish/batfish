package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Map;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpWildcardIpSpace extends IpSpace {
  private static final String PROP_IP_WILDCARD = "ipWildcard";

  private static final long serialVersionUID = 1L;

  private final IpWildcard _ipWildcard;

  @JsonCreator
  IpWildcardIpSpace(@JsonProperty(PROP_IP_WILDCARD) IpWildcard ipWildcard) {
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
  public boolean containsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    return _ipWildcard.containsIp(ip);
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
}
