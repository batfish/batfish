package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;

@JsonTypeName("SessionMatchExpr")
@ParametersAreNonnullByDefault
public class SessionMatchExpr {
  private static final String PROP_IP_PROTOCOL = "ipProtocol";
  private static final String PROP_SRC_IP = "srcIp";
  private static final String PROP_DST_IP = "dstIp";
  private static final String PROP_SRC_PORT = "srcPort";
  private static final String PROP_DST_PORT = "dstPort";

  private final IpProtocol _ipProtocol;
  private final Ip _srcIp;
  private final Ip _dstIp;
  @Nullable private final Integer _srcPort;
  @Nullable private final Integer _dstPort;

  @JsonCreator
  public SessionMatchExpr(
      @JsonProperty(PROP_IP_PROTOCOL) IpProtocol ipProtocol,
      @JsonProperty(PROP_SRC_IP) Ip srcIp,
      @JsonProperty(PROP_DST_IP) Ip dstIp,
      @JsonProperty(PROP_SRC_PORT) @Nullable Integer srcPort,
      @JsonProperty(PROP_DST_PORT) @Nullable Integer dstPort) {
    _ipProtocol = ipProtocol;
    _srcIp = srcIp;
    _dstIp = dstIp;
    _srcPort = srcPort;
    _dstPort = dstPort;
  }

  @JsonProperty
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty
  public Ip getSrcIp() {
    return _srcIp;
  }

  @JsonProperty
  public Ip getDstIp() {
    return _dstIp;
  }

  @JsonProperty
  @Nullable
  public Integer getSrcPort() {
    return _srcPort;
  }

  @JsonProperty
  @Nullable
  public Integer getDstPort() {
    return _dstPort;
  }

  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SessionMatchExpr)) {
      return false;
    }
    SessionMatchExpr rhs = (SessionMatchExpr) o;
    return Objects.equals(_ipProtocol, rhs._ipProtocol)
        && Objects.equals(_srcIp, rhs._srcIp)
        && Objects.equals(_dstIp, rhs._dstIp)
        && Objects.equals(_srcPort, rhs._srcPort)
        && Objects.equals(_dstPort, rhs._dstPort);
  }

  public int hashCode() {
    return Objects.hash(_ipProtocol, _srcIp, _dstIp, _srcPort, _dstPort);
  }
}
