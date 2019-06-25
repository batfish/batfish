package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;

public class Ip6Prefix extends Prefix6Expr {

  private Ip6Expr _ip6;

  private IntExpr _prefixLength;

  @JsonCreator
  private Ip6Prefix() {}

  public Ip6Prefix(Ip6Expr ip6, IntExpr prefixLength) {
    _ip6 = ip6;
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
    Ip6Prefix other = (Ip6Prefix) obj;
    if (_ip6 == null) {
      if (other._ip6 != null) {
        return false;
      }
    } else if (!_ip6.equals(other._ip6)) {
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
  public Prefix6 evaluate(Environment env) {
    Ip6 ip6 = _ip6.evaluate(env);
    int prefixLength = _prefixLength.evaluate(env);
    return new Prefix6(ip6, prefixLength);
  }

  public Ip6Expr getIp6() {
    return _ip6;
  }

  public IntExpr getPrefixLength() {
    return _prefixLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_ip6 == null) ? 0 : _ip6.hashCode());
    result = prime * result + ((_prefixLength == null) ? 0 : _prefixLength.hashCode());
    return result;
  }

  public void setIp6(Ip6Expr ip6) {
    _ip6 = ip6;
  }

  public void setPrefixLength(IntExpr prefixLength) {
    _prefixLength = prefixLength;
  }
}
