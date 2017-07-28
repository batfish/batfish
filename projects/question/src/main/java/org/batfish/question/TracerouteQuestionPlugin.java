package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.ITracerouteQuestion;
import org.batfish.datamodel.questions.Question;

public class TracerouteQuestionPlugin extends QuestionPlugin {

  public static class TracerouteAnswerer extends Answerer {

    public TracerouteAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      String tag = _batfish.getFlowTag();
      Set<Flow> flows = getFlows(tag);
      _batfish.processFlows(flows);
      AnswerElement answerElement = _batfish.getHistory();
      return answerElement;
    }

    @Override
    public AnswerElement answerDiff() {
      String tag = _batfish.getDifferentialFlowTag();
      Set<Flow> flows = getFlows(tag);
      _batfish.pushBaseEnvironment();
      _batfish.processFlows(flows);
      _batfish.popEnvironment();
      _batfish.pushDeltaEnvironment();
      _batfish.processFlows(flows);
      _batfish.popEnvironment();
      FlowHistory history = _batfish.getHistory();
      FlowHistory filteredHistory = new FlowHistory();
      for (String flowText : history.getFlowsByText().keySet()) {
        // String baseEnvId = _batfish.getBaseTestrigSettings().getName() +
        // ":"
        // + _batfish.getBaseTestrigSettings().getEnvironmentSettings()
        // .getName();
        _batfish.pushBaseEnvironment();
        String baseEnvId = _batfish.getFlowTag();
        _batfish.popEnvironment();
        // String deltaEnvId = _batfish.getDeltaTestrigSettings().getName()
        // +
        // ":"
        // + _batfish.getDeltaTestrigSettings().getEnvironmentSettings()
        // .getName();
        _batfish.pushDeltaEnvironment();
        String deltaEnvId = _batfish.getFlowTag();
        _batfish.popEnvironment();
        Set<FlowTrace> baseFlowTraces = history.getTraces().get(flowText).get(baseEnvId);
        Set<FlowTrace> deltaFlowTraces = history.getTraces().get(flowText).get(deltaEnvId);
        if (!baseFlowTraces.toString().equals(deltaFlowTraces.toString())) {
          Flow flow = history.getFlowsByText().get(flowText);
          for (FlowTrace flowTrace : baseFlowTraces) {
            filteredHistory.addFlowTrace(flow, baseEnvId, flowTrace);
          }
          for (FlowTrace flowTrace : deltaFlowTraces) {
            filteredHistory.addFlowTrace(flow, deltaEnvId, flowTrace);
          }
        }
      }
      return filteredHistory;
    }

