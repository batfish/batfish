package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

// <question_page_comment>

/**
 * Checks if IPSec VPNs are correctly configured.
 *
 * <p>Details coming on what it means to be correctly configured.
 *
 * @type IpsecVpnStatus multifile
 * @param nodeRegex NodesSpecifier expression to match the nodes. Default is '.*' (all nodes).
 * @param filterRegex FiltersSpecifier to match the filters. Default is '.*' (all filters).
 */
public class TraceFiltersQuestion extends Question {

  private static final String PROP_DSCP = "dscp";

  private static final String PROP_DST = "dst";

  private static final String PROP_DST_PORT = "dstPort";

  private static final String PROP_DST_PROTOCOL = "dstProtocol";

  private static final String PROP_ECN = "ecn";

  private static final String PROP_FILTER_REGEX = "filterRegex";

  private static final String PROP_ICMP_CODE = "icmpCode";

  private static final String PROP_ICMP_TYPE = "icmpType";

  private static final String PROP_INGRESS_INTERFACE = "ingressInterface";

  private static final String PROP_INGRESS_NODE = "ingressNode";

  private static final String PROP_INGRESS_VRF = "ingressVrf";

  private static final String PROP_IP_PROTOCOL = "ipProtocol";

  private static final String PROP_NODE_REGEX = "nodeRegex";

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

  @Nonnull private FiltersSpecifier _filterRegex;

  private Integer _icmpCode;

  private Integer _icmpType;

  private String _ingressInterface;

  private String _ingressNode;

  private String _ingressVrf;

  private IpProtocol _ipProtocol;

  @Nonnull private NodesSpecifier _nodeRegex;

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

  @JsonCreator
  public TraceFiltersQuestion(
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_FILTER_REGEX) FiltersSpecifier filterRegex) {
    _nodeRegex = nodeRegex == null ? NodesSpecifier.ALL : nodeRegex;
    _filterRegex = filterRegex == null ? FiltersSpecifier.ALL : filterRegex;
  }

  public Flow.Builder createFlowBuilder() {
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
    if (_icmpCode != null) {
      flowBuilder.setIcmpCode(_icmpCode);
    }
    if (_icmpType != null) {
      flowBuilder.setIcmpType(_icmpType);
    }
    if (_ingressNode != null) {
      flowBuilder.setIngressNode(_ingressNode);
    }
    if (_ingressInterface != null) {
      flowBuilder.setIngressInterface(_ingressInterface);
    }
    if (_ingressVrf != null) {
      flowBuilder.setIngressVrf(_ingressVrf);
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

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTER_REGEX)
  public FiltersSpecifier getFilterRegex() {
    return _filterRegex;
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
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

  @JsonIgnore
  public Set<Builder> getFlowBuilders() {
    return Collections.singleton(createFlowBuilder());
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

  @Override
  public String getName() {
    return "tracefilters";
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
}
