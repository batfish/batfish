package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

public class IpPrefix extends PrefixExpr {

  private static final long serialVersionUID = 1L;

  private IpExpr _ip;

  private IntExpr _prefixLength;

  @JsonCreator
  private IpPrefix() {}

  public IpPrefix(IpExpr ip, IntExpr prefixLength) {
    _ip = ip;
    _prefixLength = prefixLength;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    IpPrefix other = (IpPrefix) obj;
    if (_ip == null) {
      if (other._ip != null) {
        return false;
      }
    } else if (!_ip.equals(other._ip)) {
      return false;
    }
    if (_prefixLength == null) {
      if (other._prefixLength != null) {
        return false;
      }
    } else if (!_prefixLength.equals(other._prefixLength)) {
      return false;
    }
    return true;
  }

  @Override
  public Prefix evaluate(Environment env) {
    Ip ip = _ip.evaluate(env);
    int prefixLength = _prefixLength.evaluate(env);
    return new Prefix(ip, prefixLength);
  }

  public IpExpr getIp() {
    return _ip;
  }

  public IntExpr getPrefixLength() {
    return _prefixLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_ip == null) ? 0 : _ip.hashCode());
    result = prime * result + ((_prefixLength == null) ? 0 : _prefixLength.hashCode());
    return result;
  }

  public void setIp(IpExpr ip) {
    _ip = ip;
  }

  public void setPrefixLength(IntExpr prefixLength) {
    _prefixLength = prefixLength;
  }
}
