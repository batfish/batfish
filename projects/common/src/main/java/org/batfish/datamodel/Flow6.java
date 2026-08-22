package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An IPv6 packet flow.
 *
 * <p>This class is intentionally parallel to {@link Flow} rather than widening
 * the IPv4 Flow class. That keeps existing IPv4 ACL, transformation, and
 * traceroute behavior source-compatible while IPv6 packet processing is
 * introduced incrementally.
 */
@ParametersAreNonnullByDefault
public final class Flow6 implements Serializable {

  public static final class Builder {

    private int _dscp;
    private @Nonnull Ip6 _dstIp;
    private @Nullable Integer _dstPort;
    private int _ecn;
    private int _fragmentOffset;
    private @Nullable Integer _icmpCode;
    private @Nullable Integer _icmpType;
    private @Nullable String _ingressInterface;
    private @Nullable String _ingressNode;
    private @Nonnull String _ingressVrf;
    private @Nullable IpProtocol _ipProtocol;
    private int _packetLength;
    private @Nonnull Ip6 _srcIp;
    private @Nullable Integer _srcPort;

    private Builder() {
      _srcIp = Ip6.ZERO;
      _dstIp = Ip6.ZERO;
      _ingressVrf = Configuration.DEFAULT_VRF_NAME;
      _packetLength = 64;
    }

    public Flow6 build() {
      checkNotNull(
          _ingressNode,
          "Cannot build IPv6 flow without specifying ingress node");

      checkArgument(
          _dscp >= 0 && _dscp <= 63,
          "Invalid DSCP value %s",
          _dscp);

      checkArgument(
          _ecn >= 0 && _ecn <= 3,
          "Invalid ECN value %s",
          _ecn);

      checkArgument(
          _fragmentOffset >= 0 && _fragmentOffset <= 8191,
          "Invalid IPv6 fragment offset %s",
          _fragmentOffset);

      IpProtocol protocol =
          firstNonNull(
              _ipProtocol,
              IpProtocol.HOPOPT);

      @Nullable Integer srcPort = _srcPort;
      @Nullable Integer dstPort = _dstPort;
      @Nullable Integer icmpType = _icmpType;
      @Nullable Integer icmpCode = _icmpCode;

      if (IpProtocol.IP_PROTOCOLS_WITH_PORTS.contains(protocol)) {
        checkArgument(
            srcPort != null,
            "%s packets must have a source port",
            protocol);
        checkArgument(
            dstPort != null,
            "%s packets must have a destination port",
            protocol);

        checkPort(srcPort);
        checkPort(dstPort);
      } else {
        srcPort = null;
        dstPort = null;
      }

      if (protocol == IpProtocol.IPV6_ICMP) {
        checkArgument(
            icmpType != null,
            "ICMPv6 packets must have an ICMP type");
        checkArgument(
            icmpCode != null,
            "ICMPv6 packets must have an ICMP code");
        checkArgument(
            icmpType >= 0 && icmpType <= 255,
            "Invalid ICMPv6 type %s",
            icmpType);
        checkArgument(
            icmpCode >= 0 && icmpCode <= 255,
            "Invalid ICMPv6 code %s",
            icmpCode);
      } else {
        icmpType = null;
        icmpCode = null;
      }

      checkArgument(
          _packetLength >= 40,
          "IPv6 packets must be at least 40 bytes");

      if (protocol == IpProtocol.TCP) {
        checkArgument(
            _packetLength >= 60,
            "IPv6 TCP packets must be at least 60 bytes");
      } else if (protocol == IpProtocol.UDP) {
        checkArgument(
            _packetLength >= 48,
            "IPv6 UDP packets must be at least 48 bytes");
      } else if (protocol == IpProtocol.IPV6_ICMP) {
        checkArgument(
            _packetLength >= 48,
            "ICMPv6 packets must be at least 48 bytes");
      }

      return new Flow6(
          _ingressNode,
          _ingressInterface,
          _ingressVrf,
          _srcIp,
          _dstIp,
          srcPort,
          dstPort,
          protocol,
          _dscp,
          _ecn,
          _fragmentOffset,
          icmpType,
          icmpCode,
          _packetLength);
    }

    private static void checkPort(int port) {
      checkArgument(
          port >= 0 && port <= 65535,
          "Invalid transport port %s",
          port);
    }

    public Builder setDscp(int dscp) {
      _dscp = dscp;
      return this;
    }

    public Builder setDstIp(Ip6 dstIp) {
      _dstIp = dstIp;
      return this;
    }

    public Builder setDstPort(@Nullable Integer dstPort) {
      _dstPort = dstPort;
      return this;
    }

    public Builder setEcn(int ecn) {
      _ecn = ecn;
      return this;
    }

    public Builder setFragmentOffset(int fragmentOffset) {
      _fragmentOffset = fragmentOffset;
      return this;
    }

