package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.questions.ITracerouteQuestion;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class TracerouteQuestionPlugin extends QuestionPlugin {

  public static class TracerouteAnswerer extends Answerer {

    public TracerouteAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      String tag = _batfish.getFlowTag();
      Set<Flow> flows = getFlows(tag);
      _batfish.processFlows(flows, ((TracerouteQuestion) _question).getIgnoreAcls());
      AnswerElement answerElement = _batfish.getHistory();
      return answerElement;
    }

    @Override
    public AnswerElement answerDiff() {
      String tag = _batfish.getDifferentialFlowTag();
      Set<Flow> flows = getFlows(tag);
      TracerouteQuestion tracerouteQuestion = (TracerouteQuestion) _question;
      _batfish.pushBaseEnvironment();
      _batfish.processFlows(flows, tracerouteQuestion.getIgnoreAcls());
      _batfish.popEnvironment();
      _batfish.pushDeltaEnvironment();
      _batfish.processFlows(flows, tracerouteQuestion.getIgnoreAcls());
      _batfish.popEnvironment();
      FlowHistory history = _batfish.getHistory();
      FlowHistory filteredHistory = new FlowHistory();
      for (String flowText : history.getTraces().keySet()) {
        // String baseEnvTag = _batfish.getBaseTestrigSettings().getEnvName() +
        // ":"
        // + _batfish.getBaseTestrigSettings().getEnvironmentSettings()
        // .getEnvName();
        _batfish.pushBaseEnvironment();
        String baseEnvTag = _batfish.getFlowTag();
        Environment baseEnv = _batfish.getEnvironment();
        _batfish.popEnvironment();
        // String deltaEnvTag = _batfish.getDeltaTestrigSettings().getEnvName()
        // +
        // ":"
        // + _batfish.getDeltaTestrigSettings().getEnvironmentSettings()
        // .getEnvName();
        _batfish.pushDeltaEnvironment();
        String deltaEnvTag = _batfish.getFlowTag();
        Environment deltaEnv = _batfish.getEnvironment();
        _batfish.popEnvironment();
        Set<FlowTrace> baseFlowTraces =
            history.getTraces().get(flowText).getPaths().get(baseEnvTag);
        Set<FlowTrace> deltaFlowTraces =
            history.getTraces().get(flowText).getPaths().get(deltaEnvTag);
        if (!baseFlowTraces.toString().equals(deltaFlowTraces.toString())) {
          Flow flow = history.getTraces().get(flowText).getFlow();
          for (FlowTrace flowTrace : baseFlowTraces) {
            filteredHistory.addFlowTrace(flow, baseEnvTag, baseEnv, flowTrace);
          }
          for (FlowTrace flowTrace : deltaFlowTraces) {
            filteredHistory.addFlowTrace(flow, deltaEnvTag, deltaEnv, flowTrace);
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
          Configuration node =
              Strings.isNullOrEmpty(hostname) ? null : configurations.get(hostname);
          if (node != null) {
            Ip canonicalIp = node.getCanonicalIp();
            if (canonicalIp != null) {
              flowBuilder.setSrcIp(canonicalIp);
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
        if (flowBuilder.getDstIp().equals(Ip.AUTO)) {
          if (configurations == null) {
            _batfish.pushBaseEnvironment();
            configurations = _batfish.loadConfigurations();
            _batfish.popEnvironment();
          }
          String hostname = question.getDst();
          Configuration node =
              Strings.isNullOrEmpty(hostname) ? null : configurations.get(hostname);
          if (node != null) {
            Ip canonicalIp = node.getCanonicalIp();
            if (canonicalIp != null) {
              flowBuilder.setDstIp(canonicalIp);
            } else {
              throw new BatfishException(
                  "Cannot automatically assign destination ip to flow since no there are no ip "
                      + "addresses assigned to any interface on destination node: '"
                      + hostname
                      + "'");
            }
          } else {
            throw new BatfishException(
                "Destination is neither a valid node nor IP: '" + hostname + "'");
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
  public static class TracerouteQuestion extends ITracerouteQuestion {

    private boolean _ignoreAcls;

    public TracerouteQuestion() {
      _ignoreAcls = false;
    }

    @Override
    public boolean getDataPlane() {
      return true;
    }

    @JsonProperty(PROP_IGNORE_ACLS)
    public boolean getIgnoreAcls() {
      return _ignoreAcls;
    }

    @Override
    public String getName() {
      return "traceroute";
    }

    @Override
    public String prettyPrint() {
      try {
        String retString =
            String.format("traceroute %singressNode=%s", prettyPrintBase(), _ingressNode);
        // we only print "interesting" values
        if (_ingressInterface != null) {
          retString += String.format(", %s=%s", PROP_INGRESS_INTERFACE, _ingressInterface);
        }
        if (_ingressVrf != null) {
          retString += String.format(", %s=%s", PROP_INGRESS_VRF, _ingressVrf);
        }
        if (_dscp != null) {
          retString += String.format(", %s=%s", PROP_DSCP, _dscp);
        }
        if (_dst != null) {
          retString += String.format(", %s=%s", PROP_DST, _dst);
        }
        if (_dstPort != null) {
          retString += String.format(", %S=%s", PROP_DST_PORT, _dstPort);
        }
        if (_dstProtocol != null) {
          retString += String.format(", %s=%s", PROP_DST_PROTOCOL, _dstProtocol);
        }
        if (_ecn != null) {
          retString += String.format(", %s=%s", PROP_ECN, _ecn);
        }
        if (_icmpCode != null) {
          retString += String.format(", %s=%s", PROP_ICMP_CODE, _icmpCode);
        }
        if (_icmpType != null) {
          retString += String.format(", %s=%s", PROP_ICMP_TYPE, _icmpType);
        }
        if (_ignoreAcls) {
          retString += String.format(", %s=%s", PROP_IGNORE_ACLS, _ignoreAcls);
        }
        if (_ipProtocol != null) {
          retString += String.format(", %s=%s", PROP_IP_PROTOCOL, _ipProtocol);
        }
        if (_packetLength != null) {
          retString += String.format(", %s=%s", PROP_PACKET_LENGTH, _packetLength);
        }
        if (_srcIp != null) {
          retString += String.format(", %s=%s", PROP_SRC_IP, _srcIp);
        }
        if (_srcPort != null) {
          retString += String.format(", %s=%s", PROP_SRC_PORT, _srcPort);
        }
        if (_srcProtocol != null) {
          retString += String.format(", %s=%s", PROP_SRC_PROTOCOL, _srcProtocol);
        }
        if (_state != null) {
          retString += String.format(", %s=%s", PROP_STATE, _state);
        }
        if (_tcpFlagsAck != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_ACK, _tcpFlagsAck);
        }
        if (_tcpFlagsCwr != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_CWR, _tcpFlagsCwr);
        }
        if (_tcpFlagsEce != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_ECE, _tcpFlagsEce);
        }
        if (_tcpFlagsFin != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_FIN, _tcpFlagsFin);
        }
        if (_tcpFlagsPsh != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_PSH, _tcpFlagsPsh);
        }
        if (_tcpFlagsRst != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_RST, _tcpFlagsRst);
        }
        if (_tcpFlagsSyn != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_SYN, _tcpFlagsSyn);
        }
        if (_tcpFlagsUrg != null) {
          retString += String.format(", %s=%s", PROP_TCP_FLAGS_URG, _tcpFlagsUrg);
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

    @JsonProperty(PROP_DSCP)
    public void setDscp(Integer dscp) {
      _dscp = dscp;
    }

    @Override
    @JsonProperty(PROP_DST)
    public void setDst(String dst) {
      _dst = dst;
    }

    @JsonProperty(PROP_DST_PORT)
    public void setDstPort(Integer dstPort) {
      _dstPort = dstPort;
    }

    @Override
    @JsonProperty(PROP_DST_PROTOCOL)
    public void setDstProtocol(Protocol dstProtocol) {
      _dstProtocol = dstProtocol;
    }

    @JsonProperty(PROP_ECN)
    public void setEcn(Integer ecn) {
      _ecn = ecn;
    }

    @JsonProperty(PROP_ICMP_CODE)
    public void setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
    }

    @JsonProperty(PROP_ICMP_TYPE)
    public void setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
    }

    @JsonProperty(PROP_IGNORE_ACLS)
    public void setIgnoreAcls(boolean ignoreAcls) {
      _ignoreAcls = ignoreAcls;
    }

    @JsonProperty(PROP_INGRESS_INTERFACE)
    public void setIngressInterface(String ingressInterface) {
      _ingressInterface = ingressInterface;
    }

    @Override
    @JsonProperty(PROP_INGRESS_NODE)
    public void setIngressNode(String ingressNode) {
      _ingressNode = ingressNode;
    }

    @Override
    @JsonProperty(PROP_INGRESS_VRF)
    public void setIngressVrf(String ingressVrf) {
      _ingressVrf = ingressVrf;
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

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new TracerouteAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new TracerouteQuestion();
  }
}
