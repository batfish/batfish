package org.batfish.question;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.IReachabilityQuestion;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

   public static class ReachabilityQuestion extends Question
         implements IReachabilityQuestion {

      private static final String ACTIONS_VAR = "actions";

      private static final String DEFAULT_FINAL_NODE_REGEX = ".*";

      private static final String DEFAULT_INGRESS_NODE_REGEX = ".*";

      private static final String DEFAULT_NOT_FINAL_NODE_REGEX = "";

      private static final String DEFAULT_NOT_INGRESS_NODE_REGEX = "";

      private static final String DST_IPS_VAR = "dstIps";

      private static final String DST_PORTS_VAR = "dstPorts";

      private static final String FINAL_NODE_REGEX_VAR = "finalNodeRegex";

      private static final String FRAGMENT_OFFSETS_VAR = "fragmentOffsets";

      private static final String ICMP_CODES_VAR = "icmpCodes";

      private static final String ICMP_TYPES_VAR = "icmpTypes";

      private static final String INGRESS_NODE_REGEX_VAR = "ingressNodeRegex";

      private static final String IP_PROTOCOLS_VAR = "ipProtocols";

      private static final String NEGATE_HEADER_VAR = "negateHeader";

      private static final String NOT_DST_IPS_VAR = "notDstIps";

      private static final String NOT_DST_PORTS_VAR = "notDstPors";

      private static final String NOT_FINAL_NODE_REGEX_VAR = "notFinalNodeRegex";

      private static final String NOT_FRAGMENT_OFFSETS_VAR = "notFragmentOffsets";

      private static final String NOT_ICMP_CODE_VAR = "notIcmpCodes";

      private static final String NOT_ICMP_TYPE_VAR = "notIcmpTypes";

      private static final String NOT_INGRESS_NODE_REGEX_VAR = "notIngressNodeRegex";

      private static final String NOT_IP_PROTOCOLS_VAR = "notIpProtocols";

      private static final String NOT_SRC_IPS_VAR = "notSrcIps";

      private static final String NOT_SRC_PORTS_VAR = "notSrcPorts";

      private static final String REACHABILITY_TYPE_VAR = "type";

      private static final String SRC_IPS_VAR = "srcIps";

      private static final String SRC_OR_DST_IPS_VAR = "srcOrDstIps";

      private static final String SRC_OR_DST_PORTS_VAR = "srcOrDstPorts";

      private static final String SRC_PORTS_VAR = "srcPorts";

      private Set<ForwardingAction> _actions;

      private String _finalNodeRegex;

      private final HeaderSpace _headerSpace;

      private String _ingressNodeRegex;

      private String _notFinalNodeRegex;

      private String _notIngressNodeRegex;

      private ReachabilityType _reachabilityType;

      public ReachabilityQuestion() {
         _actions = EnumSet.of(ForwardingAction.ACCEPT);
         _finalNodeRegex = DEFAULT_FINAL_NODE_REGEX;
         _headerSpace = new HeaderSpace();
         _ingressNodeRegex = DEFAULT_INGRESS_NODE_REGEX;
         _reachabilityType = ReachabilityType.STANDARD;
         _notFinalNodeRegex = DEFAULT_NOT_FINAL_NODE_REGEX;
         _notIngressNodeRegex = DEFAULT_NOT_INGRESS_NODE_REGEX;
      }

      @JsonProperty(ACTIONS_VAR)
      public Set<ForwardingAction> getActions() {
         return _actions;
      }

      @Override
      public boolean getDataPlane() {
         return true;
      }

      @JsonProperty(DST_IPS_VAR)
      public Set<IpWildcard> getDstIps() {
         return _headerSpace.getDstIps();
      }

      @JsonProperty(DST_PORTS_VAR)
      public Set<SubRange> getDstPorts() {
         return _headerSpace.getDstPorts();
      }

      @JsonProperty(FINAL_NODE_REGEX_VAR)
      public String getFinalNodeRegex() {
         return _finalNodeRegex;
      }

      @JsonProperty(FRAGMENT_OFFSETS_VAR)
      public Set<SubRange> getFragmentOffsets() {
         return _headerSpace.getFragmentOffsets();
      }

      @JsonIgnore
      public HeaderSpace getHeaderSpace() {
         return _headerSpace;
      }

      @JsonProperty(ICMP_CODES_VAR)
      public Set<SubRange> getIcmpCodes() {
         return _headerSpace.getIcmpCodes();
      }

      @JsonProperty(ICMP_TYPES_VAR)
      public Set<SubRange> getIcmpTypes() {
         return _headerSpace.getIcmpTypes();
      }

      @JsonProperty(INGRESS_NODE_REGEX_VAR)
      public String getIngressNodeRegex() {
         return _ingressNodeRegex;
      }

      @JsonProperty(IP_PROTOCOLS_VAR)
      public Set<IpProtocol> getIpProtocols() {
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
      public Set<IpWildcard> getNotDstIps() {
         return _headerSpace.getNotDstIps();
      }

      @JsonProperty(NOT_DST_PORTS_VAR)
      public Set<SubRange> getNotDstPorts() {
         return _headerSpace.getNotDstPorts();
      }

      @JsonProperty(NOT_FINAL_NODE_REGEX_VAR)
      public String getNotFinalNodeRegex() {
         return _notFinalNodeRegex;
      }

      @JsonProperty(NOT_FRAGMENT_OFFSETS_VAR)
      private Set<SubRange> getNotFragmentOffsets() {
         return _headerSpace.getNotFragmentOffsets();
      }

      @JsonProperty(NOT_ICMP_CODE_VAR)
      public Set<SubRange> getNotIcmpCodes() {
         return _headerSpace.getNotIcmpCodes();
      }

      @JsonProperty(NOT_ICMP_TYPE_VAR)
      public Set<SubRange> getNotIcmpTypes() {
         return _headerSpace.getNotIcmpTypes();
      }

      @JsonProperty(NOT_INGRESS_NODE_REGEX_VAR)
      public String getNotIngressNodeRegex() {
         return _notIngressNodeRegex;
      }

      @JsonProperty(NOT_IP_PROTOCOLS_VAR)
      public Set<IpProtocol> getNotIpProtocols() {
         return _headerSpace.getNotIpProtocols();
      }

      @JsonProperty(NOT_SRC_IPS_VAR)
      public Set<IpWildcard> getNotSrcIps() {
         return _headerSpace.getNotSrcIps();
      }

      @JsonProperty(NOT_SRC_PORTS_VAR)
      public Set<SubRange> getNotSrcPorts() {
         return _headerSpace.getNotSrcPorts();
      }

      @JsonProperty(REACHABILITY_TYPE_VAR)
      public ReachabilityType getReachabilityType() {
         return _reachabilityType;
      }

      @JsonProperty(SRC_IPS_VAR)
      public Set<IpWildcard> getSrcIps() {
         return _headerSpace.getSrcIps();
      }

      @JsonProperty(SRC_OR_DST_IPS_VAR)
      public Set<IpWildcard> getSrcOrDstIps() {
         return _headerSpace.getSrcOrDstIps();
      }

      @JsonProperty(SRC_OR_DST_PORTS_VAR)
      public Set<SubRange> getSrcOrDstPorts() {
         return _headerSpace.getSrcOrDstPorts();
      }

      @JsonProperty(SRC_PORTS_VAR)
      public Set<SubRange> getSrcPorts() {
         return _headerSpace.getSrcPorts();
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
            if (getDstPorts() != null && getDstPorts().size() != 0) {
               retString += String.format(" | dstPorts=%s", getDstPorts());
            }
            if (getDstIps() != null && getDstIps().size() != 0) {
               retString += String.format(" | dstIps=%s", getDstIps());
            }
            if (!_finalNodeRegex.equals(DEFAULT_FINAL_NODE_REGEX)) {
               retString += String.format(" | finalNodeRegex=%s",
                     _finalNodeRegex);
            }
            if (getFragmentOffsets() != null
                  && getFragmentOffsets().size() != 0) {
               retString += String.format(" | fragmentOffsets=%s",
                     getFragmentOffsets());
            }
            if (getIcmpCodes() != null && getIcmpCodes().size() != 0) {
               retString += String.format(" | icmpCodes=%s", getIcmpCodes());
            }
            if (getIcmpTypes() != null && getIcmpTypes().size() != 0) {
               retString += String.format(" | icmpTypes=%s", getIcmpTypes());
            }
            if (!_ingressNodeRegex.equals(DEFAULT_INGRESS_NODE_REGEX)) {
               retString += String.format(" | ingressNodeRegex=%s",
                     _finalNodeRegex);
            }
            if (getIpProtocols() != null && getIpProtocols().size() != 0) {
               retString += String.format(" | ipProtocols=%s",
                     getIpProtocols().toString());
            }
            if (getSrcOrDstPorts() != null && getSrcOrDstPorts().size() != 0) {
               retString += String.format(" | srcOrDstPorts=%s",
                     getSrcOrDstPorts());
            }
            if (getSrcOrDstIps() != null && getSrcOrDstIps().size() != 0) {
               retString += String.format(" | srcOrDstIps=%s",
                     getSrcOrDstIps());
            }
            if (getSrcIps() != null && getSrcIps().size() != 0) {
               retString += String.format(" | srcIps=%s", getSrcIps());
            }
            if (getSrcPorts() != null && getSrcPorts().size() != 0) {
               retString += String.format(" | srcPorts=%s", getSrcPorts());
            }
            if (getNotDstPorts() != null && getNotDstPorts().size() != 0) {
               retString += String.format(" | notDstPorts=%s",
                     getNotDstPorts());
            }
            if (getNotDstIps() != null && getNotDstIps().size() != 0) {
               retString += String.format(" | notDstIps=%s", getNotDstIps());
            }
            if (!_notFinalNodeRegex.equals(DEFAULT_NOT_FINAL_NODE_REGEX)) {
               retString += String.format(" | notFinalNodeRegex=%s",
                     _notFinalNodeRegex);
            }
            if (getNotFragmentOffsets() != null
                  && getNotFragmentOffsets().size() != 0) {
               retString += String.format(" | notFragmentOffsets=%s",
                     getNotFragmentOffsets());
            }
            if (getNotIcmpCodes() != null && getNotIcmpCodes().size() != 0) {
               retString += String.format(" | notIcmpCodes=%s",
                     getNotIcmpCodes());
            }
            if (getNotIcmpTypes() != null && getNotIcmpTypes().size() != 0) {
               retString += String.format(" | notIcmpTypes=%s",
                     getNotIcmpTypes());
            }
            if (!_notIngressNodeRegex.equals(DEFAULT_NOT_INGRESS_NODE_REGEX)) {
               retString += String.format(" | notIngressNodeRegex=%s",
                     _notIngressNodeRegex);
            }
            if (getNotIpProtocols() != null
                  && getNotIpProtocols().size() != 0) {
               retString += String.format(" | notIpProtocols=%s",
                     getNotIpProtocols().toString());
            }
            if (getNotSrcIps() != null && getNotSrcIps().size() != 0) {
               retString += String.format(" | notSrcIps=%s", getNotSrcIps());
            }
            if (getNotSrcPorts() != null && getNotSrcPorts().size() != 0) {
               retString += String.format(" | notSrcPorts=%s",
                     getNotSrcPorts());
            }
            return retString;
         }
         catch (Exception e) {
            try {
               return "Pretty printing failed. Printing Json\n"
                     + toJsonString();
            }
            catch (JsonProcessingException e1) {
               throw new BatfishException(
                     "Both pretty and json printing failed\n");
            }
         }
      }

      @Override
      @JsonProperty(ACTIONS_VAR)
      public void setActions(Set<ForwardingAction> actionSet) {
         _actions = actionSet;
      }

      @Override
      @JsonProperty(DST_IPS_VAR)
      public void setDstIps(Set<IpWildcard> dstIps) {
         _headerSpace.setDstIps(new TreeSet<>(dstIps));
      }

      @Override
      @JsonProperty(DST_PORTS_VAR)
      public void setDstPorts(Set<SubRange> dstPorts) {
         _headerSpace.setDstPorts(new TreeSet<>(dstPorts));
      }

      @JsonProperty(FINAL_NODE_REGEX_VAR)
      public void setFinalNodeRegex(String regex) {
         _finalNodeRegex = regex;
      }

      @JsonProperty(ICMP_CODES_VAR)
      public void setIcmpCodes(Set<SubRange> icmpCodes) {
         _headerSpace.setIcmpCodes(new TreeSet<>(icmpCodes));
      }

      @JsonProperty(ICMP_TYPES_VAR)
      public void setIcmpTypes(Set<SubRange> icmpTypes) {
         _headerSpace.setIcmpTypes(new TreeSet<>(icmpTypes));
      }

      @Override
      @JsonProperty(INGRESS_NODE_REGEX_VAR)
      public void setIngressNodeRegex(String regex) {
         _ingressNodeRegex = regex;
      }

      @Override
      @JsonProperty(IP_PROTOCOLS_VAR)
      public void setIpProtocols(Set<IpProtocol> ipProtocols) {
         _headerSpace.setIpProtocols(ipProtocols);
      }

      @Override
      public void setJsonParameters(JSONObject parameters) {
         super.setJsonParameters(parameters);

         Iterator<?> paramKeys = parameters.keys();

         while (paramKeys.hasNext()) {
            String paramKey = (String) paramKeys.next();
            if (isBaseParamKey(paramKey)) {
               continue;
            }

            try {
               switch (paramKey) {
               case ACTIONS_VAR:
                  setActions(
                        new ObjectMapper().<Set<ForwardingAction>> readValue(
                              parameters.getString(paramKey),
                              new TypeReference<Set<ForwardingAction>>() {
                              }));
                  break;
               case DST_IPS_VAR:
                  setDstIps(new ObjectMapper().<Set<IpWildcard>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<IpWildcard>>() {
                        }));
                  break;
               case DST_PORTS_VAR:
                  setDstPorts(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case FINAL_NODE_REGEX_VAR:
                  setFinalNodeRegex(parameters.getString(paramKey));
                  break;
               case ICMP_CODES_VAR:
                  setIcmpCodes(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case ICMP_TYPES_VAR:
                  setIcmpTypes(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case INGRESS_NODE_REGEX_VAR:
                  setIngressNodeRegex(parameters.getString(paramKey));
                  break;
               case IP_PROTOCOLS_VAR:
                  setIpProtocols(new ObjectMapper().<Set<IpProtocol>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<IpProtocol>>() {
                        }));
                  break;
               case NEGATE_HEADER_VAR:
                  setNegateHeader(parameters.getBoolean(paramKey));
                  break;
               case REACHABILITY_TYPE_VAR:
                  setReachabilityType(ReachabilityType
                        .fromName(parameters.getString(paramKey)));
                  break;
               case SRC_IPS_VAR:
                  setSrcIps(new ObjectMapper().<Set<IpWildcard>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<IpWildcard>>() {
                        }));
                  break;
               case SRC_OR_DST_IPS_VAR:
                  setSrcOrDstIps(new ObjectMapper().<Set<IpWildcard>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<IpWildcard>>() {
                        }));
                  break;
               case SRC_OR_DST_PORTS_VAR:
                  setSrcOrDstPorts(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case SRC_PORTS_VAR:
                  setSrcPorts(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case NOT_DST_IPS_VAR:
                  setNotDstIps(new ObjectMapper().<Set<IpWildcard>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<IpWildcard>>() {
                        }));
                  break;
               case NOT_DST_PORTS_VAR:
                  setNotDstPorts(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case NOT_FINAL_NODE_REGEX_VAR:
                  setNotFinalNodeRegex(parameters.getString(paramKey));
                  break;
               case NOT_ICMP_CODE_VAR:
                  setNotIcmpCodes(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));

                  break;
               case NOT_ICMP_TYPE_VAR:
                  setNotIcmpTypes(new ObjectMapper().<Set<SubRange>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<SubRange>>() {
                        }));
                  break;
               case NOT_INGRESS_NODE_REGEX_VAR:
                  setNotIngressNodeRegex(parameters.getString(paramKey));
                  break;
               case NOT_IP_PROTOCOLS_VAR:
                  setNotIpProtocols(
                        new ObjectMapper().<Set<IpProtocol>> readValue(
                              parameters.getString(paramKey),
                              new TypeReference<Set<IpProtocol>>() {
                              }));
                  break;
               case NOT_SRC_IPS_VAR:
                  setNotSrcIps(new ObjectMapper().<Set<IpWildcard>> readValue(
                        parameters.getString(paramKey),
                        new TypeReference<Set<IpWildcard>>() {
                        }));
                  break;
               case NOT_SRC_PORTS_VAR:
                  setNotSrcPortRange(
                        new ObjectMapper().<Set<SubRange>> readValue(
                              parameters.getString(paramKey),
                              new TypeReference<Set<SubRange>>() {
                              }));
                  break;
               default:
                  throw new BatfishException(
                        "Unknown key in ReachabilityQuestion: " + paramKey);
               }
            }
            catch (JSONException | IOException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      @JsonProperty(NEGATE_HEADER_VAR)
      public void setNegateHeader(boolean negateHeader) {
         _headerSpace.setNegate(negateHeader);
      }

      @JsonProperty(NOT_DST_IPS_VAR)
      public void setNotDstIps(Set<IpWildcard> notDstIps) {
         _headerSpace.setNotDstIps(new TreeSet<>(notDstIps));
      }

      @Override
      @JsonProperty(NOT_DST_PORTS_VAR)
      public void setNotDstPorts(Set<SubRange> notDstPorts) {
         _headerSpace.setNotDstPorts(new TreeSet<>(notDstPorts));
      }

      @JsonProperty(NOT_FINAL_NODE_REGEX_VAR)
      public void setNotFinalNodeRegex(String notFinalNodeRegex) {
         _notFinalNodeRegex = notFinalNodeRegex;
      }

      @JsonProperty(NOT_ICMP_CODE_VAR)
      public void setNotIcmpCodes(Set<SubRange> notIcmpCodes) {
         _headerSpace.setNotIcmpCodes(new TreeSet<>(notIcmpCodes));
      }

      @JsonProperty(NOT_ICMP_TYPE_VAR)
      public void setNotIcmpTypes(Set<SubRange> notIcmpType) {
         _headerSpace.setNotIcmpTypes(new TreeSet<>(notIcmpType));
      }

      @JsonProperty(NOT_INGRESS_NODE_REGEX_VAR)
      public void setNotIngressNodeRegex(String notIngressNodeRegex) {
         _notIngressNodeRegex = notIngressNodeRegex;
      }

      @Override
      @JsonProperty(NOT_IP_PROTOCOLS_VAR)
      public void setNotIpProtocols(Set<IpProtocol> notIpProtocols) {
         _headerSpace.setNotIpProtocols(notIpProtocols);
      }

      @JsonProperty(NOT_SRC_IPS_VAR)
      public void setNotSrcIps(Set<IpWildcard> notSrcIps) {
         _headerSpace.setNotSrcIps(new TreeSet<>(notSrcIps));
      }

      @JsonProperty(NOT_SRC_PORTS_VAR)
      public void setNotSrcPortRange(Set<SubRange> notSrcPorts) {
         _headerSpace.setNotSrcPorts(new TreeSet<>(notSrcPorts));
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
      public void setSrcIps(Set<IpWildcard> srcIps) {
         _headerSpace.setSrcIps(new TreeSet<>(srcIps));
      }

      @JsonProperty(SRC_OR_DST_IPS_VAR)
      public void setSrcOrDstIps(Set<IpWildcard> srcOrDstIps) {
         _headerSpace.setSrcOrDstIps(new TreeSet<>(srcOrDstIps));
      }

      @JsonProperty(SRC_OR_DST_PORTS_VAR)
      public void setSrcOrDstPorts(Set<SubRange> srcOrDstPorts) {
         _headerSpace.setSrcOrDstPorts(new TreeSet<>(srcOrDstPorts));
      }

      @JsonProperty(SRC_PORTS_VAR)
      public void setSrcPorts(Set<SubRange> srcPorts) {
         _headerSpace.setSrcPorts(new TreeSet<>(srcPorts));
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
