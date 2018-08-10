package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.State;

/** Parent class for questions that reason about the fate of flow. */
public abstract class IPacketTraceQuestion extends Question implements IQuestion {

  private static final String PROP_DSCP = "dscp";

  private static final String PROP_DST = "dst";

  private static final String PROP_DST_PORT = "dstPort";

  private static final String PROP_DST_PROTOCOL = "dstProtocol";

  private static final String PROP_ECN = "ecn";

  private static final String PROP_FRAGMENT_OFFSET = "fragmentOffset";

  private static final String PROP_ICMP_CODE = "icmpCode";

  private static final String PROP_ICMP_TYPE = "icmpType";

  private static final String PROP_INGRESS_INTERFACE = "ingressInterface";

  private static final String PROP_IP_PROTOCOL = "ipProtocol";

  private static final String PROP_PACKET_LENGTH = "packetLength";

  private static final String PROP_SRC_IP = "srcIp";

  private static final String PROP_SRC_PORT = "srcPort";

  private static final String PROP_SRC_PROTOCOL = "srcProtocol";

  private static final String PROP_STATE = "state";

  private static final String PROP_TCP_FLAGS_ACK = "tcpAck";

  private static final String PROP_TCP_FLAGS_CWR = "tcpCwr";

  private static final String PROP_TCP_FLAGS_ECE = "tcpEce";

  private static final String PROP_TCP_FLAGS_FIN = "tcpFin";

  private static final String PROP_TCP_FLAGS_PSH = "tcpPsh";

  private static final String PROP_TCP_FLAGS_RST = "tcpRst";

  private static final String PROP_TCP_FLAGS_SYN = "tcpSyn";

  private static final String PROP_TCP_FLAGS_URG = "tcpUrg";

  private Integer _dscp;

  private String _dst;

  private Integer _dstPort;

  private Protocol _dstProtocol;

  private Integer _ecn;

  private Integer _fragmentOffset;

  private Integer _icmpCode;

  private Integer _icmpType;

  private String _ingressInterface;

  private IpProtocol _ipProtocol;

  private Integer _packetLength;

  private Ip _srcIp;

  private Integer _srcPort;

  private Protocol _srcProtocol;

  private State _state;

  private Boolean _tcpFlagsAck;

  private Boolean _tcpFlagsCwr;

  private Boolean _tcpFlagsEce;

  private Boolean _tcpFlagsFin;

  private Boolean _tcpFlagsPsh;

  private Boolean _tcpFlagsRst;

  private Boolean _tcpFlagsSyn;

  private Boolean _tcpFlagsUrg;

  /**
   * Creates a {@link Flow.Builder} object with the flow parameters
   *
   * @return The created builder object
   */
  public Flow.Builder createBaseFlowBuilder() {
    Flow.Builder flowBuilder = new Flow.Builder();
    if (_dscp != null) {
      flowBuilder.setDscp(_dscp);
    }
    if (_dst != null) {
      try {
        Ip dstIp = new Ip(_dst);
        flowBuilder.setDstIp(dstIp);
      } catch (IllegalArgumentException e) {
        flowBuilder.setDstIp(Ip.AUTO); // use auto we couldn't parse a valid IP
      }
    }
    if (_dstPort != null) {
      flowBuilder.setDstPort(_dstPort);
    }
    if (_ecn != null) {
      flowBuilder.setEcn(_ecn);
    }
    if (_fragmentOffset != null) {
      flowBuilder.setFragmentOffset(_fragmentOffset);
    }
    if (_icmpCode != null) {
      flowBuilder.setIcmpCode(_icmpCode);
    }
    if (_icmpType != null) {
      flowBuilder.setIcmpType(_icmpType);
    }
    if (_ingressInterface != null) {
      flowBuilder.setIngressInterface(_ingressInterface);
    }
    if (_ipProtocol != null) {
      flowBuilder.setIpProtocol(_ipProtocol);
    }
    if (_packetLength != null) {
      flowBuilder.setPacketLength(_packetLength);
    }
    if (_srcIp != null) {
      flowBuilder.setSrcIp(_srcIp);
    } else {
      flowBuilder.setSrcIp(Ip.AUTO);
    }
    if (_srcPort != null) {
      flowBuilder.setSrcPort(_srcPort);
    }
    if (_state != null) {
      flowBuilder.setState(_state);
    }
    if (_tcpFlagsAck != null) {
      flowBuilder.setTcpFlagsAck(_tcpFlagsAck ? 1 : 0);
    }
    if (_tcpFlagsCwr != null) {
      flowBuilder.setTcpFlagsCwr(_tcpFlagsCwr ? 1 : 0);
    }
    if (_tcpFlagsEce != null) {
      flowBuilder.setTcpFlagsEce(_tcpFlagsEce ? 1 : 0);
    }
    if (_tcpFlagsFin != null) {
      flowBuilder.setTcpFlagsFin(_tcpFlagsFin ? 1 : 0);
    }
    if (_tcpFlagsPsh != null) {
      flowBuilder.setTcpFlagsPsh(_tcpFlagsPsh ? 1 : 0);
    }
    if (_tcpFlagsRst != null) {
      flowBuilder.setTcpFlagsRst(_tcpFlagsRst ? 1 : 0);
    }
    if (_tcpFlagsSyn != null) {
      flowBuilder.setTcpFlagsSyn(_tcpFlagsSyn ? 1 : 0);
    }
    if (_tcpFlagsUrg != null) {
      flowBuilder.setTcpFlagsUrg(_tcpFlagsUrg ? 1 : 0);
    }
    // do not move src or dst protocol up
    if (_srcProtocol != null) {
      IpProtocol ipProtocol = _srcProtocol.getIpProtocol();
      flowBuilder.setIpProtocol(ipProtocol);
      Integer port = _srcProtocol.getPort();
      if (port != null) {
        flowBuilder.setSrcPort(port);
      }
    }
    if (_dstProtocol != null) {
      IpProtocol ipProtocol = _dstProtocol.getIpProtocol();
      flowBuilder.setIpProtocol(ipProtocol);
      Integer port = _dstProtocol.getPort();
      if (port != null) {
        flowBuilder.setDstPort(port);
      }
    }
    return flowBuilder;
  }

