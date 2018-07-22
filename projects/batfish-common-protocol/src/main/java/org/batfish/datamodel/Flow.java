package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;

public final class Flow implements Comparable<Flow>, Serializable {
  public static class Builder {

    private int _dscp;

    private Ip _dstIp;

    private int _dstPort;

    private int _ecn;

    private int _fragmentOffset;

    private int _icmpCode;

    private int _icmpType;

    private @Nullable String _ingressInterface;

    // Nullable because no sensible default. User is required to specify.
    private @Nullable String _ingressNode;

    private @Nullable String _ingressVrf;

    private @Nonnull IpProtocol _ipProtocol;

    private int _packetLength;

    private @Nonnull Ip _srcIp;

    private int _srcPort;

    private @Nonnull State _state;

    // Nullable because no sensible default. User is required to specify.
    private @Nullable String _tag;

    private int _tcpFlagsAck;

    private int _tcpFlagsCwr;

    private int _tcpFlagsEce;

    private int _tcpFlagsFin;

    private int _tcpFlagsPsh;

    private int _tcpFlagsRst;

    private int _tcpFlagsSyn;

    private int _tcpFlagsUrg;

    public Builder() {
      _dscp = 0;
      _dstIp = Ip.ZERO;
      _dstPort = 0;
      _ecn = 0;
      _fragmentOffset = 0;
      _ipProtocol = IpProtocol.IP;
      _srcIp = Ip.ZERO;
      _srcPort = 0;
      _icmpType = IcmpType.UNSET;
      _icmpCode = IcmpCode.UNSET;
      _ingressVrf = Configuration.DEFAULT_VRF_NAME;
      _packetLength = 0;
      _state = State.NEW;
      _tcpFlagsCwr = 0;
      _tcpFlagsEce = 0;
      _tcpFlagsUrg = 0;
      _tcpFlagsAck = 0;
      _tcpFlagsPsh = 0;
      _tcpFlagsRst = 0;
      _tcpFlagsSyn = 0;
      _tcpFlagsFin = 0;
    }

    public Builder(Flow flow) {
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
      _state = flow._state;
      _tcpFlagsCwr = flow._tcpFlagsCwr;
      _tcpFlagsEce = flow._tcpFlagsEce;
      _tcpFlagsUrg = flow._tcpFlagsUrg;
      _tcpFlagsAck = flow._tcpFlagsAck;
      _tcpFlagsPsh = flow._tcpFlagsPsh;
      _tcpFlagsRst = flow._tcpFlagsRst;
      _tcpFlagsSyn = flow._tcpFlagsSyn;
      _tcpFlagsFin = flow._tcpFlagsFin;
      _tag = flow._tag;
    }

    public Flow build() {
      checkNotNull(_ingressNode, "Cannot build flow without at least specifying ingress node");
      checkNotNull(_tag, "Cannot build flow without specifying tag");
      return new Flow(
          _ingressNode,
          _ingressInterface,
          _ingressVrf,
          _srcIp,
          _dstIp,
          _srcPort,
          _dstPort,
          _ipProtocol,
          _dscp,
          _ecn,
          _fragmentOffset,
          _icmpType,
          _icmpCode,
          _packetLength,
          _state,
          _tcpFlagsCwr,
          _tcpFlagsEce,
          _tcpFlagsUrg,
          _tcpFlagsAck,
          _tcpFlagsPsh,
          _tcpFlagsRst,
          _tcpFlagsSyn,
          _tcpFlagsFin,
          _tag);
    }

    public int getDscp() {
      return _dscp;
    }

    public Ip getDstIp() {
      return _dstIp;
    }

    public int getDstPort() {
      return _dstPort;
    }

    public int getEcn() {
      return _ecn;
    }

    public int getIcmpCode() {
      return _icmpCode;
    }

    public int getIcmpType() {
      return _icmpType;
    }

    public String getIngressInterface() {
      return _ingressInterface;
    }

    public String getIngressNode() {
      return _ingressNode;
    }

    public String getIngressVrf() {
      return _ingressVrf;
    }

    public IpProtocol getIpProtocol() {
      return _ipProtocol;
    }

    public int getPacketLength() {
      return _packetLength;
    }

    public Ip getSrcIp() {
      return _srcIp;
    }

    public int getSrcPort() {
      return _srcPort;
    }

    public State getState() {
      return _state;
    }

    public String getTag() {
      return _tag;
    }

    public int getTcpFlagsAck() {
      return _tcpFlagsAck;
    }

    public int getTcpFlagsCwr() {
      return _tcpFlagsCwr;
    }

