package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Flow implements Comparable<Flow>, Serializable {
  public static class Builder {
    private int _dscp;
    private Ip _dstIp;
    private @Nullable Integer _dstPort;
    private int _ecn;
    private int _fragmentOffset;
    private @Nullable Integer _icmpCode;
    private @Nullable Integer _icmpType;
    private @Nullable String _ingressInterface;
    // Nullable because no sensible default. User is required to specify.
    private @Nullable String _ingressNode;
    private @Nullable String _ingressVrf;
    private @Nullable IpProtocol _ipProtocol;
    private int _packetLength;
    private @Nonnull Ip _srcIp;
    private @Nullable Integer _srcPort;
    private boolean _tcpFlagsAck;
    private boolean _tcpFlagsCwr;
    private boolean _tcpFlagsEce;
    private boolean _tcpFlagsFin;
    private boolean _tcpFlagsPsh;
    private boolean _tcpFlagsRst;
    private boolean _tcpFlagsSyn;
    private boolean _tcpFlagsUrg;

    private Builder() {
      _dscp = 0;
      _dstIp = Ip.ZERO;
      _ecn = 0;
      _fragmentOffset = 0;
      _ipProtocol = null;
      _srcIp = Ip.ZERO;
      _ingressVrf = Configuration.DEFAULT_VRF_NAME;
      _packetLength = 64; // min-sized ICMP packet (which is more than min-sized TCP/UDP)
      // all tcp flags default to false
    }

    private Builder(Flow flow) {
      _ingressNode = flow._ingressNode;
      _ingressInterface = flow._ingressInterface;
      _ingressVrf = flow._ingressVrf;
      _srcIp = flow._srcIp;
      _dstIp = flow._dstIp;
      _srcPort = flow._srcPort;
      _dstPort = flow._dstPort;
      _ipProtocol = flow._ipProtocol;
      _dscp = flow._dscp;
      _ecn = flow._ecn;
      _fragmentOffset = flow._fragmentOffset;
      _icmpType = flow._icmpType;
      _icmpCode = flow._icmpCode;
      _packetLength = flow._packetLength;
      TcpFlags flags = firstNonNull(flow.getTcpFlags(), TcpFlags.FALSE);
      _tcpFlagsAck = flags.getAck();
      _tcpFlagsCwr = flags.getCwr();
      _tcpFlagsEce = flags.getEce();
      _tcpFlagsFin = flags.getFin();
      _tcpFlagsPsh = flags.getPsh();
      _tcpFlagsRst = flags.getRst();
      _tcpFlagsSyn = flags.getSyn();
      _tcpFlagsUrg = flags.getUrg();
    }

    public Flow build() {
      checkNotNull(_ingressNode, "Cannot build flow without at least specifying ingress node");
      if (_ipProtocol == IpProtocol.ICMP) {
        checkArgument(_icmpType != null, "ICMP packets must have an ICMP type");
        checkArgument(_icmpCode != null, "ICMP packets must have an ICMP code");
        checkArgument(_packetLength >= 64, "ICMP packets must be at least 64 bytes");
      } else if (_ipProtocol == IpProtocol.TCP) {
        checkArgument(_srcPort != null, "TCP packets must have a source port");
        checkArgument(_dstPort != null, "TCP packets must have a destination port");
        checkArgument(_packetLength >= 40, "TCP packets must be at least 40 bytes");
      } else if (_ipProtocol == IpProtocol.UDP) {
        checkArgument(_srcPort != null, "UDP packets must have a source port");
        checkArgument(_dstPort != null, "UDP packets must have a destination port");
        checkArgument(_packetLength >= 28, "UDP packets must be at least 28 bytes");
      }
      checkArgument(_packetLength >= 20, "IP packets must be at least 20 bytes");

      @Nullable Integer icmpCode = _icmpCode;
      @Nullable Integer icmpType = _icmpType;
      if (_ipProtocol != IpProtocol.ICMP) {
        icmpCode = null;
        icmpType = null;
      }

      @Nullable Integer srcPort = _srcPort;
      @Nullable Integer dstPort = _dstPort;
      if (!IpProtocol.IP_PROTOCOLS_WITH_PORTS.contains(_ipProtocol)) {
        srcPort = dstPort = null;
      }

      return new Flow(
          _ingressNode,
          _ingressInterface,
          _ingressVrf,
          _srcIp,
          _dstIp,
          srcPort,
          dstPort,
          firstNonNull(_ipProtocol, IpProtocol.fromNumber(0)),
          _dscp,
          _ecn,
          _fragmentOffset,
          icmpType,
          icmpCode,
          _packetLength,
          _ipProtocol == IpProtocol.TCP
              ? new TcpFlags(
                  _tcpFlagsAck,
                  _tcpFlagsCwr,
                  _tcpFlagsEce,
                  _tcpFlagsFin,
                  _tcpFlagsPsh,
                  _tcpFlagsRst,
                  _tcpFlagsSyn,
                  _tcpFlagsUrg)
              : null);
    }

    public int getDscp() {
      return _dscp;
    }

    public Ip getDstIp() {
      return _dstIp;
    }

    public @Nullable Integer getDstPort() {
      return _dstPort;
    }

    public int getEcn() {
      return _ecn;
    }

    public @Nullable Integer getIcmpCode() {
      return _icmpCode;
    }

    public @Nullable Integer getIcmpType() {
      return _icmpType;
    }

    public @Nullable String getIngressInterface() {
      return _ingressInterface;
    }

    public @Nullable String getIngressNode() {
      return _ingressNode;
    }

    public @Nullable String getIngressVrf() {
      return _ingressVrf;
    }

    public @Nullable IpProtocol getIpProtocol() {
      return _ipProtocol;
    }

    public int getPacketLength() {
      return _packetLength;
    }

    public Ip getSrcIp() {
      return _srcIp;
    }

    public @Nullable Integer getSrcPort() {
      return _srcPort;
    }

    public Builder setDscp(int dscp) {
      _dscp = dscp;
      return this;
    }

    public Builder setDstIp(Ip dstIp) {
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

    public Builder setIngressInterface(@Nullable String ingressInterface) {
      _ingressInterface = ingressInterface;

      // invariant: either ingressVrf or ingressInterface is always null.
      if (_ingressInterface != null) {
        _ingressVrf = null;
      }
      return this;
    }

    public Builder setIngressNode(@Nonnull String ingressNode) {
      _ingressNode = ingressNode;
      return this;
    }

    public Builder setIngressVrf(@Nullable String ingressVrf) {
      _ingressVrf = ingressVrf;

      // invariant: either ingressVrf or ingressInterface is always null.
      if (_ingressVrf != null) {
        _ingressInterface = null;
      }
      return this;
    }

    public Builder setIpProtocol(@Nonnull IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
      return this;
    }

    public Builder setPacketLength(Integer packetLength) {
      _packetLength = packetLength;
      return this;
    }

    public Builder setSrcIp(Ip srcIp) {
      _srcIp = srcIp;
      return this;
    }

    public Builder setSrcPort(@Nullable Integer srcPort) {
      _srcPort = srcPort;
      return this;
    }

    public Builder setTcpFlagsAck(boolean tcpFlagsAck) {
      _tcpFlagsAck = tcpFlagsAck;
      return this;
    }

    public Builder setTcpFlagsCwr(boolean tcpFlagsCwr) {
      _tcpFlagsCwr = tcpFlagsCwr;
      return this;
    }

    public Builder setTcpFlagsEce(boolean tcpFlagsEce) {
      _tcpFlagsEce = tcpFlagsEce;
      return this;
    }

    public Builder setTcpFlagsFin(boolean tcpFlagsFin) {
      _tcpFlagsFin = tcpFlagsFin;
      return this;
    }

    public Builder setTcpFlagsPsh(boolean tcpFlagsPsh) {
      _tcpFlagsPsh = tcpFlagsPsh;
      return this;
    }

    public Builder setTcpFlagsRst(boolean tcpFlagsRst) {
      _tcpFlagsRst = tcpFlagsRst;
      return this;
    }

    public Builder setTcpFlagsSyn(boolean tcpFlagsSyn) {
      _tcpFlagsSyn = tcpFlagsSyn;
      return this;
    }

    public Builder setTcpFlagsUrg(boolean tcpFlagsUrg) {
      _tcpFlagsUrg = tcpFlagsUrg;
      return this;
    }

    public Builder setTcpFlags(@Nullable TcpFlags tcpFlags) {
      if (tcpFlags != null) {
        _tcpFlagsAck = tcpFlags.getAck();
        _tcpFlagsCwr = tcpFlags.getCwr();
        _tcpFlagsEce = tcpFlags.getEce();
        _tcpFlagsFin = tcpFlags.getFin();
        _tcpFlagsPsh = tcpFlags.getPsh();
        _tcpFlagsRst = tcpFlags.getRst();
        _tcpFlagsSyn = tcpFlags.getSyn();
        _tcpFlagsUrg = tcpFlags.getUrg();
      }
      return this;
    }
  }

  private static final String PROP_DSCP = "dscp";
  static final String PROP_DST_IP = "dstIp";
  static final String PROP_DST_PORT = "dstPort";
  private static final String PROP_ECN = "ecn";
  private static final String PROP_FRAGMENT_OFFSET = "fragmentOffset";
  private static final String PROP_ICMP_CODE = "icmpCode";
  private static final String PROP_ICMP_TYPE = "icmpVar";
  private static final String PROP_INGRESS_INTERFACE = "ingressInterface";
  private static final String PROP_INGRESS_NODE = "ingressNode";
  private static final String PROP_INGRESS_VRF = "ingressVrf";
  private static final String PROP_IP_PROTOCOL = "ipProtocol";
  private static final String PROP_PACKET_LENGTH = "packetLength";
  static final String PROP_SRC_IP = "srcIp";
  static final String PROP_SRC_PORT = "srcPort";
  private static final String PROP_TCP_FLAGS_ACK = "tcpFlagsAck";
  private static final String PROP_TCP_FLAGS_CWR = "tcpFlagsCwr";
  private static final String PROP_TCP_FLAGS_ECE = "tcpFlagsEce";
  private static final String PROP_TCP_FLAGS_FIN = "tcpFlagsFin";
  private static final String PROP_TCP_FLAGS_PSH = "tcpFlagsPsh";
  private static final String PROP_TCP_FLAGS_RST = "tcpFlagsRst";
  private static final String PROP_TCP_FLAGS_SYN = "tcpFlagsSyn";
  private static final String PROP_TCP_FLAGS_URG = "tcpFlagsUrg";
  private static final String PROP_DEPRECATED_STATE = "state";
  private static final String PROP_DEPRECATED_TAG = "tag";

  public static Builder builder() {
    return new Builder();
  }

  private final int _dscp;
  private final @Nonnull Ip _dstIp;
  private final @Nullable Integer _dstPort;
  private final int _ecn;
  private final int _fragmentOffset;
  private final @Nullable Integer _icmpCode;
  private final @Nullable Integer _icmpType;
  private final @Nullable String _ingressInterface;
  private final @Nonnull String _ingressNode;
  private final @Nullable String _ingressVrf;
  private final @Nonnull IpProtocol _ipProtocol;
  private final int _packetLength;
  private final @Nonnull Ip _srcIp;
  private final @Nullable Integer _srcPort;
  private final @Nullable TcpFlags _tcpFlags;

  private Flow(
      @Nonnull String ingressNode,
      @Nullable String ingressInterface,
      @Nullable String ingressVrf,
      @Nonnull Ip srcIp,
      @Nonnull Ip dstIp,
      @Nullable Integer srcPort,
      @Nullable Integer dstPort,
      @Nonnull IpProtocol ipProtocol,
      int dscp,
      int ecn,
      int fragmentOffset,
      @Nullable Integer icmpType,
      @Nullable Integer icmpCode,
      int packetLength,
      @Nullable TcpFlags tcpFlags) {
    checkArgument(
        ingressInterface != null || ingressVrf != null,
        "Either ingressInterface or ingressVrf must not be null.");
    checkArgument(
        ingressInterface == null || ingressVrf == null,
        "Either ingressInterface or ingressVrf must be null.");
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
    _tcpFlags = tcpFlags;
    if (_ipProtocol == IpProtocol.ICMP) {
      checkArgument(_icmpType != null, "ICMP packets must have an ICMP type");
      checkArgument(_icmpCode != null, "ICMP packets must have an ICMP code");
    } else if (_ipProtocol == IpProtocol.TCP) {
      checkArgument(_srcPort != null, "TCP packets must have a source port");
      checkArgument(_dstPort != null, "TCP packets must have a destination port");
      checkArgument(_tcpFlags != null, "TCP packets must have tcp flags");
    } else if (_ipProtocol == IpProtocol.UDP) {
      checkArgument(_srcPort != null, "UDP packets must have a source port");
      checkArgument(_dstPort != null, "UDP packets must have a destination port");
    }
  }

  @JsonCreator
  private static Flow createFlow(
      @JsonProperty(PROP_INGRESS_NODE) String ingressNode,
      @JsonProperty(PROP_INGRESS_INTERFACE) String ingressInterface,
      @JsonProperty(PROP_INGRESS_VRF) String ingressVrf,
      @JsonProperty(PROP_SRC_IP) Ip srcIp,
      @JsonProperty(PROP_DST_IP) Ip dstIp,
      @JsonProperty(PROP_SRC_PORT) @Nullable Integer srcPort,
      @JsonProperty(PROP_DST_PORT) @Nullable Integer dstPort,
      @JsonProperty(PROP_IP_PROTOCOL) IpProtocol ipProtocol,
      @JsonProperty(PROP_DSCP) int dscp,
      @JsonProperty(PROP_ECN) int ecn,
      @JsonProperty(PROP_FRAGMENT_OFFSET) int fragmentOffset,
      @JsonProperty(PROP_ICMP_TYPE) @Nullable Integer icmpType,
      @JsonProperty(PROP_ICMP_CODE) @Nullable Integer icmpCode,
      @JsonProperty(PROP_PACKET_LENGTH) int packetLength,
      @JsonProperty(PROP_TCP_FLAGS_CWR) @Nullable Integer tcpFlagsCwr,
      @JsonProperty(PROP_TCP_FLAGS_ECE) @Nullable Integer tcpFlagsEce,
      @JsonProperty(PROP_TCP_FLAGS_URG) @Nullable Integer tcpFlagsUrg,
      @JsonProperty(PROP_TCP_FLAGS_ACK) @Nullable Integer tcpFlagsAck,
      @JsonProperty(PROP_TCP_FLAGS_PSH) @Nullable Integer tcpFlagsPsh,
      @JsonProperty(PROP_TCP_FLAGS_RST) @Nullable Integer tcpFlagsRst,
      @JsonProperty(PROP_TCP_FLAGS_SYN) @Nullable Integer tcpFlagsSyn,
      @JsonProperty(PROP_TCP_FLAGS_FIN) @Nullable Integer tcpFlagsFin,
      @JsonProperty(PROP_DEPRECATED_TAG) String ignoredTag,
      @JsonProperty(PROP_DEPRECATED_STATE) Object ignored) {
    checkArgument(ipProtocol != null, PROP_IP_PROTOCOL + " must not be null");
    TcpFlags tcpFlags = null;
    if (ipProtocol == IpProtocol.TCP) {
      tcpFlags =
          new TcpFlags(
              firstNonNull(tcpFlagsAck, 0) != 0,
              firstNonNull(tcpFlagsCwr, 0) != 0,
              firstNonNull(tcpFlagsEce, 0) != 0,
              firstNonNull(tcpFlagsFin, 0) != 0,
              firstNonNull(tcpFlagsPsh, 0) != 0,
              firstNonNull(tcpFlagsRst, 0) != 0,
              firstNonNull(tcpFlagsSyn, 0) != 0,
              firstNonNull(tcpFlagsUrg, 0) != 0);
    }
    return new Flow(
        requireNonNull(ingressNode, PROP_INGRESS_NODE + " must not be null"),
        ingressInterface,
        ingressVrf,
        requireNonNull(srcIp, PROP_SRC_IP + " must not be null"),
        requireNonNull(dstIp, PROP_DST_IP + " must not be null"),
        srcPort,
        dstPort,
        ipProtocol,
        dscp,
        ecn,
        fragmentOffset,
        icmpType,
        icmpCode,
        packetLength,
        tcpFlags);
  }

  @Override
  public int compareTo(@Nonnull Flow rhs) {
    return Comparator.comparing(Flow::getIngressNode)
        .thenComparing(Flow::getIngressInterface, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Flow::getIngressVrf, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Flow::getSrcIp)
        .thenComparing(Flow::getDstIp)
        .thenComparing((flow) -> flow.getIpProtocol().number())
        .thenComparing(Flow::getSrcPort, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Flow::getDstPort, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Flow::getDscp)
        .thenComparing(Flow::getEcn)
        .thenComparing(Flow::getFragmentOffset)
        .thenComparing(Flow::getIcmpType, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(Flow::getIcmpCode, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(Flow::getPacketLength)
        .thenComparing(Flow::getTcpFlags, Comparator.nullsLast(Comparator.naturalOrder()))
        .compare(this, rhs);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Flow)) {
      return false;
    }
    Flow other = (Flow) o;
    return _dscp == other._dscp
        && _dstIp.equals(other._dstIp)
        && Objects.equals(_dstPort, other._dstPort)
        && _ecn == other._ecn
        && _fragmentOffset == other._fragmentOffset
        && Objects.equals(_icmpCode, other._icmpCode)
        && Objects.equals(_icmpType, other._icmpType)
        && _ingressNode.equals(other._ingressNode)
        && Objects.equals(_ingressInterface, other._ingressInterface)
        && Objects.equals(_ingressVrf, other._ingressVrf)
        && _ipProtocol == other._ipProtocol
        && _packetLength == other._packetLength
        && _srcIp.equals(other._srcIp)
        && Objects.equals(_srcPort, other._srcPort)
        && Objects.equals(_tcpFlags, other._tcpFlags);
  }

  @JsonProperty(PROP_DSCP)
  public int getDscp() {
    return _dscp;
  }

  @JsonProperty(PROP_DST_IP)
  public @Nonnull Ip getDstIp() {
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
  public @Nullable String getIngressVrf() {
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
  public @Nonnull Ip getSrcIp() {
    return _srcIp;
  }

  @JsonProperty(PROP_SRC_PORT)
  public @Nullable Integer getSrcPort() {
    return _srcPort;
  }

  /** Jackson use only. */
  @JsonProperty(PROP_DEPRECATED_TAG)
  @Deprecated
  private @Nonnull String getTag() {
    return "tag"; // For backwards compatibility. Older clients expect a tag
  }

  @JsonProperty(PROP_DEPRECATED_STATE)
  @Deprecated
  private @Nonnull String getState() {
    return "NEW";
  }

  // Again, backwards-compatibility tcp-flag ugliness below
  @JsonProperty(PROP_TCP_FLAGS_ACK)
  private int getJsonTcpFlagsAck() {
    return _tcpFlags != null && _tcpFlags.getAck() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_CWR)
  private int getJsonTcpFlagsCwr() {
    return _tcpFlags != null && _tcpFlags.getCwr() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_ECE)
  private int getJsonTcpFlagsEce() {
    return _tcpFlags != null && _tcpFlags.getEce() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_FIN)
  private int getJsonTcpFlagsFin() {
    return _tcpFlags != null && _tcpFlags.getFin() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_PSH)
  private int getJsonTcpFlagsPsh() {
    return _tcpFlags != null && _tcpFlags.getPsh() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_RST)
  private int getJsonTcpFlagsRst() {
    return _tcpFlags != null && _tcpFlags.getRst() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_SYN)
  private int getJsonTcpFlagsSyn() {
    return _tcpFlags != null && _tcpFlags.getSyn() ? 1 : 0;
  }

  @JsonProperty(PROP_TCP_FLAGS_URG)
  private int getJsonTcpFlagsUrg() {
    return _tcpFlags != null && _tcpFlags.getUrg() ? 1 : 0;
  }

  @JsonIgnore
  public @Nullable TcpFlags getTcpFlags() {
    return _tcpFlags;
  }

  @Override
  public int hashCode() {
    int hash = _hashCode;
    if (hash == 0) {
      hash =
          Objects.hash(
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
              _ipProtocol.ordinal(),
              _packetLength,
              _srcIp,
              _srcPort,
              _tcpFlags);
      _hashCode = hash;
    }
    return hash;
  }

  private transient int _hashCode;

  private String tcpFlagsStr() {
    return String.format(
        " tcpFlags:%d%d%d%d%d%d%d%d",
        // Ordered by wire order, not alphabetical / logical order. LSB on the right.
        getJsonTcpFlagsCwr(),
        getJsonTcpFlagsEce(),
        getJsonTcpFlagsUrg(),
        getJsonTcpFlagsAck(),
        getJsonTcpFlagsPsh(),
        getJsonTcpFlagsRst(),
        getJsonTcpFlagsSyn(),
        getJsonTcpFlagsFin());
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public String toString() {
    boolean icmp = _ipProtocol == IpProtocol.ICMP;
    boolean tcp = _ipProtocol == IpProtocol.TCP;
    boolean udp = _ipProtocol == IpProtocol.UDP;
    String srcPortStr = "";
    String dstPortStr = "";
    String icmpTypeStr = "";
    String icmpCodeStr = "";
    String tcpFlagsStr = "";
    if (tcp || udp) {
      assert _srcPort != null;
      assert _dstPort != null;
      srcPortStr = " srcPort:" + NamedPort.nameFromNumber(_srcPort);
      dstPortStr = " dstPort:" + NamedPort.nameFromNumber(_dstPort);
    }
    if (tcp) {
      tcpFlagsStr = tcpFlagsStr();
    }
    if (icmp) {
      icmpCodeStr = " icmpCode:" + _icmpCode;
      icmpTypeStr = " icmpType:" + _icmpType;
    }
    return "Flow<ingressNode:"
        + _ingressNode
        + (_ingressVrf != null ? " ingressVrf:" + _ingressVrf : "")
        + (_ingressInterface != null ? " iface:" + _ingressInterface : "")
        + " srcIp:"
        + _srcIp
        + " dstIp:"
        + _dstIp
        + " ipProtocol:"
        + _ipProtocol
        + srcPortStr
        + dstPortStr
        + " dscp: "
        + _dscp
        + " ecn:"
        + _ecn
        + " fragmentOffset:"
        + _fragmentOffset
        + icmpTypeStr
        + icmpCodeStr
        + " packetLength:"
        + _packetLength
        + tcpFlagsStr
        + ">";
  }
}
