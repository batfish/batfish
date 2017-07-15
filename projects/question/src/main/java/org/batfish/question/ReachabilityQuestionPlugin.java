package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.IReachabilityQuestion;
import org.batfish.datamodel.questions.Question;

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
                  "Unsupported non-differential reachabilty type: "
                        + type.reachabilityTypeName());
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
                  "Unsupported differential reachabilty type: "
                        + type.reachabilityTypeName());
         }
      }

      private AnswerElement multipath(ReachabilityQuestion question) {
         return _batfish.multipath(question.getHeaderSpace());
      }

      private AnswerElement pathDiff(ReachabilityQuestion question) {
         return _batfish.pathDiff(question.getHeaderSpace());
      }

      private AnswerElement reducedReachability(ReachabilityQuestion question) {
         return _batfish.reducedReachability(question.getHeaderSpace());
      }

      private AnswerElement standard(ReachabilityQuestion question) {
         return _batfish.standard(question.getHeaderSpace(),
               question.getActions(), question.getIngressNodeRegex(),
               question.getNotIngressNodeRegex(), question.getFinalNodeRegex(),
               question.getNotFinalNodeRegex());
      }
   }

   // <question_page_comment>

   /**
    * Details coming.
    * <p>
    * More details coming.
    *
    * @type Reachability dataplane
    *
    * @param DetailsComing
    *           Details coming.
    *
    * @example bf_answer("Reachability", dstIps=["2.128.0.101"], dstPorts=[53],
    *ipProtocols=["UDP"], actions=["drop"]) Finds all (starting node,
    *          packet header) combinations that cannot reach (action=drop) the
    *          2.128.0.101 using a DNS (UDP on port 53) packet.
    * @example bf_answer_type("Reachability", actions=["ACCEPT"],
    *dstIps=["2.128.1.101"], notDstPorts=[22], notIpProtocols=["TCP"])
    *          Finds all (starting node, packet header) combinations that can
    *          reach (action=drop) 2.128.1.101 using non-SSH packets.
    */
   public static class ReachabilityQuestion extends Question
         implements IReachabilityQuestion {

      private static final String ACTIONS_VAR = "actions";

      private static final String DEFAULT_FINAL_NODE_REGEX = ".*";

      private static final String DEFAULT_INGRESS_NODE_REGEX = ".*";

      private static final String DEFAULT_NOT_FINAL_NODE_REGEX = "";

      private static final String DEFAULT_NOT_INGRESS_NODE_REGEX = "";

      private static final String DST_IPS_VAR = "dstIps";

      private static final String DST_PORTS_VAR = "dstPorts";

      private static final String DST_PROTOCOLS_VAR = "dstProtocols";

      private static final String FINAL_NODE_REGEX_VAR = "finalNodeRegex";

      private static final String FRAGMENT_OFFSETS_VAR = "fragmentOffsets";

      private static final String ICMP_CODES_VAR = "icmpCodes";

      private static final String ICMP_TYPES_VAR = "icmpTypes";

      private static final String INGRESS_NODE_REGEX_VAR = "ingressNodeRegex";

      private static final String IP_PROTOCOLS_VAR = "ipProtocols";

      private static final String NEGATE_HEADER_VAR = "negateHeader";

      private static final String NOT_DST_IPS_VAR = "notDstIps";

      private static final String NOT_DST_PORTS_VAR = "notDstPorts";

      private static final String NOT_DST_PROTOCOLS_VAR = "notDstProtocols";

      private static final String NOT_FINAL_NODE_REGEX_VAR = "notFinalNodeRegex";

      private static final String NOT_FRAGMENT_OFFSETS_VAR = "notFragmentOffsets";

      private static final String NOT_ICMP_CODES_VAR = "notIcmpCodes";

      private static final String NOT_ICMP_TYPES_VAR = "notIcmpTypes";

      private static final String NOT_INGRESS_NODE_REGEX_VAR = "notIngressNodeRegex";

      private static final String NOT_IP_PROTOCOLS_VAR = "notIpProtocols";

      private static final String NOT_PACKET_LENGTHS_VAR = "notPacketLengths";

      private static final String NOT_SRC_IPS_VAR = "notSrcIps";

      private static final String NOT_SRC_PORTS_VAR = "notSrcPorts";

      private static final String NOT_SRC_PROTOCOLS_VAR = "notSrcProtocols";

      private static final String PACKET_LENGTHS_VAR = "packetLengths";

      private static final String REACHABILITY_TYPE_VAR = "type";

      private static final String SRC_IPS_VAR = "srcIps";

      private static final String SRC_OR_DST_IPS_VAR = "srcOrDstIps";

      private static final String SRC_OR_DST_PORTS_VAR = "srcOrDstPorts";

      private static final String SRC_OR_DST_PROTOCOLS_VAR = "srcOrDstProtocols";

      private static final String SRC_PORTS_VAR = "srcPorts";

      private static final String SRC_PROTOCOLS_VAR = "srcProtocols";

      private SortedSet<ForwardingAction> _actions;

      private String _finalNodeRegex;

      private final HeaderSpace _headerSpace;

      private String _ingressNodeRegex;

      private String _notFinalNodeRegex;

      private String _notIngressNodeRegex;

      private ReachabilityType _reachabilityType;

      public ReachabilityQuestion() {
         _actions = new TreeSet<>(
               Collections.singleton(ForwardingAction.ACCEPT));
         _finalNodeRegex = DEFAULT_FINAL_NODE_REGEX;
         _headerSpace = new HeaderSpace();
         _ingressNodeRegex = DEFAULT_INGRESS_NODE_REGEX;
         _reachabilityType = ReachabilityType.STANDARD;
         _notFinalNodeRegex = DEFAULT_NOT_FINAL_NODE_REGEX;
         _notIngressNodeRegex = DEFAULT_NOT_INGRESS_NODE_REGEX;
      }

      @JsonProperty(ACTIONS_VAR)
      public SortedSet<ForwardingAction> getActions() {
         return _actions;
      }

      @Override
      public boolean getDataPlane() {
         return true;
      }

      @JsonProperty(DST_IPS_VAR)
      public SortedSet<IpWildcard> getDstIps() {
         return _headerSpace.getDstIps();
      }

      @JsonProperty(DST_PORTS_VAR)
      public SortedSet<SubRange> getDstPorts() {
         return _headerSpace.getDstPorts();
      }

      @JsonProperty(DST_PROTOCOLS_VAR)
      public SortedSet<Protocol> getDstProtocols() {
         return _headerSpace.getDstProtocols();
      }

      @JsonProperty(FINAL_NODE_REGEX_VAR)
      public String getFinalNodeRegex() {
         return _finalNodeRegex;
      }

      @JsonProperty(FRAGMENT_OFFSETS_VAR)
      public SortedSet<SubRange> getFragmentOffsets() {
         return _headerSpace.getFragmentOffsets();
      }

      @JsonIgnore
      public HeaderSpace getHeaderSpace() {
         return _headerSpace;
      }

      @JsonProperty(ICMP_CODES_VAR)
      public SortedSet<SubRange> getIcmpCodes() {
         return _headerSpace.getIcmpCodes();
      }

      @JsonProperty(ICMP_TYPES_VAR)
      public SortedSet<SubRange> getIcmpTypes() {
         return _headerSpace.getIcmpTypes();
      }

      @JsonProperty(INGRESS_NODE_REGEX_VAR)
      public String getIngressNodeRegex() {
         return _ingressNodeRegex;
      }

      @JsonProperty(IP_PROTOCOLS_VAR)
      public SortedSet<IpProtocol> getIpProtocols() {
         return _headerSpace.getIpProtocols();
      }

      @Override
      public String getName() {
         return NAME;
      }

      @JsonProperty(NEGATE_HEADER_VAR)
      public boolean getNegateHeader() {
         return _headerSpace.getNegate();
      }

      @JsonProperty(NOT_DST_IPS_VAR)
      public SortedSet<IpWildcard> getNotDstIps() {
         return _headerSpace.getNotDstIps();
      }

      @JsonProperty(NOT_DST_PORTS_VAR)
      public SortedSet<SubRange> getNotDstPorts() {
         return _headerSpace.getNotDstPorts();
      }

      @JsonProperty(NOT_DST_PROTOCOLS_VAR)
      public SortedSet<Protocol> getNotDstProtocols() {
         return _headerSpace.getNotDstProtocols();
      }

      @JsonProperty(NOT_FINAL_NODE_REGEX_VAR)
      public String getNotFinalNodeRegex() {
         return _notFinalNodeRegex;
      }

      @JsonProperty(NOT_FRAGMENT_OFFSETS_VAR)
      private SortedSet<SubRange> getNotFragmentOffsets() {
         return _headerSpace.getNotFragmentOffsets();
      }

      @JsonProperty(NOT_ICMP_CODES_VAR)
      public SortedSet<SubRange> getNotIcmpCodes() {
         return _headerSpace.getNotIcmpCodes();
      }

      @JsonProperty(NOT_ICMP_TYPES_VAR)
      public SortedSet<SubRange> getNotIcmpTypes() {
         return _headerSpace.getNotIcmpTypes();
      }

      @JsonProperty(NOT_INGRESS_NODE_REGEX_VAR)
      public String getNotIngressNodeRegex() {
         return _notIngressNodeRegex;
      }

      @JsonProperty(NOT_IP_PROTOCOLS_VAR)
      public SortedSet<IpProtocol> getNotIpProtocols() {
         return _headerSpace.getNotIpProtocols();
      }

      @JsonProperty(NOT_PACKET_LENGTHS_VAR)
      public SortedSet<SubRange> getNotPacketLengths() {
         return _headerSpace.getNotPacketLengths();
      }

      @JsonProperty(NOT_SRC_IPS_VAR)
      public SortedSet<IpWildcard> getNotSrcIps() {
         return _headerSpace.getNotSrcIps();
      }

      @JsonProperty(NOT_SRC_PORTS_VAR)
      public SortedSet<SubRange> getNotSrcPorts() {
         return _headerSpace.getNotSrcPorts();
      }

      @JsonProperty(NOT_SRC_PROTOCOLS_VAR)
      public SortedSet<Protocol> getNotSrcProtocols() {
         return _headerSpace.getNotSrcProtocols();
      }

      @JsonProperty(PACKET_LENGTHS_VAR)
      public SortedSet<SubRange> getPacketLengths() {
         return _headerSpace.getPacketLengths();
      }

      @JsonProperty(REACHABILITY_TYPE_VAR)
      public ReachabilityType getReachabilityType() {
         return _reachabilityType;
      }

      @JsonProperty(SRC_IPS_VAR)
      public SortedSet<IpWildcard> getSrcIps() {
         return _headerSpace.getSrcIps();
      }

      @JsonProperty(SRC_OR_DST_IPS_VAR)
      public SortedSet<IpWildcard> getSrcOrDstIps() {
         return _headerSpace.getSrcOrDstIps();
      }

      @JsonProperty(SRC_OR_DST_PORTS_VAR)
      public SortedSet<SubRange> getSrcOrDstPorts() {
         return _headerSpace.getSrcOrDstPorts();
      }

      @JsonProperty(SRC_OR_DST_PROTOCOLS_VAR)
      public SortedSet<Protocol> getSrcOrDstProtocols() {
         return _headerSpace.getSrcOrDstProtocols();
      }

      @JsonProperty(SRC_PORTS_VAR)
      public SortedSet<SubRange> getSrcPorts() {
         return _headerSpace.getSrcPorts();
      }

      @JsonProperty(SRC_PROTOCOLS_VAR)
      public SortedSet<Protocol> getSrcProtocols() {
         return _headerSpace.getSrcProtocols();
      }

      @Override
      public boolean getTraffic() {
         return true;
      }

      @Override
      public String prettyPrint() {
         try {
            String retString = String.format("reachability %sactions=%s",
                  prettyPrintBase(), _actions.toString());
            // we only print "interesting" values
            if (_reachabilityType != ReachabilityType.STANDARD) {
               retString += String.format(" | %s=%s", REACHABILITY_TYPE_VAR,
                     _reachabilityType);
            }
            if (getNegateHeader()) {
               retString += " | negateHeader=true";
            }
            if (getDstIps() != null && !getDstIps().isEmpty()) {
               retString += String.format(" | %s=%s", DST_IPS_VAR, getDstIps());
            }
            if (getDstPorts() != null && !getDstPorts().isEmpty()) {
               retString += String.format(" | %s=%s", DST_PORTS_VAR,
                     getDstPorts());
            }
            if (getDstProtocols() != null && !getDstProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", DST_PROTOCOLS_VAR,
                     getDstProtocols());
            }
            if (!_finalNodeRegex.equals(DEFAULT_FINAL_NODE_REGEX)) {
               retString += String.format(" | %s=%s", FINAL_NODE_REGEX_VAR,
                     _finalNodeRegex);
            }
            if (getFragmentOffsets() != null
                  && !getFragmentOffsets().isEmpty()) {
               retString += String.format(" | %s=%s", FRAGMENT_OFFSETS_VAR,
                     getFragmentOffsets());
            }
            if (getIcmpCodes() != null && !getIcmpCodes().isEmpty()) {
               retString += String.format(" | %s=%s", ICMP_CODES_VAR,
                     getIcmpCodes());
            }
            if (getIcmpTypes() != null && !getIcmpTypes().isEmpty()) {
               retString += String.format(" | %s=%s", ICMP_TYPES_VAR,
                     getIcmpTypes());
            }
            if (!_ingressNodeRegex.equals(DEFAULT_INGRESS_NODE_REGEX)) {
               retString += String.format(" | %s=%s", INGRESS_NODE_REGEX_VAR,
                     _ingressNodeRegex);
            }
            if (getIpProtocols() != null && !getIpProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", IP_PROTOCOLS_VAR,
                     getIpProtocols().toString());
            }
            if (getPacketLengths() != null && !getPacketLengths().isEmpty()) {
               retString += String.format(" | %s=%s", PACKET_LENGTHS_VAR,
                     getPacketLengths().toString());
            }
            if (getSrcIps() != null && !getSrcIps().isEmpty()) {
               retString += String.format(" | %s=%s", SRC_IPS_VAR, getSrcIps());
            }
            if (getSrcPorts() != null && !getSrcPorts().isEmpty()) {
               retString += String.format(" | %s=%s", SRC_PORTS_VAR,
                     getSrcPorts());
            }
            if (getSrcProtocols() != null && !getSrcProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", SRC_PROTOCOLS_VAR,
                     getSrcProtocols());
            }
            if (getSrcOrDstIps() != null && !getSrcOrDstIps().isEmpty()) {
               retString += String.format(" | %s=%s", SRC_OR_DST_IPS_VAR,
                     getSrcOrDstIps());
            }
            if (getSrcOrDstPorts() != null && !getSrcOrDstPorts().isEmpty()) {
               retString += String.format(" | %s=%s", SRC_OR_DST_PORTS_VAR,
                     getSrcOrDstPorts());
            }
            if (getSrcOrDstProtocols() != null
                  && !getSrcOrDstProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", SRC_OR_DST_PROTOCOLS_VAR,
                     getSrcOrDstProtocols());
            }
            if (getNotDstIps() != null && !getNotDstIps().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_DST_IPS_VAR,
                     getNotDstIps());
            }
            if (getNotDstPorts() != null && !getNotDstPorts().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_DST_PORTS_VAR,
                     getNotDstPorts());
            }
            if (getNotDstProtocols() != null
                  && !getNotDstProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_DST_PROTOCOLS_VAR,
                     getNotDstProtocols());
            }
            if (!_notFinalNodeRegex.equals(DEFAULT_NOT_FINAL_NODE_REGEX)) {
               retString += String.format(" | %s=%s", NOT_FINAL_NODE_REGEX_VAR,
                     _notFinalNodeRegex);
            }
            if (getNotFragmentOffsets() != null
                  && !getNotFragmentOffsets().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_FRAGMENT_OFFSETS_VAR,
                     getNotFragmentOffsets());
            }
            if (getNotIcmpCodes() != null && !getNotIcmpCodes().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_ICMP_CODES_VAR,
                     getNotIcmpCodes());
            }
            if (getNotIcmpTypes() != null && !getNotIcmpTypes().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_ICMP_TYPES_VAR,
                     getNotIcmpTypes());
            }
            if (!_notIngressNodeRegex.equals(DEFAULT_NOT_INGRESS_NODE_REGEX)) {
               retString += String.format(" | %s=%s",
                     NOT_INGRESS_NODE_REGEX_VAR, _notIngressNodeRegex);
            }
            if (getNotIpProtocols() != null && !getNotIpProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_IP_PROTOCOLS_VAR,
                     getNotIpProtocols().toString());
            }
            if (getNotPacketLengths() != null
                  && !getNotPacketLengths().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_PACKET_LENGTHS_VAR,
                     getNotPacketLengths().toString());
            }
            if (getNotSrcIps() != null && !getNotSrcIps().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_SRC_IPS_VAR,
                     getNotSrcIps());
            }
            if (getNotSrcPorts() != null && !getNotSrcPorts().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_SRC_PORTS_VAR,
                     getNotSrcPorts());
            }
            if (getNotSrcProtocols() != null
                  && !getNotSrcProtocols().isEmpty()) {
               retString += String.format(" | %s=%s", NOT_SRC_PROTOCOLS_VAR,
                     getNotSrcProtocols());
            }
            return retString;
         }
         catch (Exception e) {
            try {
               return "Pretty printing failed. Printing Json\n"
                     + toJsonString();
            }
            catch (BatfishException e1) {
               throw new BatfishException(
                     "Both pretty and json printing failed\n");
            }
         }
      }

      @Override
      @JsonProperty(ACTIONS_VAR)
      public void setActions(SortedSet<ForwardingAction> actionSet) {
         _actions = new TreeSet<>(actionSet);
      }

      @Override
      @JsonProperty(DST_IPS_VAR)
      public void setDstIps(SortedSet<IpWildcard> dstIps) {
         _headerSpace.setDstIps(new TreeSet<>(dstIps));
      }

      @JsonProperty(DST_PORTS_VAR)
      public void setDstPorts(SortedSet<SubRange> dstPorts) {
         _headerSpace.setDstPorts(new TreeSet<>(dstPorts));
      }

      @Override
      @JsonProperty(DST_PROTOCOLS_VAR)
      public void setDstProtocols(SortedSet<Protocol> dstProtocols) {
         _headerSpace.setDstProtocols(new TreeSet<>(dstProtocols));
      }

      @JsonProperty(FINAL_NODE_REGEX_VAR)
      public void setFinalNodeRegex(String regex) {
         _finalNodeRegex = regex;
      }

      @JsonProperty(ICMP_CODES_VAR)
      public void setIcmpCodes(SortedSet<SubRange> icmpCodes) {
         _headerSpace.setIcmpCodes(new TreeSet<>(icmpCodes));
      }

      @JsonProperty(ICMP_TYPES_VAR)
      public void setIcmpTypes(SortedSet<SubRange> icmpTypes) {
         _headerSpace.setIcmpTypes(new TreeSet<>(icmpTypes));
      }

      @Override
      @JsonProperty(INGRESS_NODE_REGEX_VAR)
      public void setIngressNodeRegex(String regex) {
         _ingressNodeRegex = regex;
      }

      @JsonProperty(IP_PROTOCOLS_VAR)
      public void setIpProtocols(SortedSet<IpProtocol> ipProtocols) {
         _headerSpace.setIpProtocols(ipProtocols);
      }

      @JsonProperty(NEGATE_HEADER_VAR)
      public void setNegateHeader(boolean negateHeader) {
         _headerSpace.setNegate(negateHeader);
      }

      @JsonProperty(NOT_DST_IPS_VAR)
      public void setNotDstIps(SortedSet<IpWildcard> notDstIps) {
         _headerSpace.setNotDstIps(new TreeSet<>(notDstIps));
      }

      @JsonProperty(NOT_DST_PORTS_VAR)
      public void setNotDstPorts(SortedSet<SubRange> notDstPorts) {
         _headerSpace.setNotDstPorts(new TreeSet<>(notDstPorts));
      }

      @Override
      @JsonProperty(NOT_DST_PROTOCOLS_VAR)
      public void setNotDstProtocols(SortedSet<Protocol> notDstProtocols) {
         _headerSpace.setNotDstProtocols(new TreeSet<>(notDstProtocols));
      }

      @JsonProperty(NOT_FINAL_NODE_REGEX_VAR)
      public void setNotFinalNodeRegex(String notFinalNodeRegex) {
         _notFinalNodeRegex = notFinalNodeRegex;
      }

      @JsonProperty(NOT_ICMP_CODES_VAR)
      public void setNotIcmpCodes(SortedSet<SubRange> notIcmpCodes) {
         _headerSpace.setNotIcmpCodes(new TreeSet<>(notIcmpCodes));
      }

      @JsonProperty(NOT_ICMP_TYPES_VAR)
      public void setNotIcmpTypes(SortedSet<SubRange> notIcmpType) {
         _headerSpace.setNotIcmpTypes(new TreeSet<>(notIcmpType));
      }

      @JsonProperty(NOT_INGRESS_NODE_REGEX_VAR)
      public void setNotIngressNodeRegex(String notIngressNodeRegex) {
         _notIngressNodeRegex = notIngressNodeRegex;
      }

      @JsonProperty(NOT_IP_PROTOCOLS_VAR)
      public void setNotIpProtocols(SortedSet<IpProtocol> notIpProtocols) {
         _headerSpace.setNotIpProtocols(notIpProtocols);
      }

      @JsonProperty(NOT_PACKET_LENGTHS_VAR)
      public void setNotPacketLengths(SortedSet<SubRange> notPacketLengths) {
         _headerSpace.setNotPacketLengths(new TreeSet<>(notPacketLengths));
      }

      @JsonProperty(NOT_SRC_IPS_VAR)
      public void setNotSrcIps(SortedSet<IpWildcard> notSrcIps) {
         _headerSpace.setNotSrcIps(new TreeSet<>(notSrcIps));
      }

      @JsonProperty(NOT_SRC_PORTS_VAR)
      public void setNotSrcPortRange(SortedSet<SubRange> notSrcPorts) {
         _headerSpace.setNotSrcPorts(new TreeSet<>(notSrcPorts));
      }

      @JsonProperty(NOT_SRC_PROTOCOLS_VAR)
      public void setNotSrcProtocols(SortedSet<Protocol> notSrcProtocols) {
         _headerSpace.setNotSrcProtocols(new TreeSet<>(notSrcProtocols));
      }

      @JsonProperty(PACKET_LENGTHS_VAR)
      public void setPacketLengths(SortedSet<SubRange> packetLengths) {
         _headerSpace.setPacketLengths(new TreeSet<>(packetLengths));
      }

      @JsonProperty(REACHABILITY_TYPE_VAR)
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
            throw new BatfishException("Invalid reachability type: "
                  + reachabilityType.reachabilityTypeName());
         }
      }

      @JsonProperty(SRC_IPS_VAR)
      public void setSrcIps(SortedSet<IpWildcard> srcIps) {
         _headerSpace.setSrcIps(new TreeSet<>(srcIps));
      }

      @JsonProperty(SRC_OR_DST_IPS_VAR)
      public void setSrcOrDstIps(SortedSet<IpWildcard> srcOrDstIps) {
         _headerSpace.setSrcOrDstIps(new TreeSet<>(srcOrDstIps));
      }

      @JsonProperty(SRC_OR_DST_PORTS_VAR)
      public void setSrcOrDstPorts(SortedSet<SubRange> srcOrDstPorts) {
         _headerSpace.setSrcOrDstPorts(new TreeSet<>(srcOrDstPorts));
      }

      @JsonProperty(SRC_OR_DST_PROTOCOLS_VAR)
      public void setSrcOrDstProtocols(SortedSet<Protocol> srcOrDstProtocols) {
         _headerSpace.setSrcOrDstProtocols(new TreeSet<>(srcOrDstProtocols));
      }

      @JsonProperty(SRC_PORTS_VAR)
      public void setSrcPorts(SortedSet<SubRange> srcPorts) {
         _headerSpace.setSrcPorts(new TreeSet<>(srcPorts));
      }

      @JsonProperty(SRC_PROTOCOLS_VAR)
      public void setSrcProtocols(SortedSet<Protocol> srcProtocols) {
         _headerSpace.setSrcProtocols(new TreeSet<>(srcProtocols));
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