    public int getTcpFlagsEce() {
      return _tcpFlagsEce;
    }

    public int getTcpFlagsFin() {
      return _tcpFlagsFin;
    }

    public int getTcpFlagsPsh() {
      return _tcpFlagsPsh;
    }

    public int getTcpFlagsRst() {
      return _tcpFlagsRst;
    }

    public int getTcpFlagsSyn() {
      return _tcpFlagsSyn;
    }

    public int getTcpFlagsUrg() {
      return _tcpFlagsUrg;
    }

    public Builder setDscp(int dscp) {
      _dscp = dscp;
      return this;
    }

    public Builder setDstIp(Ip dstIp) {
      _dstIp = dstIp;
      return this;
    }

    public Builder setDstPort(int dstPort) {
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

    public Builder setIcmpCode(int icmpCode) {
      _icmpCode = icmpCode;
      return this;
    }

    public Builder setIcmpType(int icmpType) {
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

    public Builder setSrcPort(int srcPort) {
      _srcPort = srcPort;
      return this;
    }

    public Builder setSrcPort(Integer srcPort) {
      _srcPort = srcPort;
      return this;
    }

    public Builder setState(State state) {
      _state = state;
      return this;
    }

    public Builder setTag(String tag) {
      _tag = tag;
      return this;
    }

    public Builder setTcpFlagsAck(Integer tcpFlagsAck) {
      _tcpFlagsAck = tcpFlagsAck;
      return this;
    }

    public Builder setTcpFlagsCwr(Integer tcpFlagsCwr) {
      _tcpFlagsCwr = tcpFlagsCwr;
      return this;
    }

    public Builder setTcpFlagsEce(Integer tcpFlagsEce) {
      _tcpFlagsEce = tcpFlagsEce;
      return this;
    }

    public Builder setTcpFlagsFin(Integer tcpFlagsFin) {
      _tcpFlagsFin = tcpFlagsFin;
      return this;
    }

    public Builder setTcpFlagsPsh(Integer tcpFlagsPsh) {
      _tcpFlagsPsh = tcpFlagsPsh;
      return this;
    }

    public Builder setTcpFlagsRst(Integer tcpFlagsRst) {
      _tcpFlagsRst = tcpFlagsRst;
      return this;
    }

    public Builder setTcpFlagsSyn(Integer tcpFlagsSyn) {
      _tcpFlagsSyn = tcpFlagsSyn;
      return this;
    }

    public Builder setTcpFlagsUrg(Integer tcpFlagsUrg) {
      _tcpFlagsUrg = tcpFlagsUrg;
      return this;
    }
  }

  private static final String PROP_DSCP = "dscp";

  private static final String PROP_DST_IP = "dstIp";

  private static final String PROP_DST_PORT = "dstPort";

  private static final String PROP_ECN = "ecn";

  private static final String PROP_FRAGMENT_OFFSET = "fragmentOffset";

  private static final String PROP_ICMP_CODE = "icmpCode";

  private static final String PROP_ICMP_TYPE = "icmpVar";

  private static final String PROP_INGRESS_INTERFACE = "ingressInterface";

  private static final String PROP_INGRESS_NODE = "ingressNode";

  private static final String PROP_INGRESS_VRF = "ingressVrf";

  private static final String PROP_IP_PROTOCOL = "ipProtocol";

  private static final String PROP_PACKET_LENGTH = "packetLength";

  private static final String PROP_SRC_IP = "srcIp";

  private static final String PROP_SRC_PORT = "srcPort";

  private static final String PROP_STATE = "state";

  private static final String PROP_TAG = "tag";

  private static final String PROP_TCP_FLAGS_ACK = "tcpFlagsAck";

  private static final String PROP_TCP_FLAGS_CWR = "tcpFlagsCwr";

  private static final String PROP_TCP_FLAGS_ECE = "tcpFlagsEce";

  private static final String PROP_TCP_FLAGS_FIN = "tcpFlagsFin";

  private static final String PROP_TCP_FLAGS_PSH = "tcpFlagsPsh";

  private static final String PROP_TCP_FLAGS_RST = "tcpFlagsRst";

  private static final String PROP_TCP_FLAGS_SYN = "tcpFlagsSyn";

  private static final String PROP_TCP_FLAGS_URG = "tcpFlagsUrg";

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final int _dscp;

  private final @Nonnull Ip _dstIp;

  private final int _dstPort;

  private final int _ecn;

  private final int _fragmentOffset;

  private final int _icmpCode;

  private final int _icmpType;

  private final @Nullable String _ingressInterface;

  private final @Nonnull String _ingressNode;

  private final @Nullable String _ingressVrf;

  private final @Nonnull IpProtocol _ipProtocol;

  private final int _packetLength;

  private final @Nonnull Ip _srcIp;

  private final int _srcPort;

  private final @Nonnull State _state;

  private final @Nonnull String _tag;

  private final int _tcpFlagsAck;

  private final int _tcpFlagsCwr;

  private final int _tcpFlagsEce;

  private final int _tcpFlagsFin;

  private final int _tcpFlagsPsh;

  private final int _tcpFlagsRst;

  private final int _tcpFlagsSyn;

  private final int _tcpFlagsUrg;

  private Flow(
      @Nonnull String ingressNode,
      @Nullable String ingressInterface,
      @Nullable String ingressVrf,
      @Nonnull Ip srcIp,
      @Nonnull Ip dstIp,
      int srcPort,
      int dstPort,
      @Nonnull IpProtocol ipProtocol,
      int dscp,
      int ecn,
      int fragmentOffset,
      int icmpType,
      int icmpCode,
      int packetLength,
      @Nonnull State state,
      int tcpFlagsCwr,
      int tcpFlagsEce,
      int tcpFlagsUrg,
      int tcpFlagsAck,
      int tcpFlagsPsh,
      int tcpFlagsRst,
      int tcpFlagsSyn,
      int tcpFlagsFin,
      @Nonnull String tag) {
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
    _state = state;
    _tcpFlagsCwr = tcpFlagsCwr;
    _tcpFlagsEce = tcpFlagsEce;
    _tcpFlagsUrg = tcpFlagsUrg;
    _tcpFlagsAck = tcpFlagsAck;
    _tcpFlagsPsh = tcpFlagsPsh;
    _tcpFlagsRst = tcpFlagsRst;
    _tcpFlagsSyn = tcpFlagsSyn;
    _tcpFlagsFin = tcpFlagsFin;
    _tag = tag;
  }

  @JsonCreator
  private static Flow createFlow(
      @JsonProperty(PROP_INGRESS_NODE) String ingressNode,
      @JsonProperty(PROP_INGRESS_INTERFACE) String ingressInterface,
      @JsonProperty(PROP_INGRESS_VRF) String ingressVrf,
      @JsonProperty(PROP_SRC_IP) Ip srcIp,
      @JsonProperty(PROP_DST_IP) Ip dstIp,
      @JsonProperty(PROP_SRC_PORT) int srcPort,
      @JsonProperty(PROP_DST_PORT) int dstPort,
      @JsonProperty(PROP_IP_PROTOCOL) IpProtocol ipProtocol,
      @JsonProperty(PROP_DSCP) int dscp,
      @JsonProperty(PROP_ECN) int ecn,
      @JsonProperty(PROP_FRAGMENT_OFFSET) int fragmentOffset,
      @JsonProperty(PROP_ICMP_TYPE) int icmpType,
      @JsonProperty(PROP_ICMP_CODE) int icmpCode,
      @JsonProperty(PROP_PACKET_LENGTH) int packetLength,
      @JsonProperty(PROP_STATE) State state,
      @JsonProperty(PROP_TCP_FLAGS_CWR) int tcpFlagsCwr,
      @JsonProperty(PROP_TCP_FLAGS_ECE) int tcpFlagsEce,
      @JsonProperty(PROP_TCP_FLAGS_URG) int tcpFlagsUrg,
      @JsonProperty(PROP_TCP_FLAGS_ACK) int tcpFlagsAck,
      @JsonProperty(PROP_TCP_FLAGS_PSH) int tcpFlagsPsh,
      @JsonProperty(PROP_TCP_FLAGS_RST) int tcpFlagsRst,
      @JsonProperty(PROP_TCP_FLAGS_SYN) int tcpFlagsSyn,
      @JsonProperty(PROP_TCP_FLAGS_FIN) int tcpFlagsFin,
      @JsonProperty(PROP_TAG) String tag) {
    return new Flow(
        requireNonNull(ingressNode, PROP_INGRESS_NODE + " must not be null"),
        ingressInterface,
        ingressVrf,
        requireNonNull(srcIp, PROP_SRC_IP + " must not be null"),
        requireNonNull(dstIp, PROP_DST_IP + " must not be null"),
        srcPort,
        dstPort,
        requireNonNull(ipProtocol, PROP_IP_PROTOCOL + " must not be null"),
        dscp,
        ecn,
        fragmentOffset,
        icmpType,
        icmpCode,
        packetLength,
        requireNonNull(state, PROP_STATE + " must not be null"),
        tcpFlagsCwr,
        tcpFlagsEce,
        tcpFlagsUrg,
        tcpFlagsAck,
        tcpFlagsPsh,
        tcpFlagsRst,
        tcpFlagsSyn,
        tcpFlagsFin,
        requireNonNull(tag, PROP_TAG + " must not be null"));
  }

  @Override
  public int compareTo(Flow rhs) {
    int ret;
    ret = _ingressNode.compareTo(rhs._ingressNode);
    if (ret != 0) {
      return ret;
    }
    ret = CommonUtil.compareNullable(_ingressInterface, rhs._ingressInterface);
    if (ret != 0) {
      return ret;
    }
    ret = CommonUtil.compareNullable(_ingressVrf, rhs._ingressVrf);
    if (ret != 0) {
      return ret;
    }
    ret = _srcIp.compareTo(rhs._srcIp);
    if (ret != 0) {
      return ret;
    }
    ret = _dstIp.compareTo(rhs._dstIp);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_ipProtocol.number(), rhs._ipProtocol.number());
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_srcPort, rhs._srcPort);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_dstPort, rhs._dstPort);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_dscp, rhs._dscp);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_ecn, rhs._ecn);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_fragmentOffset, rhs._fragmentOffset);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_icmpType, rhs._icmpType);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_icmpCode, rhs._icmpCode);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_packetLength, rhs._packetLength);
    if (ret != 0) {
      return ret;
    }
    ret = _state.compareTo(rhs._state);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsCwr, rhs._tcpFlagsCwr);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsEce, rhs._tcpFlagsEce);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsUrg, rhs._tcpFlagsUrg);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsAck, rhs._tcpFlagsAck);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsPsh, rhs._tcpFlagsPsh);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsRst, rhs._tcpFlagsRst);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_tcpFlagsSyn, rhs._tcpFlagsSyn);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_tcpFlagsFin, rhs._tcpFlagsFin);
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
        && _dstPort == other._dstPort
        && _ecn == other._ecn
        && _fragmentOffset == other._fragmentOffset
        && _icmpCode == other._icmpCode
        && _icmpType == other._icmpType
        && _ingressNode.equals(other._ingressNode)
        && Objects.equals(_ingressInterface, other._ingressInterface)
        && Objects.equals(_ingressVrf, other._ingressVrf)
        && _ipProtocol.equals(other._ipProtocol)
        && _packetLength == other._packetLength
        && _srcIp.equals(other._srcIp)
        && _srcPort == other._srcPort
        && _state.equals(other._state)
        && _tag.equals(other._tag)
        && _tcpFlagsAck == other._tcpFlagsAck
        && _tcpFlagsCwr == other._tcpFlagsCwr
        && _tcpFlagsEce == other._tcpFlagsEce
        && _tcpFlagsFin == other._tcpFlagsFin
        && _tcpFlagsPsh == other._tcpFlagsPsh
        && _tcpFlagsRst == other._tcpFlagsRst
        && _tcpFlagsSyn == other._tcpFlagsSyn
        && _tcpFlagsUrg == other._tcpFlagsUrg;
  }

  @JsonProperty(PROP_DSCP)
  public int getDscp() {
    return _dscp;
  }

  @JsonProperty(PROP_DST_IP)
  public Ip getDstIp() {
    return _dstIp;
  }

  @JsonProperty(PROP_DST_PORT)
  public Integer getDstPort() {
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
  public Integer getIcmpCode() {
    return _icmpCode;
  }

  @JsonProperty(PROP_ICMP_TYPE)
  public Integer getIcmpType() {
    return _icmpType;
  }

  @JsonProperty(PROP_INGRESS_INTERFACE)
  public String getIngressInterface() {
    return _ingressInterface;
  }

  @JsonProperty(PROP_INGRESS_NODE)
  public String getIngressNode() {
    return _ingressNode;
  }

  @JsonProperty(PROP_INGRESS_VRF)
  public String getIngressVrf() {
    return _ingressVrf;
  }

  @JsonProperty(PROP_IP_PROTOCOL)
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty(PROP_PACKET_LENGTH)
  public int getPacketLength() {
    return _packetLength;
  }

  @JsonProperty(PROP_SRC_IP)
  public Ip getSrcIp() {
    return _srcIp;
  }

  @JsonProperty(PROP_SRC_PORT)
  public Integer getSrcPort() {
    return _srcPort;
  }

  @JsonProperty(PROP_STATE)
  public State getState() {
    return _state;
  }

  @JsonProperty(PROP_TAG)
  public String getTag() {
    return _tag;
  }

  @JsonProperty(PROP_TCP_FLAGS_ACK)
  public int getTcpFlagsAck() {
    return _tcpFlagsAck;
  }

  @JsonProperty(PROP_TCP_FLAGS_CWR)
  public int getTcpFlagsCwr() {
    return _tcpFlagsCwr;
  }

  @JsonProperty(PROP_TCP_FLAGS_ECE)
  public int getTcpFlagsEce() {
    return _tcpFlagsEce;
  }

  @JsonProperty(PROP_TCP_FLAGS_FIN)
  public int getTcpFlagsFin() {
    return _tcpFlagsFin;
  }

  @JsonProperty(PROP_TCP_FLAGS_PSH)
  public int getTcpFlagsPsh() {
    return _tcpFlagsPsh;
  }

  @JsonProperty(PROP_TCP_FLAGS_RST)
  public int getTcpFlagsRst() {
    return _tcpFlagsRst;
  }

  @JsonProperty(PROP_TCP_FLAGS_SYN)
  public int getTcpFlagsSyn() {
    return _tcpFlagsSyn;
  }

  @JsonProperty(PROP_TCP_FLAGS_URG)
  public int getTcpFlagsUrg() {
    return _tcpFlagsUrg;
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
        _srcPort,
        _state,
        _tag,
        _tcpFlagsAck,
        _tcpFlagsCwr,
        _tcpFlagsEce,
        _tcpFlagsFin,
        _tcpFlagsPsh,
        _tcpFlagsRst,
        _tcpFlagsSyn,
        _tcpFlagsUrg);
  }

  public String prettyPrint(String prefixString) {
    boolean icmp = _ipProtocol == IpProtocol.ICMP;
    boolean tcp = _ipProtocol == IpProtocol.TCP;
    boolean udp = _ipProtocol == IpProtocol.UDP;
    String srcPortStr = "";
    String dstPortStr = "";
    String icmpTypeStr = "";
    String icmpCodeStr = "";
    String tcpFlagsStr = "";
    if (tcp || udp) {
      srcPortStr = " sport:" + NamedPort.nameFromNumber(_srcPort);
      dstPortStr = " dport:" + NamedPort.nameFromNumber(_dstPort);
    }
    if (tcp) {
      tcpFlagsStr =
          String.format(
              " tcpFlags:%d%d%d%d%d%d%d%d",
              _tcpFlagsCwr,
              _tcpFlagsEce,
              _tcpFlagsUrg,
              _tcpFlagsAck,
              _tcpFlagsPsh,
              _tcpFlagsRst,
              _tcpFlagsSyn,
              _tcpFlagsFin);
    }
    if (icmp) {
      icmpCodeStr = " icmpCode:" + Integer.toString(_icmpCode);
      icmpTypeStr = " icmpType:" + Integer.toString(_icmpType);
    }
    String dscpStr = (_dscp != 0) ? " dscp:" + _dscp : "";
    String ecnStr = (_ecn != 0) ? " ecn:" + _ecn : "";

    return prefixString
        + "Flow: ingress:"
        + _ingressNode
        + (_ingressVrf != null ? " vrf:" + _ingressVrf : "")
        + (_ingressInterface != null ? " iface:" + _ingressInterface : "")
        + " "
        + _srcIp
        + "->"
        + _dstIp
        + " "
        + _ipProtocol
        + srcPortStr
        + dstPortStr
        + dscpStr
        + ecnStr
        + icmpTypeStr
        + icmpCodeStr
        + " packetLength:"
        + _packetLength
        + " state:"
        + _state
        + tcpFlagsStr;
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
      srcPortStr = " srcPort:" + NamedPort.nameFromNumber(_srcPort);
      dstPortStr = " dstPort:" + NamedPort.nameFromNumber(_dstPort);
    }
    if (tcp) {
      tcpFlagsStr =
          String.format(
              " tcpFlags:%d%d%d%d%d%d%d%d",
              _tcpFlagsCwr,
              _tcpFlagsEce,
              _tcpFlagsUrg,
              _tcpFlagsAck,
              _tcpFlagsPsh,
              _tcpFlagsRst,
              _tcpFlagsSyn,
              _tcpFlagsFin);
    }
    if (icmp) {
      icmpCodeStr = " icmpCode:" + Integer.toString(_icmpCode);
      icmpTypeStr = " icmpType:" + Integer.toString(_icmpType);
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
        + " state:"
        + _state
        + tcpFlagsStr
        + " tag:"
        + _tag
        + ">";
  }
}