    private Set<Flow> getFlows(String tag) {
      Set<Flow> flows = new TreeSet<>();
      TracerouteQuestion question = (TracerouteQuestion) _question;
      Set<Flow.Builder> flowBuilders = question.getFlowBuilders();
      Map<String, Configuration> configurations = null;
      for (Flow.Builder flowBuilder : flowBuilders) {
        // TODO: better automatic source ip, considering VRFs and routing
        if (flowBuilder.getSrcIp().equals(Ip.AUTO)) {
          if (configurations == null) {
            _batfish.pushBaseEnvironment();
            configurations = _batfish.loadConfigurations();
            _batfish.popEnvironment();
          }
          String hostname = flowBuilder.getIngressNode();
          Configuration node = (hostname == null) ? null : configurations.get(hostname);
          if (node != null) {
            Set<Ip> ips =
                new TreeSet<>(
                    node.getVrfs()
                        .values()
                        .stream()
                        .flatMap(v -> v.getInterfaces().values().stream())
                        .flatMap(i -> i.getAllPrefixes().stream())
                        .map(prefix -> prefix.getAddress())
                        .collect(Collectors.toSet()));
            if (!ips.isEmpty()) {
              Ip lowestIp = ips.toArray(new Ip[] {})[0];
              flowBuilder.setSrcIp(lowestIp);
            } else {
              throw new BatfishException(
                  "Cannot automatically assign source ip to flow since no there are no ip "
                      + "addresses assigned to any interface on ingress node: '"
                      + hostname
                      + "'");
            }
          } else {
            throw new BatfishException(
                "Cannot create flow with non-existent ingress node: '" + hostname + "'");
          }
        }
        flowBuilder.setTag(tag);
        Flow flow = flowBuilder.build();
        flows.add(flow);
      }
      return flows;
    }
  }

  // <question_page_comment>

  /**
   * Perform a traceroute.
   *
   * <p>This question performs a virtual traceroute in the network from a starting node. The
   * destination IP is randomly picked if not explicitly specified. Other IP headers are also
   * randomly picked if unspecified, with a bias toward generating packets similar to a real
   * traceroute (see below).
   *
   * <p>Unlike a real traceroute, this traceroute is directional. That is, for it to succeed, the
   * reverse connectivity is not needed. This feature can help debug connectivity issues by
   * decoupling the two directions.
   *
   * @type Traceroute dataplane
   * @param ingressNode Name of the node where the traceroute should be done from. This parameter is
   *     mandatory and has no default value.
   * @param ingressVrf Name of the VRF to use on the ingress node. If unspecified, uses the default
   *     VRF.
   * @param dscp Details coming
   * @param dstIp Destination IP for the traceroute. The default is to pick one randomly.
   * @param dstPort Destination port for the traceroute. The default is Details coming.
   * @param ecn Details coming
   * @param icmpCode Details coming
   * @param icmpType Details coming
   * @param ipProtocol Details coming
   * @param srcIp Details coming
   * @param srcPort Details coming
   * @param stateVar Details coming
   * @param tcpAck Details coming
   * @param tcpAck Details coming
   * @param tcpAck Details coming
   * @param tcpCwr Details coming
   * @param tcpEce Details coming
   * @param tcpFin Details coming
   * @param tcpPsh Details coming
   * @param tcpRst Details coming
   * @param tcpSyn Details coming
   * @param tcpUrg Details coming
   * @example bf_answer("Traceroute", ingressNode="as2border1", dstIp="2.128.0.101", dstPort=53,
   *     ipProtocol="UDP") Show the path of a DNS packet (UDP to port 53) from as2border1
   */
  public static class TracerouteQuestion extends Question implements ITracerouteQuestion {

    private static final String DSCP_VAR = "dscp";

    private static final String DST_IP_VAR = "dstIp";

    private static final String DST_PORT_VAR = "dstPort";

    private static final String DST_PROTOCOL_VAR = "dstProtocol";

    private static final String ECN_VAR = "ecn";

    private static final String ICMP_CODE_VAR = "icmpCode";

    private static final String ICMP_TYPE_VAR = "icmpType";

    private static final String INGRESS_INTERFACE_VAR = "ingressInterface";

    private static final String INGRESS_NODE_VAR = "ingressNode";

    private static final String INGRESS_VRF_VAR = "ingressVrf";

    private static final String IP_PROTOCOL_VAR = "ipProtocol";

    private static final String PACKET_LENGTH_VAR = "packetLength";

    private static final String SRC_IP_VAR = "srcIp";

    private static final String SRC_PORT_VAR = "srcPort";

    private static final String SRC_PROTOCOL_VAR = "srcProtocol";

    private static final String STATE_VAR = "state";

    private static final String TCP_FLAGS_ACK_VAR = "tcpAck";

    private static final String TCP_FLAGS_CWR_VAR = "tcpCwr";

    private static final String TCP_FLAGS_ECE_VAR = "tcpEce";

    private static final String TCP_FLAGS_FIN_VAR = "tcpFin";

    private static final String TCP_FLAGS_PSH_VAR = "tcpPsh";

    private static final String TCP_FLAGS_RST_VAR = "tcpRst";

    private static final String TCP_FLAGS_SYN_VAR = "tcpSyn";

    private static final String TCP_FLAGS_URG_VAR = "tcpUrg";

    private Integer _dscp;

    private Ip _dstIp;

    private Integer _dstPort;

    private Protocol _dstProtocol;

    private Integer _ecn;

    private Integer _icmpCode;

    private Integer _icmpType;

    private String _ingressInterface;

    private String _ingressNode;

    private String _ingressVrf;

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

    public TracerouteQuestion() {}

    public Flow.Builder createFlowBuilder() {
      Flow.Builder flowBuilder = new Flow.Builder();
      if (_dscp != null) {
        flowBuilder.setDscp(_dscp);
      }
      if (_dstIp != null) {
        flowBuilder.setDstIp(_dstIp);
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
      return true;
    }

    @JsonProperty(DSCP_VAR)
    public Integer getDscp() {
      return _dscp;
    }

    @JsonProperty(DST_IP_VAR)
    public Ip getDstIp() {
      return _dstIp;
    }

    @JsonProperty(DST_PORT_VAR)
    public Integer getDstPort() {
      return _dstPort;
    }

    @JsonProperty(DST_PROTOCOL_VAR)
    public Protocol getDstProtocol() {
      return _dstProtocol;
    }

    @JsonProperty(ECN_VAR)
    public Integer getEcn() {
      return _ecn;
    }

    @JsonIgnore
    public Set<Flow.Builder> getFlowBuilders() {
      return Collections.singleton(createFlowBuilder());
    }

    @JsonProperty(ICMP_CODE_VAR)
    public Integer getIcmpCode() {
      return _icmpCode;
    }

    @JsonProperty(ICMP_TYPE_VAR)
    public Integer getIcmpType() {
      return _icmpType;
    }

    @JsonProperty(INGRESS_INTERFACE_VAR)
    public String getIngressInterface() {
      return _ingressInterface;
    }

    @JsonProperty(INGRESS_NODE_VAR)
    public String getIngressNode() {
      return _ingressNode;
    }

    @JsonProperty(INGRESS_VRF_VAR)
    public String getIngressVrf() {
      return _ingressVrf;
    }

    @JsonProperty(IP_PROTOCOL_VAR)
    public IpProtocol getIpProtocol() {
      return _ipProtocol;
    }

    @Override
    public String getName() {
      return NAME;
    }

    @JsonProperty(PACKET_LENGTH_VAR)
    public Integer getPacketLength() {
      return _packetLength;
    }

    @JsonProperty(SRC_IP_VAR)
    public Ip getSrcIp() {
      return _srcIp;
    }

    @JsonProperty(SRC_PORT_VAR)
    public Integer getSrcPort() {
      return _srcPort;
    }

    @JsonProperty(SRC_PROTOCOL_VAR)
    public Protocol getSrcProtocol() {
      return _srcProtocol;
    }

    @JsonProperty(STATE_VAR)
    public State getState() {
      return _state;
    }

    @JsonProperty(TCP_FLAGS_ACK_VAR)
    public Boolean getTcpFlagsAck() {
      return _tcpFlagsAck;
    }

    @JsonProperty(TCP_FLAGS_CWR_VAR)
    public Boolean getTcpFlagsCwr() {
      return _tcpFlagsCwr;
    }

    @JsonProperty(TCP_FLAGS_ECE_VAR)
    public Boolean getTcpFlagsEce() {
      return _tcpFlagsEce;
    }

    @JsonProperty(TCP_FLAGS_FIN_VAR)
    public Boolean getTcpFlagsFin() {
      return _tcpFlagsFin;
    }

    @JsonProperty(TCP_FLAGS_PSH_VAR)
    public Boolean getTcpFlagsPsh() {
      return _tcpFlagsPsh;
    }

    @JsonProperty(TCP_FLAGS_RST_VAR)
    public Boolean getTcpFlagsRst() {
      return _tcpFlagsRst;
    }

    @JsonProperty(TCP_FLAGS_SYN_VAR)
    public Boolean getTcpFlagsSyn() {
      return _tcpFlagsSyn;
    }

    @JsonProperty(TCP_FLAGS_URG_VAR)
    public Boolean getTcpFlagsUrg() {
      return _tcpFlagsUrg;
    }

    @Override
    public boolean getTraffic() {
      return true;
    }

    @Override
    public String prettyPrint() {
      try {
        String retString =
            String.format("traceroute %singressNode=%s", prettyPrintBase(), _ingressNode);
        // we only print "interesting" values
        if (_ingressInterface != null) {
          retString += String.format(" | %s=%s", INGRESS_INTERFACE_VAR, _ingressInterface);
        }
        if (_ingressVrf != null) {
          retString += String.format(" | %s=%s", INGRESS_VRF_VAR, _ingressVrf);
        }
        if (_dscp != null) {
          retString += String.format(" | %s=%s", DSCP_VAR, _dscp);
        }
        if (_dstIp != null) {
          retString += String.format(" | %s=%s", DST_IP_VAR, _dstIp);
        }
        if (_dstPort != null) {
          retString += String.format(" | %S=%s", DST_PORT_VAR, _dstPort);
        }
        if (_dstProtocol != null) {
          retString += String.format(" | %s=%s", DST_PROTOCOL_VAR, _dstProtocol);
        }
        if (_ecn != null) {
          retString += String.format(" | %s=%s", ECN_VAR, _ecn);
        }
        if (_icmpCode != null) {
          retString += String.format(" | %s=%s", ICMP_CODE_VAR, _icmpCode);
        }
        if (_icmpType != null) {
          retString += String.format(" | %s=%s", ICMP_TYPE_VAR, _icmpType);
        }
        if (_ipProtocol != null) {
          retString += String.format(" | %s=%s", IP_PROTOCOL_VAR, _ipProtocol);
        }
        if (_packetLength != null) {
          retString += String.format(" | %s=%s", PACKET_LENGTH_VAR, _packetLength);
        }
        if (_srcIp != null) {
          retString += String.format(" | %s=%s", SRC_IP_VAR, _srcIp);
        }
        if (_srcPort != null) {
          retString += String.format(" | %s=%s", SRC_PORT_VAR, _srcPort);
        }
        if (_srcProtocol != null) {
          retString += String.format(" | %s=%s", SRC_PROTOCOL_VAR, _srcProtocol);
        }
        if (_state != null) {
          retString += String.format(" | %s=%s", STATE_VAR, _state);
        }
        if (_tcpFlagsAck != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_ACK_VAR, _tcpFlagsAck);
        }
        if (_tcpFlagsCwr != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_CWR_VAR, _tcpFlagsCwr);
        }
        if (_tcpFlagsEce != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_ECE_VAR, _tcpFlagsEce);
        }
        if (_tcpFlagsFin != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_FIN_VAR, _tcpFlagsFin);
        }
        if (_tcpFlagsPsh != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_PSH_VAR, _tcpFlagsPsh);
        }
        if (_tcpFlagsRst != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_RST_VAR, _tcpFlagsRst);
        }
        if (_tcpFlagsSyn != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_SYN_VAR, _tcpFlagsSyn);
        }
        if (_tcpFlagsUrg != null) {
          retString += String.format(" | %s=%s", TCP_FLAGS_URG_VAR, _tcpFlagsUrg);
        }
        return retString;
      } catch (Exception e) {
        try {
          return "Pretty printing failed. Printing Json\n" + toJsonString();
        } catch (BatfishException e1) {
          throw new BatfishException("Both pretty and json printing failed\n");
        }
      }
    }

    @JsonProperty(DSCP_VAR)
    public void setDscp(Integer dscp) {
      _dscp = dscp;
    }

    @Override
    @JsonProperty(DST_IP_VAR)
    public void setDstIp(Ip dstIp) {
      _dstIp = dstIp;
    }

    @JsonProperty(DST_PORT_VAR)
    public void setDstPort(Integer dstPort) {
      _dstPort = dstPort;
    }

    @Override
    @JsonProperty(DST_PROTOCOL_VAR)
    public void setDstProtocol(Protocol dstProtocol) {
      _dstProtocol = dstProtocol;
    }

    @JsonProperty(ECN_VAR)
    public void setEcn(Integer ecn) {
      _ecn = ecn;
    }

    @JsonProperty(ICMP_CODE_VAR)
    public void setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
    }

    @JsonProperty(ICMP_TYPE_VAR)
    public void setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
    }

    @JsonProperty(INGRESS_INTERFACE_VAR)
    public void setIngressInterface(String ingressInterface) {
      _ingressInterface = ingressInterface;
    }

    @Override
    @JsonProperty(INGRESS_NODE_VAR)
    public void setIngressNode(String ingressNode) {
      _ingressNode = ingressNode;
    }

    @Override
    @JsonProperty(INGRESS_VRF_VAR)
    public void setIngressVrf(String ingressVrf) {
      _ingressVrf = ingressVrf;
    }

    @JsonProperty(IP_PROTOCOL_VAR)
    public void setIpProtocol(IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
    }

    @JsonProperty(PACKET_LENGTH_VAR)
    public void setPacketLength(Integer packetLength) {
      _packetLength = packetLength;
    }

    @JsonProperty(SRC_IP_VAR)
    public void setSrcIp(Ip srcIp) {
      _srcIp = srcIp;
    }

    @JsonProperty(SRC_PORT_VAR)
    public void setSrcPort(Integer srcPort) {
      _srcPort = srcPort;
    }

    @JsonProperty(SRC_PROTOCOL_VAR)
    public void setSrcProtocol(Protocol srcProtocol) {
      _srcProtocol = srcProtocol;
    }

    @JsonProperty(STATE_VAR)
    public void setState(State state) {
      _state = state;
    }

    @JsonProperty(TCP_FLAGS_ACK_VAR)
    public void setTcpFlagsAck(Boolean tcpFlagsAck) {
      _tcpFlagsAck = tcpFlagsAck;
    }

    @JsonProperty(TCP_FLAGS_CWR_VAR)
    public void setTcpFlagsCwr(Boolean tcpFlagsCwr) {
      _tcpFlagsCwr = tcpFlagsCwr;
    }

    @JsonProperty(TCP_FLAGS_ECE_VAR)
    public void setTcpFlagsEce(Boolean tcpFlagsEce) {
      _tcpFlagsEce = tcpFlagsEce;
    }

    @JsonProperty(TCP_FLAGS_FIN_VAR)
    public void setTcpFlagsFin(Boolean tcpFlagsFin) {
      _tcpFlagsFin = tcpFlagsFin;
    }

    @JsonProperty(TCP_FLAGS_PSH_VAR)
    public void setTcpFlagsPsh(Boolean tcpFlagsPsh) {
      _tcpFlagsPsh = tcpFlagsPsh;
    }

    @JsonProperty(TCP_FLAGS_RST_VAR)
    public void setTcpFlagsRst(Boolean tcpFlagsRst) {
      _tcpFlagsRst = tcpFlagsRst;
    }

    @JsonProperty(TCP_FLAGS_SYN_VAR)
    public void setTcpFlagsSyn(Boolean tcpFlagsSyn) {
      _tcpFlagsSyn = tcpFlagsSyn;
    }

    @JsonProperty(TCP_FLAGS_URG_VAR)
    public void setTcpFlagsUrg(Boolean tcpFlagsUrg) {
      _tcpFlagsUrg = tcpFlagsUrg;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new TracerouteAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new TracerouteQuestion();
  }
}
