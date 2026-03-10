package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

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
  private final @Nullable Integer _srcPort;
  private final @Nullable Integer _dstPort;

  public SessionMatchExpr(
      IpProtocol ipProtocol,
      Ip srcIp,
      Ip dstIp,
      @Nullable Integer srcPort,
      @Nullable Integer dstPort) {
    checkArgument(
        (srcPort == null && dstPort == null) || (srcPort != null && dstPort != null),
        "srcPort and dstPort should be both null or both non-null");
    _ipProtocol = ipProtocol;
    _srcIp = srcIp;
    _dstIp = dstIp;
    _srcPort = srcPort;
    _dstPort = dstPort;
  }

  @JsonCreator
  private static SessionMatchExpr jsonCreator(
      @JsonProperty(PROP_IP_PROTOCOL) @Nullable IpProtocol ipProtocol,
      @JsonProperty(PROP_SRC_IP) @Nullable Ip srcIp,
      @JsonProperty(PROP_DST_IP) @Nullable Ip dstIp,
      @JsonProperty(PROP_SRC_PORT) @Nullable Integer srcPort,
      @JsonProperty(PROP_DST_PORT) @Nullable Integer dstPort) {
    checkArgument(ipProtocol != null, "Missing %s", PROP_IP_PROTOCOL);
    checkArgument(srcIp != null, "Missing %s", PROP_SRC_IP);
    checkArgument(dstIp != null, "Missing %s", PROP_DST_IP);
    return new SessionMatchExpr(ipProtocol, srcIp, dstIp, srcPort, dstPort);
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
  public @Nullable Integer getSrcPort() {
    return _srcPort;
  }

  @JsonProperty(PROP_DST_PORT)
  public @Nullable Integer getDstPort() {
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