    public Builder setIcmpCode(@Nullable Integer icmpCode) {
      _icmpCode = icmpCode;
      return this;
    }

    public Builder setIcmpType(@Nullable Integer icmpType) {
      _icmpType = icmpType;
      return this;
    }

    public Builder setIngressInterface(
        @Nullable String ingressInterface) {
      _ingressInterface = ingressInterface;
      return this;
    }

    public Builder setIngressNode(String ingressNode) {
      _ingressNode = ingressNode;
      return this;
    }

    public Builder setIngressVrf(String ingressVrf) {
      _ingressVrf = ingressVrf;
      return this;
    }

    public Builder setIpProtocol(
        @Nullable IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
      return this;
    }

    public Builder setPacketLength(int packetLength) {
      _packetLength = packetLength;
      return this;
    }

    public Builder setSrcIp(Ip6 srcIp) {
      _srcIp = srcIp;
      return this;
    }

    public Builder setSrcPort(@Nullable Integer srcPort) {
      _srcPort = srcPort;
      return this;
    }
  }

  private static final String PROP_DSCP = "dscp";
  private static final String PROP_DST_IP = "dstIp";
  private static final String PROP_DST_PORT = "dstPort";
  private static final String PROP_ECN = "ecn";
  private static final String PROP_FRAGMENT_OFFSET = "fragmentOffset";
  private static final String PROP_ICMP_CODE = "icmpCode";
  private static final String PROP_ICMP_TYPE = "icmpType";
  private static final String PROP_INGRESS_INTERFACE = "ingressInterface";
  private static final String PROP_INGRESS_NODE = "ingressNode";
  private static final String PROP_INGRESS_VRF = "ingressVrf";
  private static final String PROP_IP_PROTOCOL = "ipProtocol";
  private static final String PROP_PACKET_LENGTH = "packetLength";
  private static final String PROP_SRC_IP = "srcIp";
  private static final String PROP_SRC_PORT = "srcPort";

  public static Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static Flow6 create(
      @JsonProperty(PROP_INGRESS_NODE)
          @Nullable String ingressNode,
      @JsonProperty(PROP_INGRESS_INTERFACE)
          @Nullable String ingressInterface,
      @JsonProperty(PROP_INGRESS_VRF)
          @Nullable String ingressVrf,
      @JsonProperty(PROP_SRC_IP)
          @Nullable Ip6 srcIp,
      @JsonProperty(PROP_DST_IP)
          @Nullable Ip6 dstIp,
      @JsonProperty(PROP_SRC_PORT)
          @Nullable Integer srcPort,
      @JsonProperty(PROP_DST_PORT)
          @Nullable Integer dstPort,
      @JsonProperty(PROP_IP_PROTOCOL)
          @Nullable IpProtocol ipProtocol,
      @JsonProperty(PROP_DSCP)
          @Nullable Integer dscp,
      @JsonProperty(PROP_ECN)
          @Nullable Integer ecn,
      @JsonProperty(PROP_FRAGMENT_OFFSET)
          @Nullable Integer fragmentOffset,
      @JsonProperty(PROP_ICMP_TYPE)
          @Nullable Integer icmpType,
      @JsonProperty(PROP_ICMP_CODE)
          @Nullable Integer icmpCode,
      @JsonProperty(PROP_PACKET_LENGTH)
          @Nullable Integer packetLength) {

    Builder builder = builder();

    checkArgument(
        ingressNode != null,
        "Missing %s",
        PROP_INGRESS_NODE);

    builder
        .setIngressNode(ingressNode)
        .setIngressInterface(ingressInterface)
        .setIngressVrf(
            firstNonNull(
                ingressVrf,
                Configuration.DEFAULT_VRF_NAME))
        .setSrcIp(firstNonNull(srcIp, Ip6.ZERO))
        .setDstIp(firstNonNull(dstIp, Ip6.ZERO))
        .setSrcPort(srcPort)
        .setDstPort(dstPort)
        .setIpProtocol(ipProtocol)
        .setDscp(firstNonNull(dscp, 0))
        .setEcn(firstNonNull(ecn, 0))
        .setFragmentOffset(
            firstNonNull(fragmentOffset, 0))
        .setIcmpType(icmpType)
        .setIcmpCode(icmpCode)
        .setPacketLength(
            firstNonNull(packetLength, 64));

    return builder.build();
  }

