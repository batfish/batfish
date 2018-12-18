package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Map;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpIpSpace extends IpSpace {

  private static final String PROP_IP = "ip";

  /** */
  private static final long serialVersionUID = 1L;

  private final Ip _ip;

  @JsonCreator
  IpIpSpace(@JsonProperty(PROP_IP) Ip ip) {
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
  public boolean containsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    return _ip.equals(ip);
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
    return MoreObjects.toStringHelper(getClass()).add(PROP_IP, _ip).toString();
  }
}
