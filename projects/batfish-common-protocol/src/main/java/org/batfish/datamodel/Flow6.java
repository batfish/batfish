package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public final class Flow6 implements Comparable<Flow6> {
  private static final String PROP_DSCP = "dscp";
  private static final String PROP_DST_IP = "dstIp";
  private static final String PROP_DST_PORT = "dstPort";
  private static final String PROP_ECN = "ecn";
  private static final String PROP_FRAGMENT_OFFSET = "fragmentOffset";
  private static final String PROP_ICMP_CODE = "icmpCode";
  private static final String PROP_ICMP_TYPE = "icmpVar";
  private static final String PROP_INGRESS_NODE = "ingressNode";
  private static final String PROP_IP_PROTOCOL = "ipProtocol";
  private static final String PROP_SRC_IP = "srcIp";
  private static final String PROP_SRC_PORT = "srcPort";
  private static final String PROP_TAG = "tag";
  private static final String PROP_TCP_FLAGS_ACK = "tcpFlagsAck";
  private static final String PROP_TCP_FLAGS_CWR = "tcpFlagsCwr";
  private static final String PROP_TCP_FLAGS_ECE = "tcpFlagsEce";
  private static final String PROP_TCP_FLAGS_FIN = "tcpFlagsFin";
  private static final String PROP_TCP_FLAGS_PSH = "tcpFlagsPsh";
  private static final String PROP_TCP_FLAGS_RST = "tcpFlagsRst";
  private static final String PROP_TCP_FLAGS_SYN = "tcpFlagsSyn";
  private static final String PROP_TCP_FLAGS_URG = "tcpFlagsUrg";
  private static final String PROP_DEPRECATED_STATE = "state";

  private final int _dscp;

  private final Ip6 _dstIp;

  private final int _dstPort;

  private final int _ecn;

  private final int _fragmentOffset;

  private final int _icmpCode;

  private final int _icmpType;

  private final String _ingressNode;

  private final IpProtocol _ipProtocol;

  private final Ip6 _srcIp;

  private final int _srcPort;

  private final String _tag;

  private final int _tcpFlagsAck;

  private final int _tcpFlagsCwr;

  private final int _tcpFlagsEce;

  private final int _tcpFlagsFin;

  private final int _tcpFlagsPsh;

  private final int _tcpFlagsRst;

  private final int _tcpFlagsSyn;

  private final int _tcpFlagsUrg;

  @JsonCreator
  public Flow6(
      @JsonProperty(PROP_INGRESS_NODE) String ingressNode,
      @JsonProperty(PROP_SRC_IP) Ip6 srcIp,
      @JsonProperty(PROP_DST_IP) Ip6 dstIp,
      @JsonProperty(PROP_SRC_PORT) int srcPort,
      @JsonProperty(PROP_DST_PORT) int dstPort,
      @JsonProperty(PROP_IP_PROTOCOL) IpProtocol ipProtocol,
      @JsonProperty(PROP_DSCP) int dscp,
      @JsonProperty(PROP_ECN) int ecn,
      @JsonProperty(PROP_FRAGMENT_OFFSET) int fragmentOffset,
      @JsonProperty(PROP_ICMP_TYPE) int icmpType,
      @JsonProperty(PROP_ICMP_CODE) int icmpCode,
      @JsonProperty(PROP_TCP_FLAGS_CWR) int tcpFlagsCwr,
      @JsonProperty(PROP_TCP_FLAGS_ECE) int tcpFlagsEce,
      @JsonProperty(PROP_TCP_FLAGS_URG) int tcpFlagsUrg,
      @JsonProperty(PROP_TCP_FLAGS_ACK) int tcpFlagsAck,
      @JsonProperty(PROP_TCP_FLAGS_PSH) int tcpFlagsPsh,
      @JsonProperty(PROP_TCP_FLAGS_RST) int tcpFlagsRst,
      @JsonProperty(PROP_TCP_FLAGS_SYN) int tcpFlagsSyn,
      @JsonProperty(PROP_TCP_FLAGS_FIN) int tcpFlagsFin,
      @JsonProperty(PROP_TAG) String tag,
      @JsonProperty(PROP_DEPRECATED_STATE) Object ignored) {
    _ingressNode = ingressNode;
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

  @Override
  public int compareTo(Flow6 rhs) {
    int ret;
    ret = _ingressNode.compareTo(rhs._ingressNode);
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
    } else if (!(o instanceof Flow6)) {
      return false;
    }
    Flow6 other = (Flow6) o;
    if (_dscp != other._dscp) {
      return false;
    }
    if (!_dstIp.equals(other._dstIp)) {
      return false;
    }
    if (_dstPort != other._dstPort) {
      return false;
    }
    if (_ecn != other._ecn) {
      return false;
    }
    if (_fragmentOffset != other._fragmentOffset) {
      return false;
    }
    if (!_ingressNode.equals(other._ingressNode)) {
      return false;
    }
    if (_ipProtocol != other._ipProtocol) {
      return false;
    }
    if (!_srcIp.equals(other._srcIp)) {
      return false;
    }
    if (_srcPort != other._srcPort) {
      return false;
    }
    if (_icmpType != other._icmpType) {
      return false;
    }
    if (_icmpCode != other._icmpCode) {
      return false;
    }
    if (_tcpFlagsCwr != other._tcpFlagsCwr) {
      return false;
    }
    if (_tcpFlagsEce != other._tcpFlagsEce) {
      return false;
    }
    if (_tcpFlagsUrg != other._tcpFlagsUrg) {
      return false;
    }
    if (_tcpFlagsAck != other._tcpFlagsAck) {
      return false;
    }
    if (_tcpFlagsPsh != other._tcpFlagsPsh) {
      return false;
    }
    if (_tcpFlagsRst != other._tcpFlagsRst) {
      return false;
    }
    if (_tcpFlagsSyn != other._tcpFlagsSyn) {
      return false;
    }
    if (_tcpFlagsFin != other._tcpFlagsFin) {
      return false;
    }
    return _tag.equals(other._tag);
  }

  @JsonProperty(PROP_DSCP)
  public int getDscp() {
    return _dscp;
  }

  @JsonProperty(PROP_DST_IP)
  public Ip6 getDstIp() {
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

  @JsonProperty(PROP_INGRESS_NODE)
  public String getIngressNode() {
    return _ingressNode;
  }

  @JsonProperty(PROP_IP_PROTOCOL)
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty(PROP_SRC_IP)
  public Ip6 getSrcIp() {
    return _srcIp;
  }

  @JsonProperty(PROP_SRC_PORT)
  public Integer getSrcPort() {
    return _srcPort;
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
    final int prime = 31;
    int result = 1;
    result = prime * result + _dscp;
    result = prime * result + _dstIp.hashCode();
    result = prime * result + _dstPort;
    result = prime * result + _ecn;
    result = prime * result + _fragmentOffset;
    result = prime * result + _ingressNode.hashCode();
    result = prime * result + _ipProtocol.ordinal();
    result = prime * result + _srcIp.hashCode();
    result = prime * result + _srcPort;
    result = prime * result + _icmpType;
    result = prime * result + _icmpCode;
    result = prime * result + _tag.hashCode();
    result = prime * result + _tcpFlagsCwr;
    result = prime * result + _tcpFlagsEce;
    result = prime * result + _tcpFlagsUrg;
    result = prime * result + _tcpFlagsAck;
    result = prime * result + _tcpFlagsPsh;
    result = prime * result + _tcpFlagsRst;
    result = prime * result + _tcpFlagsSyn;
    result = prime * result + _tcpFlagsFin;
    return result;
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
      icmpCodeStr = " icmpCode:" + _icmpCode;
      icmpTypeStr = " icmpType:" + _icmpType;
    }
    return "Flow<ingressNode:"
        + _ingressNode
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
        + tcpFlagsStr
        + " tag:"
        + _tag
        + ">";
  }
}