  private Flow6(
      String ingressNode,
      @Nullable String ingressInterface,
      String ingressVrf,
      Ip6 srcIp,
      Ip6 dstIp,
      @Nullable Integer srcPort,
      @Nullable Integer dstPort,
      IpProtocol ipProtocol,
      int dscp,
      int ecn,
      int fragmentOffset,
      @Nullable Integer icmpType,
      @Nullable Integer icmpCode,
      int packetLength) {
    _ingressNode = ingressNode;
    _ingressInterface = ingressInterface;
    _ingressVrf = ingressVrf;
    _srcIp = srcIp;
    _dstIp = dstIp;
    _srcPort = srcPort;
    _dstPort = dstPort;
    _ipProtocol = ipProtocol;
    _dscp = dscp;
    _ecn = ecn;
    _fragmentOffset = fragmentOffset;
    _icmpType = icmpType;
    _icmpCode = icmpCode;
    _packetLength = packetLength;
  }

  @JsonProperty(PROP_DSCP)
  public int getDscp() {
    return _dscp;
  }

  @JsonProperty(PROP_DST_IP)
  public @Nonnull Ip6 getDstIp() {
    return _dstIp;
  }

  @JsonProperty(PROP_DST_PORT)
  public @Nullable Integer getDstPort() {
    return _dstPort;
  }

  @JsonProperty(PROP_ECN)
  public int getEcn() {
    return _ecn;
  }

  @JsonProperty(PROP_FRAGMENT_OFFSET)
  public int getFragmentOffset() {
    return _fragmentOffset;
  }

  @JsonProperty(PROP_ICMP_CODE)
  public @Nullable Integer getIcmpCode() {
    return _icmpCode;
  }

  @JsonProperty(PROP_ICMP_TYPE)
  public @Nullable Integer getIcmpType() {
    return _icmpType;
  }

  @JsonProperty(PROP_INGRESS_INTERFACE)
  public @Nullable String getIngressInterface() {
    return _ingressInterface;
  }

  @JsonProperty(PROP_INGRESS_NODE)
  public @Nonnull String getIngressNode() {
    return _ingressNode;
  }

  @JsonProperty(PROP_INGRESS_VRF)
  public @Nonnull String getIngressVrf() {
    return _ingressVrf;
  }

  @JsonProperty(PROP_IP_PROTOCOL)
  public @Nonnull IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty(PROP_PACKET_LENGTH)
  public int getPacketLength() {
    return _packetLength;
  }

  @JsonProperty(PROP_SRC_IP)
  public @Nonnull Ip6 getSrcIp() {
    return _srcIp;
  }

  @JsonProperty(PROP_SRC_PORT)
  public @Nullable Integer getSrcPort() {
    return _srcPort;
  }

  public Builder toBuilder() {
    return builder()
        .setIngressNode(_ingressNode)
        .setIngressInterface(_ingressInterface)
        .setIngressVrf(_ingressVrf)
        .setSrcIp(_srcIp)
        .setDstIp(_dstIp)
        .setSrcPort(_srcPort)
        .setDstPort(_dstPort)
        .setIpProtocol(_ipProtocol)
        .setDscp(_dscp)
        .setEcn(_ecn)
        .setFragmentOffset(_fragmentOffset)
        .setIcmpType(_icmpType)
        .setIcmpCode(_icmpCode)
        .setPacketLength(_packetLength);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Flow6)) {
      return false;
    }

    Flow6 rhs = (Flow6) o;

    return _dscp == rhs._dscp
        && _ecn == rhs._ecn
        && _fragmentOffset == rhs._fragmentOffset
        && _packetLength == rhs._packetLength
        && _dstIp.equals(rhs._dstIp)
        && Objects.equals(_dstPort, rhs._dstPort)
        && Objects.equals(_icmpCode, rhs._icmpCode)
        && Objects.equals(_icmpType, rhs._icmpType)
        && Objects.equals(
            _ingressInterface,
            rhs._ingressInterface)
        && _ingressNode.equals(rhs._ingressNode)
        && _ingressVrf.equals(rhs._ingressVrf)
        && _ipProtocol == rhs._ipProtocol
        && _srcIp.equals(rhs._srcIp)
        && Objects.equals(_srcPort, rhs._srcPort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _dscp,
        _dstIp,
        _dstPort,
        _ecn,
        _fragmentOffset,
        _icmpCode,
        _icmpType,
        _ingressInterface,
        _ingressNode,
        _ingressVrf,
        _ipProtocol,
        _packetLength,
        _srcIp,
        _srcPort);
  }

  private final int _dscp;
  private final @Nonnull Ip6 _dstIp;
  private final @Nullable Integer _dstPort;
  private final int _ecn;
  private final int _fragmentOffset;
  private final @Nullable Integer _icmpCode;
  private final @Nullable Integer _icmpType;
  private final @Nullable String _ingressInterface;
  private final @Nonnull String _ingressNode;
  private final @Nonnull String _ingressVrf;
  private final @Nonnull IpProtocol _ipProtocol;
  private final int _packetLength;
  private final @Nonnull Ip6 _srcIp;
  private final @Nullable Integer _srcPort;
}
