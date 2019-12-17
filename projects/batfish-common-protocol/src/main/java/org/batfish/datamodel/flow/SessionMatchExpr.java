package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** Represents a match criteria for a {@link FirewallSessionTraceInfo} */
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

  @JsonProperty(PROP_IP_PROTOCOL)
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty(PROP_SRC_IP)
  public Ip getSrcIp() {
    return _srcIp;
  }

  @JsonProperty(PROP_DST_IP)
  public Ip getDstIp() {
    return _dstIp;
  }

  @JsonProperty(PROP_SRC_PORT)
  @Nullable
  public Integer getSrcPort() {
    return _srcPort;
  }

  @JsonProperty(PROP_DST_PORT)
  @Nullable
  public Integer getDstPort() {
    return _dstPort;
  }

  /** Transforms into generic {@link AclLineMatchExpr} which allows usage with visitors */
  public AclLineMatchExpr toAclLineMatchExpr() {
    HeaderSpace.Builder hb =
        HeaderSpace.builder()
            .setSrcIps(_srcIp.toIpSpace())
            .setDstIps(_dstIp.toIpSpace())
            .setIpProtocols(ImmutableList.of(_ipProtocol));

    if (_srcPort != null) {
      hb.setSrcPorts(ImmutableList.of(SubRange.singleton(_srcPort)))
          .setDstPorts(ImmutableList.of(SubRange.singleton(_dstPort)));
    }

    return new MatchHeaderSpace(hb.build());
  }

  @Override
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

  @Override
  public int hashCode() {
    return Objects.hash(_ipProtocol, _srcIp, _dstIp, _srcPort, _dstPort);
  }
}
