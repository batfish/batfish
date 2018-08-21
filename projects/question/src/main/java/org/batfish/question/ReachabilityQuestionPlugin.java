package org.batfish.question;

import static org.batfish.common.util.CommonUtil.asNegativeIpWildcards;
import static org.batfish.common.util.CommonUtil.asPositiveIpWildcards;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.IReachabilityQuestion;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class ReachabilityQuestionPlugin extends QuestionPlugin {

  public static class ReachabilityAnswerer extends Answerer {

    public ReachabilityAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      ReachabilityQuestion question = (ReachabilityQuestion) _question;
      ReachabilityType type = question.getReachabilityType();
      switch (type) {
        case MULTIPATH:
          return multipath(question);
        case STANDARD:
          return standard(question);
        case PATH_DIFF:
        case REDUCED_REACHABILITY:
        case INCREASED:
        case MULTIPATH_DIFF:
        default:
          throw new BatfishException(
              "Unsupported non-differential reachability type: " + type.reachabilityTypeName());
      }
    }

    @Override
    public AnswerElement answerDiff() {
      ReachabilityQuestion question = (ReachabilityQuestion) _question;
      ReachabilityType type = question.getReachabilityType();
      switch (type) {
        case PATH_DIFF:
          return pathDiff(question);
        case REDUCED_REACHABILITY:
          return reducedReachability(question);
        case STANDARD:
        case MULTIPATH:
        case INCREASED:
        case MULTIPATH_DIFF:
        default:
          throw new BatfishException(
              "Unsupported differential reachabilty type: " + type.reachabilityTypeName());
      }
    }

    private AnswerElement multipath(ReachabilityQuestion question) {
      return _batfish.multipath(question._reachabilitySettings.build().toReachabilityParameters());
    }

    private AnswerElement pathDiff(ReachabilityQuestion question) {
      return _batfish.pathDiff(question._reachabilitySettings.build().toReachabilityParameters());
    }

    private AnswerElement reducedReachability(ReachabilityQuestion question) {
      return _batfish.reducedReachability(
          question._reachabilitySettings.build().toReachabilityParameters());
    }

    private AnswerElement standard(ReachabilityQuestion question) {
      return _batfish.standard(question._reachabilitySettings.build().toReachabilityParameters());
    }
  }

  // <question_page_comment>

  /**
   * Details coming.
   *
   * <p>More details coming.
   *
   * @type Reachability dataplane
   * @param transitNodes set of transit nodes (packet must transit through all of them)
   * @param nonTransitNodes set of non-transit nodes (packet does not transit through any of them)
   * @param DetailsComing Details coming.
   * @example bf_answer("Reachability", dstIps=["2.128.0.101"], dstPorts=[53], ipProtocols=["UDP"],
   *     actions=["drop"]) Finds all (starting node, packet header) combinations that cannot reach
   *     (action=drop) the 2.128.0.101 using a DNS (UDP on port 53) packet.
   * @example bf_answer_type("Reachability", actions=["PERMIT"], dstIps=["2.128.1.101"],
   *     notDstPorts=[22], notIpProtocols=["TCP"]) Finds all (starting node, packet header)
   *     combinations that can reach (action=drop) 2.128.1.101 using non-SSH packets.
   */
  public static class ReachabilityQuestion extends Question implements IReachabilityQuestion {

    private static final SortedSet<ForwardingAction> DEFAULT_ACTIONS =
        ImmutableSortedSet.of(ForwardingAction.ACCEPT);

    private static final NodesSpecifier DEFAULT_FINAL_NODES = NodesSpecifier.ALL;

    private static final NodesSpecifier DEFAULT_INGRESS_NODES = NodesSpecifier.ALL;

    private static final InterfacesSpecifier DEFAULT_INGRESS_INTERFACES = InterfacesSpecifier.ALL;

    private static final int DEFAULT_MAX_CHUNK_SIZE = 1;

    private static final NodesSpecifier DEFAULT_NON_TRANSIT_NODES = null;

    private static final NodesSpecifier DEFAULT_NOT_FINAL_NODE_REGEX = null;

    private static final NodesSpecifier DEFAULT_NOT_INGRESS_NODE_REGEX = null;

    private static final boolean DEFAULT_SPECIALIZE = true;

    private static final NodesSpecifier DEFAULT_TRANSIT_NODES = null;

    private static final boolean DEFAULT_USE_COMPRESSION = false;

    private static final String PROP_ACTIONS = "actions";

    private static final String PROP_DST_IPS = "dstIps";

    private static final String PROP_DST_PORTS = "dstPorts";

    private static final String PROP_DST_PROTOCOLS = "dstProtocols";

    private static final String PROP_FINAL_NODE_REGEX = "finalNodeRegex";

    private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";

    private static final String PROP_ICMP_CODES = "icmpCodes";

    private static final String PROP_ICMP_TYPES = "icmpTypes";

    private static final String PROP_INGRESS_INTERFACES = "ingressInterfaces";

    private static final String PROP_INGRESS_NODE_REGEX = "ingressNodeRegex";

    private static final String PROP_IP_PROTOCOLS = "ipProtocols";

    private static final String PROP_MAX_CHUNK_SIZE = "maxChunkSize";

    private static final String PROP_NEGATE_HEADER = "negateHeader";

    private static final String PROP_NON_TRANSIT_NODES = "notTransitNodes";

    private static final String PROP_NOT_DST_IPS = "notDstIps";

    private static final String PROP_NOT_DST_PORTS = "notDstPorts";

    private static final String PROP_NOT_DST_PROTOCOLS = "notDstProtocols";

    private static final String PROP_NOT_FINAL_NODE_REGEX = "notFinalNodeRegex";

    private static final String PROP_NOT_FRAGMENT_OFFSETS = "notFragmentOffsets";

    private static final String PROP_NOT_ICMP_CODES = "notIcmpCodes";

    private static final String PROP_NOT_ICMP_TYPES = "notIcmpTypes";

    private static final String PROP_NOT_INGRESS_NODE_REGEX = "notIngressNodeRegex";

    private static final String PROP_NOT_IP_PROTOCOLS = "notIpProtocols";

    private static final String PROP_NOT_PACKET_LENGTHS = "notPacketLengths";

    private static final String PROP_NOT_SRC_IPS = "notSrcIps";

    private static final String PROP_NOT_SRC_PORTS = "notSrcPorts";

    private static final String PROP_NOT_SRC_PROTOCOLS = "notSrcProtocols";

    private static final String PROP_PACKET_LENGTHS = "packetLengths";

    private static final String PROP_REACHABILITY_TYPE = "type";

    private static final String PROP_SPECIALIZE = "specialize";

    private static final String PROP_SRC_IPS = "srcIps";

    private static final String PROP_SRC_NATTED = "srcNatted";

    private static final String PROP_SRC_OR_DST_IPS = "srcOrDstIps";

    private static final String PROP_SRC_OR_DST_PORTS = "srcOrDstPorts";

    private static final String PROP_SRC_OR_DST_PROTOCOLS = "srcOrDstProtocols";

    private static final String PROP_SRC_PORTS = "srcPorts";

    private static final String PROP_SRC_PROTOCOLS = "srcProtocols";

    private static final String PROP_TRANSIT_NODES = "transitNodes";

    private static final String PROP_USE_COMPRESSION = "useCompression";

    private ReachabilitySettings.Builder _reachabilitySettings;

    private ReachabilityType _reachabilityType;

    public ReachabilityQuestion() {
      _reachabilitySettings = ReachabilitySettings.builder();
      setActions(DEFAULT_ACTIONS);
      setFinalNodeRegex(DEFAULT_FINAL_NODES);
      setHeaderSpace(new HeaderSpace());
      setIngressInterfaces(DEFAULT_INGRESS_INTERFACES);
      setIngressNodes(DEFAULT_INGRESS_NODES);
      setMaxChunkSize(DEFAULT_MAX_CHUNK_SIZE);
      setNotFinalNodeRegex(DEFAULT_NOT_FINAL_NODE_REGEX);
      setNonTransitNodes(DEFAULT_NON_TRANSIT_NODES);
      setNotIngressNodeRegex(DEFAULT_NOT_INGRESS_NODE_REGEX);
      _reachabilityType = ReachabilityType.STANDARD;
      setSpecialize(DEFAULT_SPECIALIZE);
      setTransitNodes(DEFAULT_TRANSIT_NODES);
      setUseCompression(DEFAULT_USE_COMPRESSION);
    }

    @JsonProperty(PROP_ACTIONS)
    public SortedSet<ForwardingAction> getActions() {
      return _reachabilitySettings.getActions();
    }

    @Override
    public boolean getDataPlane() {
      return true;
    }

    @JsonProperty(PROP_DST_IPS)
    public SortedSet<IpWildcard> getDstIps() {
      return asPositiveIpWildcards(_reachabilitySettings.getHeaderSpace().getDstIps());
    }

    @JsonProperty(PROP_DST_PORTS)
    public SortedSet<SubRange> getDstPorts() {
      return _reachabilitySettings.getHeaderSpace().getDstPorts();
    }

    @JsonProperty(PROP_DST_PROTOCOLS)
    public SortedSet<Protocol> getDstProtocols() {
      return _reachabilitySettings.getHeaderSpace().getDstProtocols();
    }

    @JsonProperty(PROP_FINAL_NODE_REGEX)
    public NodesSpecifier getFinalNodeRegex() {
      return _reachabilitySettings.getFinalNodes();
    }

    @JsonProperty(PROP_FRAGMENT_OFFSETS)
    public SortedSet<SubRange> getFragmentOffsets() {
      return _reachabilitySettings.getHeaderSpace().getFragmentOffsets();
    }

    @JsonProperty(PROP_ICMP_CODES)
    public SortedSet<SubRange> getIcmpCodes() {
      return _reachabilitySettings.getHeaderSpace().getIcmpCodes();
    }

    @JsonProperty(PROP_ICMP_TYPES)
    public SortedSet<SubRange> getIcmpTypes() {
      return _reachabilitySettings.getHeaderSpace().getIcmpTypes();
    }

    @JsonProperty(PROP_INGRESS_NODE_REGEX)
    public NodesSpecifier getIngressNodeRegex() {
      return _reachabilitySettings.getIngressNodes();
    }

    @JsonProperty(PROP_IP_PROTOCOLS)
    public SortedSet<IpProtocol> getIpProtocols() {
      return _reachabilitySettings.getHeaderSpace().getIpProtocols();
    }

    @JsonProperty(PROP_MAX_CHUNK_SIZE)
    public int getMaxChunkSize() {
      return _reachabilitySettings.getMaxChunkSize();
    }

    @Override
    public String getName() {
      return "reachability";
    }

    @JsonProperty(PROP_NEGATE_HEADER)
    public boolean getNegateHeader() {
      return _reachabilitySettings.getHeaderSpace().getNegate();
    }

    @JsonProperty(PROP_NON_TRANSIT_NODES)
    public NodesSpecifier getNonTransitNodes() {
      return _reachabilitySettings.getNonTransitNodes();
    }

    @JsonProperty(PROP_NOT_DST_IPS)
    public SortedSet<IpWildcard> getNotDstIps() {
      return asNegativeIpWildcards(_reachabilitySettings.getHeaderSpace().getNotDstIps());
    }

    @JsonProperty(PROP_NOT_DST_PORTS)
    public SortedSet<SubRange> getNotDstPorts() {
      return _reachabilitySettings.getHeaderSpace().getNotDstPorts();
    }

    @JsonProperty(PROP_NOT_DST_PROTOCOLS)
    public SortedSet<Protocol> getNotDstProtocols() {
      return _reachabilitySettings.getHeaderSpace().getNotDstProtocols();
    }

    @JsonProperty(PROP_NOT_FINAL_NODE_REGEX)
    public NodesSpecifier getNotFinalNodeRegex() {
      return _reachabilitySettings.getNotFinalNodes();
    }

    @JsonProperty(PROP_NOT_FRAGMENT_OFFSETS)
    private SortedSet<SubRange> getNotFragmentOffsets() {
      return _reachabilitySettings.getHeaderSpace().getNotFragmentOffsets();
    }

    @JsonProperty(PROP_NOT_ICMP_CODES)
    public SortedSet<SubRange> getNotIcmpCodes() {
      return _reachabilitySettings.getHeaderSpace().getNotIcmpCodes();
    }

    @JsonProperty(PROP_NOT_ICMP_TYPES)
    public SortedSet<SubRange> getNotIcmpTypes() {
      return _reachabilitySettings.getHeaderSpace().getNotIcmpTypes();
    }

    @JsonProperty(PROP_NOT_INGRESS_NODE_REGEX)
    public NodesSpecifier getNotIngressNodeRegex() {
      return _reachabilitySettings.getNotIngressNodes();
    }

    @JsonProperty(PROP_NOT_IP_PROTOCOLS)
    public SortedSet<IpProtocol> getNotIpProtocols() {
      return _reachabilitySettings.getHeaderSpace().getNotIpProtocols();
    }

    @JsonProperty(PROP_NOT_PACKET_LENGTHS)
    public SortedSet<SubRange> getNotPacketLengths() {
      return _reachabilitySettings.getHeaderSpace().getNotPacketLengths();
    }

    @JsonProperty(PROP_NOT_SRC_IPS)
    public SortedSet<IpWildcard> getNotSrcIps() {
      return asNegativeIpWildcards(_reachabilitySettings.getHeaderSpace().getNotSrcIps());
    }

    @JsonProperty(PROP_NOT_SRC_PORTS)
    public SortedSet<SubRange> getNotSrcPorts() {
      return _reachabilitySettings.getHeaderSpace().getNotSrcPorts();
    }

    @JsonProperty(PROP_NOT_SRC_PROTOCOLS)
    public SortedSet<Protocol> getNotSrcProtocols() {
      return _reachabilitySettings.getHeaderSpace().getNotSrcProtocols();
    }

    @JsonProperty(PROP_PACKET_LENGTHS)
    public SortedSet<SubRange> getPacketLengths() {
      return _reachabilitySettings.getHeaderSpace().getPacketLengths();
    }

    @JsonProperty(PROP_REACHABILITY_TYPE)
    public ReachabilityType getReachabilityType() {
      return _reachabilityType;
    }

    @JsonProperty(PROP_SRC_IPS)
    public SortedSet<IpWildcard> getSrcIps() {
      return asPositiveIpWildcards(_reachabilitySettings.getHeaderSpace().getSrcIps());
    }

    @JsonProperty(PROP_SRC_NATTED)
    public Boolean getSrcNatted() {
      return _reachabilitySettings.getSrcNatted();
    }

    @JsonProperty(PROP_SRC_OR_DST_IPS)
    public SortedSet<IpWildcard> getSrcOrDstIps() {
      return asPositiveIpWildcards(_reachabilitySettings.getHeaderSpace().getSrcOrDstIps());
    }

    @JsonProperty(PROP_SRC_OR_DST_PORTS)
    public SortedSet<SubRange> getSrcOrDstPorts() {
      return _reachabilitySettings.getHeaderSpace().getSrcOrDstPorts();
    }

    @JsonProperty(PROP_SRC_OR_DST_PROTOCOLS)
    public SortedSet<Protocol> getSrcOrDstProtocols() {
      return _reachabilitySettings.getHeaderSpace().getSrcOrDstProtocols();
    }

    @JsonProperty(PROP_SRC_PORTS)
    public SortedSet<SubRange> getSrcPorts() {
      return _reachabilitySettings.getHeaderSpace().getSrcPorts();
    }

    @JsonProperty(PROP_SRC_PROTOCOLS)
    public SortedSet<Protocol> getSrcProtocols() {
      return _reachabilitySettings.getHeaderSpace().getSrcProtocols();
    }

    @JsonProperty(PROP_TRANSIT_NODES)
    public NodesSpecifier getTransitNodes() {
      return _reachabilitySettings.getTransitNodes();
    }

    @JsonProperty(PROP_USE_COMPRESSION)
    public boolean getUseCompression() {
      return _reachabilitySettings.getUseCompression();
    }

    @JsonProperty(PROP_SPECIALIZE)
    public boolean getSpecialize() {
      return _reachabilitySettings.getSpecialize();
    }

    @Override
    public String prettyPrint() {
      try {
        String retString =
            String.format("reachability %sactions=%s", prettyPrintBase(), getActions());
        // we only print "interesting" values
        if (_reachabilityType != ReachabilityType.STANDARD) {
          retString += String.format(", %s=%s", PROP_REACHABILITY_TYPE, _reachabilityType);
        }
        if (getNegateHeader()) {
          retString += ", negateHeader=true";
        }
        if (getDstIps() != null && !getDstIps().isEmpty()) {
          retString += String.format(", %s=%s", PROP_DST_IPS, getDstIps());
        }
        if (getDstPorts() != null && !getDstPorts().isEmpty()) {
          retString += String.format(", %s=%s", PROP_DST_PORTS, getDstPorts());
        }
        if (getDstProtocols() != null && !getDstProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_DST_PROTOCOLS, getDstProtocols());
        }
        if (!getFinalNodeRegex().equals(DEFAULT_FINAL_NODES)) {
          retString += String.format(", %s=%s", PROP_FINAL_NODE_REGEX, getFinalNodeRegex());
        }
        if (getFragmentOffsets() != null && !getFragmentOffsets().isEmpty()) {
          retString += String.format(", %s=%s", PROP_FRAGMENT_OFFSETS, getFragmentOffsets());
        }
        if (getIcmpCodes() != null && !getIcmpCodes().isEmpty()) {
          retString += String.format(", %s=%s", PROP_ICMP_CODES, getIcmpCodes());
        }
        if (getIcmpTypes() != null && !getIcmpTypes().isEmpty()) {
          retString += String.format(", %s=%s", PROP_ICMP_TYPES, getIcmpTypes());
        }
        if (!getIngressNodeRegex().equals(DEFAULT_INGRESS_NODES)) {
          retString += String.format(", %s=%s", PROP_INGRESS_NODE_REGEX, getIngressNodeRegex());
        }
        if (getIpProtocols() != null && !getIpProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_IP_PROTOCOLS, getIpProtocols());
        }
        if (getPacketLengths() != null && !getPacketLengths().isEmpty()) {
          retString += String.format(", %s=%s", PROP_PACKET_LENGTHS, getPacketLengths());
        }
        if (getSrcIps() != null && !getSrcIps().isEmpty()) {
          retString += String.format(", %s=%s", PROP_SRC_IPS, getSrcIps());
        }
        if (getSrcPorts() != null && !getSrcPorts().isEmpty()) {
          retString += String.format(", %s=%s", PROP_SRC_PORTS, getSrcPorts());
        }
        if (getSrcProtocols() != null && !getSrcProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_SRC_PROTOCOLS, getSrcProtocols());
        }
        if (getSrcOrDstIps() != null && !getSrcOrDstIps().isEmpty()) {
          retString += String.format(", %s=%s", PROP_SRC_OR_DST_IPS, getSrcOrDstIps());
        }
        if (getSrcOrDstPorts() != null && !getSrcOrDstPorts().isEmpty()) {
          retString += String.format(", %s=%s", PROP_SRC_OR_DST_PORTS, getSrcOrDstPorts());
        }
        if (getSrcOrDstProtocols() != null && !getSrcOrDstProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_SRC_OR_DST_PROTOCOLS, getSrcOrDstProtocols());
        }
        if (!getTransitNodes().equals(DEFAULT_TRANSIT_NODES)) {
          retString += String.format(", %s=%s", PROP_TRANSIT_NODES, getTransitNodes());
        }
        if (getNotDstIps() != null && !getNotDstIps().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_DST_IPS, getNotDstIps());
        }
        if (getNotDstPorts() != null && !getNotDstPorts().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_DST_PORTS, getNotDstPorts());
        }
        if (getNotDstProtocols() != null && !getNotDstProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_DST_PROTOCOLS, getNotDstProtocols());
        }
        if (!getNotFinalNodeRegex().equals(DEFAULT_NOT_FINAL_NODE_REGEX)) {
          retString += String.format(", %s=%s", PROP_NOT_FINAL_NODE_REGEX, getNotFinalNodeRegex());
        }
        if (getNotFragmentOffsets() != null && !getNotFragmentOffsets().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_FRAGMENT_OFFSETS, getNotFragmentOffsets());
        }
        if (getNotIcmpCodes() != null && !getNotIcmpCodes().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_ICMP_CODES, getNotIcmpCodes());
        }
        if (getNotIcmpTypes() != null && !getNotIcmpTypes().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_ICMP_TYPES, getNotIcmpTypes());
        }
        if (!getNotIngressNodeRegex().equals(DEFAULT_NOT_INGRESS_NODE_REGEX)) {
          retString +=
              String.format(", %s=%s", PROP_NOT_INGRESS_NODE_REGEX, getNotIngressNodeRegex());
        }
        if (getNotIpProtocols() != null && !getNotIpProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_IP_PROTOCOLS, getNotIpProtocols());
        }
        if (getNotPacketLengths() != null && !getNotPacketLengths().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_PACKET_LENGTHS, getNotPacketLengths());
        }
        if (getNotSrcIps() != null && !getNotSrcIps().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_SRC_IPS, getNotSrcIps());
        }
        if (getNotSrcPorts() != null && !getNotSrcPorts().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_SRC_PORTS, getNotSrcPorts());
        }
        if (getNotSrcProtocols() != null && !getNotSrcProtocols().isEmpty()) {
          retString += String.format(", %s=%s", PROP_NOT_SRC_PROTOCOLS, getNotSrcProtocols());
        }
        if (!getNonTransitNodes().equals(DEFAULT_NON_TRANSIT_NODES)) {
          retString += String.format(", %s=%s", PROP_NON_TRANSIT_NODES, getNonTransitNodes());
        }
        if (getSrcNatted() != null) {
          retString += String.format(", %s=%s", PROP_SRC_NATTED, getSrcNatted());
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

    @Override
    @JsonProperty(PROP_ACTIONS)
    public void setActions(SortedSet<ForwardingAction> actionSet) {
      _reachabilitySettings.setActions(ImmutableSortedSet.copyOf(actionSet));
    }

    @Override
    @JsonProperty(PROP_DST_IPS)
    public void setDstIps(SortedSet<IpWildcard> dstIps) {
      _reachabilitySettings.getHeaderSpace().setDstIps(ImmutableSortedSet.copyOf(dstIps));
    }

    @JsonProperty(PROP_DST_PORTS)
    public void setDstPorts(SortedSet<SubRange> dstPorts) {
      _reachabilitySettings.getHeaderSpace().setDstPorts(ImmutableSortedSet.copyOf(dstPorts));
    }

    @Override
    @JsonProperty(PROP_DST_PROTOCOLS)
    public void setDstProtocols(SortedSet<Protocol> dstProtocols) {
      _reachabilitySettings
          .getHeaderSpace()
          .setDstProtocols(ImmutableSortedSet.copyOf(dstProtocols));
    }

    @Override
    @JsonProperty(PROP_INGRESS_INTERFACES)
    public void setIngressInterfaces(InterfacesSpecifier ingressInterfaces) {
      _reachabilitySettings.setIngressInterfaces(ingressInterfaces);
    }

    @JsonProperty(PROP_FINAL_NODE_REGEX)
    public void setFinalNodeRegex(NodesSpecifier regex) {
      _reachabilitySettings.setFinalNodes(regex);
    }

    private void setHeaderSpace(HeaderSpace headerSpace) {
      _reachabilitySettings.setHeaderSpace(headerSpace);
    }

    @JsonProperty(PROP_ICMP_CODES)
    public void setIcmpCodes(SortedSet<SubRange> icmpCodes) {
      _reachabilitySettings.getHeaderSpace().setIcmpCodes(ImmutableSortedSet.copyOf(icmpCodes));
    }

    @JsonProperty(PROP_ICMP_TYPES)
    public void setIcmpTypes(SortedSet<SubRange> icmpTypes) {
      _reachabilitySettings.getHeaderSpace().setIcmpTypes(ImmutableSortedSet.copyOf(icmpTypes));
    }

    @Override
    @JsonProperty(PROP_INGRESS_NODE_REGEX)
    public void setIngressNodes(NodesSpecifier ingressNodes) {
      _reachabilitySettings.setIngressNodes(ingressNodes);
    }

    @JsonProperty(PROP_IP_PROTOCOLS)
    public void setIpProtocols(SortedSet<IpProtocol> ipProtocols) {
      _reachabilitySettings.getHeaderSpace().setIpProtocols(ipProtocols);
    }

    @JsonProperty(PROP_MAX_CHUNK_SIZE)
    public void setMaxChunkSize(int maxChunkSize) {
      _reachabilitySettings.setMaxChunkSize(maxChunkSize);
    }

    @JsonProperty(PROP_NEGATE_HEADER)
    public void setNegateHeader(boolean negateHeader) {
      _reachabilitySettings.getHeaderSpace().setNegate(negateHeader);
    }

    @JsonProperty(PROP_NON_TRANSIT_NODES)
    public void setNonTransitNodes(NodesSpecifier nonTransitNodes) {
      _reachabilitySettings.setNonTransitNodes(nonTransitNodes);
    }

    @JsonProperty(PROP_NOT_DST_IPS)
    public void setNotDstIps(SortedSet<IpWildcard> notDstIps) {
      _reachabilitySettings.getHeaderSpace().setNotDstIps(ImmutableSortedSet.copyOf(notDstIps));
    }

    @JsonProperty(PROP_NOT_DST_PORTS)
    public void setNotDstPorts(SortedSet<SubRange> notDstPorts) {
      _reachabilitySettings.getHeaderSpace().setNotDstPorts(ImmutableSortedSet.copyOf(notDstPorts));
    }

    @Override
    @JsonProperty(PROP_NOT_DST_PROTOCOLS)
    public void setNotDstProtocols(SortedSet<Protocol> notDstProtocols) {
      _reachabilitySettings
          .getHeaderSpace()
          .setNotDstProtocols(ImmutableSortedSet.copyOf(notDstProtocols));
    }

    @JsonProperty(PROP_NOT_FINAL_NODE_REGEX)
    public void setNotFinalNodeRegex(NodesSpecifier notFinalNodeRegex) {
      _reachabilitySettings.setNotFinalNodeRegex(notFinalNodeRegex);
    }

    @JsonProperty(PROP_NOT_ICMP_CODES)
    public void setNotIcmpCodes(SortedSet<SubRange> notIcmpCodes) {
      _reachabilitySettings
          .getHeaderSpace()
          .setNotIcmpCodes(ImmutableSortedSet.copyOf(notIcmpCodes));
    }

    @JsonProperty(PROP_NOT_ICMP_TYPES)
    public void setNotIcmpTypes(SortedSet<SubRange> notIcmpType) {
      _reachabilitySettings
          .getHeaderSpace()
          .setNotIcmpTypes(ImmutableSortedSet.copyOf(notIcmpType));
    }

    @JsonProperty(PROP_NOT_INGRESS_NODE_REGEX)
    public void setNotIngressNodeRegex(NodesSpecifier notIngressNodeRegex) {
      _reachabilitySettings.setNotIngressNodeRegex(notIngressNodeRegex);
    }

    @JsonProperty(PROP_NOT_IP_PROTOCOLS)
    public void setNotIpProtocols(SortedSet<IpProtocol> notIpProtocols) {
      _reachabilitySettings.getHeaderSpace().setNotIpProtocols(notIpProtocols);
    }

    @JsonProperty(PROP_NOT_PACKET_LENGTHS)
    public void setNotPacketLengths(SortedSet<SubRange> notPacketLengths) {
      _reachabilitySettings
          .getHeaderSpace()
          .setNotPacketLengths(ImmutableSortedSet.copyOf(notPacketLengths));
    }

    @JsonProperty(PROP_NOT_SRC_IPS)
    public void setNotSrcIps(SortedSet<IpWildcard> notSrcIps) {
      _reachabilitySettings.getHeaderSpace().setNotSrcIps(ImmutableSortedSet.copyOf(notSrcIps));
    }

    @JsonProperty(PROP_NOT_SRC_PORTS)
    public void setNotSrcPortRange(SortedSet<SubRange> notSrcPorts) {
      _reachabilitySettings.getHeaderSpace().setNotSrcPorts(ImmutableSortedSet.copyOf(notSrcPorts));
    }

    @JsonProperty(PROP_NOT_SRC_PROTOCOLS)
    public void setNotSrcProtocols(SortedSet<Protocol> notSrcProtocols) {
      _reachabilitySettings
          .getHeaderSpace()
          .setNotSrcProtocols(ImmutableSortedSet.copyOf(notSrcProtocols));
    }

    @JsonProperty(PROP_PACKET_LENGTHS)
    public void setPacketLengths(SortedSet<SubRange> packetLengths) {
      _reachabilitySettings
          .getHeaderSpace()
          .setPacketLengths(ImmutableSortedSet.copyOf(packetLengths));
    }

    @JsonProperty(PROP_REACHABILITY_TYPE)
    public void setReachabilityType(ReachabilityType reachabilityType) {
      _reachabilityType = reachabilityType;
      switch (reachabilityType) {
        case INCREASED:
        case MULTIPATH_DIFF:
        case PATH_DIFF:
        case REDUCED_REACHABILITY:
          setDifferential(true);
          break;
        case MULTIPATH:
        case STANDARD:
          setDifferential(false);
          break;
        default:
          throw new BatfishException(
              "Invalid reachability type: " + reachabilityType.reachabilityTypeName());
      }
    }

    @JsonProperty(PROP_SRC_IPS)
    public void setSrcIps(SortedSet<IpWildcard> srcIps) {
      _reachabilitySettings.getHeaderSpace().setSrcIps(ImmutableSortedSet.copyOf(srcIps));
    }

    @JsonProperty(PROP_SRC_NATTED)
    public void setSrcNatted(Boolean srcNatted) {
      _reachabilitySettings.setSrcNatted(srcNatted);
    }

    @JsonProperty(PROP_SRC_OR_DST_IPS)
    public void setSrcOrDstIps(SortedSet<IpWildcard> srcOrDstIps) {
      _reachabilitySettings.getHeaderSpace().setSrcOrDstIps(ImmutableSortedSet.copyOf(srcOrDstIps));
    }

    @JsonProperty(PROP_SRC_OR_DST_PORTS)
    public void setSrcOrDstPorts(SortedSet<SubRange> srcOrDstPorts) {
      _reachabilitySettings
          .getHeaderSpace()
          .setSrcOrDstPorts(ImmutableSortedSet.copyOf(srcOrDstPorts));
    }

    @JsonProperty(PROP_SRC_OR_DST_PROTOCOLS)
    public void setSrcOrDstProtocols(SortedSet<Protocol> srcOrDstProtocols) {
      _reachabilitySettings
          .getHeaderSpace()
          .setSrcOrDstProtocols(ImmutableSortedSet.copyOf(srcOrDstProtocols));
    }

    @JsonProperty(PROP_SRC_PORTS)
    public void setSrcPorts(SortedSet<SubRange> srcPorts) {
      _reachabilitySettings.getHeaderSpace().setSrcPorts(ImmutableSortedSet.copyOf(srcPorts));
    }

    @JsonProperty(PROP_SRC_PROTOCOLS)
    public void setSrcProtocols(SortedSet<Protocol> srcProtocols) {
      _reachabilitySettings
          .getHeaderSpace()
          .setSrcProtocols(ImmutableSortedSet.copyOf(srcProtocols));
    }

    @JsonProperty(PROP_TRANSIT_NODES)
    public void setTransitNodes(NodesSpecifier transitNodes) {
      _reachabilitySettings.setTransitNodes(transitNodes);
    }

    @JsonProperty(PROP_USE_COMPRESSION)
    public void setUseCompression(boolean useCompression) {
      _reachabilitySettings.setUseCompression(useCompression);
    }

    @JsonProperty(PROP_SPECIALIZE)
    public void setSpecialize(boolean specialize) {
      _reachabilitySettings.setSpecialize(specialize);
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new ReachabilityAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new ReachabilityQuestion();
  }
}