  /**
   * Generates a dst Ip from destination specification in the question
   *
   * @param configurations The set of configurations to use in the generation
   * @return The dst IP
   */
  public Ip createDstIpFromDst(Map<String, Configuration> configurations) {
    String hostname = getDst();
    Configuration node = Strings.isNullOrEmpty(hostname) ? null : configurations.get(hostname);
    if (node != null) {
      Ip canonicalIp = node.getCanonicalIp();
      if (canonicalIp != null) {
        return canonicalIp;
      } else {
        throw new BatfishException(
            "Cannot automatically assign destination ip to flow since no there are no ip "
                + "addresses assigned to any interface on destination node: '"
                + hostname
                + "'");
      }
    } else {
      throw new BatfishException("Destination is neither a valid node nor IP: '" + hostname + "'");
    }
  }

  @JsonProperty(PROP_DSCP)
  public Integer getDscp() {
    return _dscp;
  }

  @JsonProperty(PROP_DST)
  public String getDst() {
    return _dst;
  }

  @JsonProperty(PROP_DST_PORT)
  public Integer getDstPort() {
    return _dstPort;
  }

  @JsonProperty(PROP_DST_PROTOCOL)
  public Protocol getDstProtocol() {
    return _dstProtocol;
  }

  @JsonProperty(PROP_ECN)
  public Integer getEcn() {
    return _ecn;
  }

