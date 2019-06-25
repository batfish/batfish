package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class IpPrefix extends PrefixExpr {
  private static final String PROP_IP = "ip";
  private static final String PROP_PREFIX_LENGTH = "prefixLength";

  @Nonnull private IpExpr _ip;

  @Nonnull private IntExpr _prefixLength;

  @JsonCreator
  private static IpPrefix jsonCreator(
      @Nullable @JsonProperty(PROP_IP) IpExpr ip,
      @Nullable @JsonProperty(PROP_PREFIX_LENGTH) IntExpr prefixLength) {
    checkArgument(ip != null, "%s must be provided", PROP_IP);
    checkArgument(prefixLength != null, "%s must be provided", PROP_PREFIX_LENGTH);
    return new IpPrefix(ip, prefixLength);
  }

  public IpPrefix(IpExpr ip, IntExpr prefixLength) {
    _ip = ip;
    _prefixLength = prefixLength;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof IpPrefix)) {
      return false;
    }
    IpPrefix other = (IpPrefix) obj;
    return _ip.equals(other._ip) && _prefixLength.equals(other._prefixLength);
  }

  @Override
  public Prefix evaluate(Environment env) {
    Ip ip = _ip.evaluate(env);
    int prefixLength = _prefixLength.evaluate(env);
    return Prefix.create(ip, prefixLength);
  }

  @JsonProperty(PROP_IP)
  @Nonnull
  public IpExpr getIp() {
    return _ip;
  }

  @JsonProperty(PROP_PREFIX_LENGTH)
  @Nonnull
  public IntExpr getPrefixLength() {
    return _prefixLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ip.hashCode();
    result = prime * result + _prefixLength.hashCode();
    return result;
  }

  public void setIp(IpExpr ip) {
    _ip = ip;
  }

  public void setPrefixLength(IntExpr prefixLength) {
    _prefixLength = prefixLength;
  }
}