  @JsonProperty(PROP_FRAGMENT_OFFSET)
  public Integer getFragmentOffset() {
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

  @JsonProperty(PROP_IP_PROTOCOL)
  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @JsonProperty(PROP_PACKET_LENGTH)
  public Integer getPacketLength() {
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

  @JsonProperty(PROP_SRC_PROTOCOL)
  public Protocol getSrcProtocol() {
    return _srcProtocol;
  }

  @JsonProperty(PROP_STATE)
  public State getState() {
    return _state;
  }

  @JsonProperty(PROP_TCP_FLAGS_ACK)
  public Boolean getTcpFlagsAck() {
    return _tcpFlagsAck;
  }

  @JsonProperty(PROP_TCP_FLAGS_CWR)
  public Boolean getTcpFlagsCwr() {
    return _tcpFlagsCwr;
  }

  @JsonProperty(PROP_TCP_FLAGS_ECE)
  public Boolean getTcpFlagsEce() {
    return _tcpFlagsEce;
  }

  @JsonProperty(PROP_TCP_FLAGS_FIN)
  public Boolean getTcpFlagsFin() {
    return _tcpFlagsFin;
  }

  @JsonProperty(PROP_TCP_FLAGS_PSH)
  public Boolean getTcpFlagsPsh() {
    return _tcpFlagsPsh;
  }

  @JsonProperty(PROP_TCP_FLAGS_RST)
  public Boolean getTcpFlagsRst() {
    return _tcpFlagsRst;
  }

  @JsonProperty(PROP_TCP_FLAGS_SYN)
  public Boolean getTcpFlagsSyn() {
    return _tcpFlagsSyn;
  }

  @JsonProperty(PROP_TCP_FLAGS_URG)
  public Boolean getTcpFlagsUrg() {
    return _tcpFlagsUrg;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DSCP, _dscp)
        .add(PROP_DST, _dst)
        .add(PROP_DST_PORT, _dstPort)
        .add(PROP_DST_PROTOCOL, _dstProtocol)
        .add(PROP_ECN, _ecn)
        .add(PROP_ICMP_CODE, _icmpCode)
        .add(PROP_ICMP_TYPE, _icmpType)
        .add(PROP_INGRESS_INTERFACE, _ingressInterface)
        .add(PROP_IP_PROTOCOL, _ipProtocol)
        .add(PROP_PACKET_LENGTH, _packetLength)
        .add(PROP_SRC_IP, _srcIp)
        .add(PROP_SRC_PORT, _srcPort)
        .add(PROP_SRC_PROTOCOL, _srcProtocol)
        .add(PROP_STATE, _state)
        .add(PROP_TCP_FLAGS_ACK, _tcpFlagsAck)
        .add(PROP_TCP_FLAGS_CWR, _tcpFlagsCwr)
        .add(PROP_TCP_FLAGS_ECE, _tcpFlagsEce)
        .add(PROP_TCP_FLAGS_FIN, _tcpFlagsFin)
        .add(PROP_TCP_FLAGS_PSH, _tcpFlagsPsh)
        .add(PROP_TCP_FLAGS_RST, _tcpFlagsRst)
        .add(PROP_TCP_FLAGS_SYN, _tcpFlagsSyn)
        .add(PROP_TCP_FLAGS_URG, _tcpFlagsUrg)
        .toString();
  }

  @JsonProperty(PROP_DSCP)
  public void setDscp(Integer dscp) {
    _dscp = dscp;
  }

  @JsonProperty(PROP_DST)
  public void setDst(String dst) {
    _dst = dst;
  }

  @JsonProperty(PROP_DST_PORT)
  public void setDstPort(Integer dstPort) {
    _dstPort = dstPort;
  }

  @JsonProperty(PROP_DST_PROTOCOL)
  public void setDstProtocol(Protocol dstProtocol) {
    _dstProtocol = dstProtocol;
  }

  @JsonProperty(PROP_ECN)
  public void setEcn(Integer ecn) {
    _ecn = ecn;
  }

  @JsonProperty(PROP_FRAGMENT_OFFSET)
  public void setFragmentOffset(Integer fragmentOffset) {
    _fragmentOffset = fragmentOffset;
  }

  @JsonProperty(PROP_ICMP_CODE)
  public void setIcmpCode(Integer icmpCode) {
    _icmpCode = icmpCode;
  }

  @JsonProperty(PROP_ICMP_TYPE)
  public void setIcmpType(Integer icmpType) {
    _icmpType = icmpType;
  }

  @JsonProperty(PROP_INGRESS_INTERFACE)
  public void setIngressInterface(String ingressInterface) {
    _ingressInterface = ingressInterface;
  }

  @JsonProperty(PROP_IP_PROTOCOL)
  public void setIpProtocol(IpProtocol ipProtocol) {
    _ipProtocol = ipProtocol;
  }

  @JsonProperty(PROP_PACKET_LENGTH)
  public void setPacketLength(Integer packetLength) {
    _packetLength = packetLength;
  }

  @JsonProperty(PROP_SRC_IP)
  public void setSrcIp(Ip srcIp) {
    _srcIp = srcIp;
  }

  @JsonProperty(PROP_SRC_PORT)
  public void setSrcPort(Integer srcPort) {
    _srcPort = srcPort;
  }

  @JsonProperty(PROP_SRC_PROTOCOL)
  public void setSrcProtocol(Protocol srcProtocol) {
    _srcProtocol = srcProtocol;
  }

  @JsonProperty(PROP_STATE)
  public void setState(State state) {
    _state = state;
  }

  @JsonProperty(PROP_TCP_FLAGS_ACK)
  public void setTcpFlagsAck(Boolean tcpFlagsAck) {
    _tcpFlagsAck = tcpFlagsAck;
  }

  @JsonProperty(PROP_TCP_FLAGS_CWR)
  public void setTcpFlagsCwr(Boolean tcpFlagsCwr) {
    _tcpFlagsCwr = tcpFlagsCwr;
  }

  @JsonProperty(PROP_TCP_FLAGS_ECE)
  public void setTcpFlagsEce(Boolean tcpFlagsEce) {
    _tcpFlagsEce = tcpFlagsEce;
  }

  @JsonProperty(PROP_TCP_FLAGS_FIN)
  public void setTcpFlagsFin(Boolean tcpFlagsFin) {
    _tcpFlagsFin = tcpFlagsFin;
  }

  @JsonProperty(PROP_TCP_FLAGS_PSH)
  public void setTcpFlagsPsh(Boolean tcpFlagsPsh) {
    _tcpFlagsPsh = tcpFlagsPsh;
  }

  @JsonProperty(PROP_TCP_FLAGS_RST)
  public void setTcpFlagsRst(Boolean tcpFlagsRst) {
    _tcpFlagsRst = tcpFlagsRst;
  }

  @JsonProperty(PROP_TCP_FLAGS_SYN)
  public void setTcpFlagsSyn(Boolean tcpFlagsSyn) {
    _tcpFlagsSyn = tcpFlagsSyn;
  }

  @JsonProperty(PROP_TCP_FLAGS_URG)
  public void setTcpFlagsUrg(Boolean tcpFlagsUrg) {
    _tcpFlagsUrg = tcpFlagsUrg;
  }
}
