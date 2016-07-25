package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReachabilityType;
import org.batfish.datamodel.SubRange;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReachabilityQuestion extends Question {

   private static final String ACTIONS_VAR = "actions";

   private static final String DST_PORT_RANGE_VAR = "dstPortRange";

   private static final String DST_PREFIXES_VAR = "dstPrefixes";

   private static final String FINAL_NODE_REGEX_VAR = "finalNodeRegex";

   private static final String ICMP_CODE_VAR = "icmpCode";

   private static final String ICMP_TYPE_VAR = "icmpType";

   private static final String INGRESS_NODE_REGEX_VAR = "ingressNodeRegex";

   private static final String IP_PROTO_RANGE_VAR = "ipProtoRange";

   private static final String REACHABILITY_TYPE_VAR = "type";

   private static final String SRC_PORT_RANGE_VAR = "srcPortRange";

   private static final String SRC_PREFIXES_VAR = "srcPrefixes";

   private Set<ForwardingAction> _actions;

   private Set<SubRange> _dstPortRange;

   private Set<Prefix> _dstPrefixes;

   private String _finalNodeRegex;

   private int _icmpCode;

   private int _icmpType;

   private String _ingressNodeRegex;

   private Set<SubRange> _ipProtocolRange;

   private ReachabilityType _reachabilityType;

   private Set<SubRange> _srcPortRange;

   private Set<Prefix> _srcPrefixes;

   // private int _tcpFlags;

   public ReachabilityQuestion() {
      super(QuestionType.REACHABILITY);
      _actions = EnumSet.of(ForwardingAction.ACCEPT);
      _dstPortRange = new TreeSet<SubRange>();
      _dstPrefixes = new TreeSet<Prefix>();
      _finalNodeRegex = ".*";
      _icmpCode = IcmpCode.UNSET;
      _icmpType = IcmpType.UNSET;
      _ingressNodeRegex = ".*";
      _ipProtocolRange = new TreeSet<SubRange>();
      _reachabilityType = ReachabilityType.STANDARD;
      _srcPortRange = new TreeSet<SubRange>();
      _srcPrefixes = new TreeSet<Prefix>();
   }

   @JsonProperty(ACTIONS_VAR)
   public Set<ForwardingAction> getActions() {
      return _actions;
   }

   @Override
   public boolean getDataPlane() {
      return true;
   }

   @JsonProperty(DST_PORT_RANGE_VAR)
   public Set<SubRange> getDstPortRange() {
      return _dstPortRange;
   }

   @JsonProperty(DST_PREFIXES_VAR)
   public Set<Prefix> getDstPrefixes() {
      return _dstPrefixes;
   }

   @JsonProperty(FINAL_NODE_REGEX_VAR)
   public String getFinalNodeRegex() {
      return _finalNodeRegex;
   }

   @JsonProperty(ICMP_CODE_VAR)
   public int getIcmpCode() {
      return _icmpCode;
   }

   @JsonProperty(ICMP_TYPE_VAR)
   public int getIcmpType() {
      return _icmpType;
   }

   @JsonProperty(INGRESS_NODE_REGEX_VAR)
   public String getIngressNodeRegex() {
      return _ingressNodeRegex;
   }

   @JsonProperty(IP_PROTO_RANGE_VAR)
   public Set<SubRange> getIpProtocolRange() {
      return _ipProtocolRange;
   }

   @JsonProperty(REACHABILITY_TYPE_VAR)
   public ReachabilityType getReachabilityType() {
      return _reachabilityType;
   }

   @JsonProperty(SRC_PORT_RANGE_VAR)
   public Set<SubRange> getSrcPortRange() {
      return _srcPortRange;
   }

   @JsonProperty(SRC_PREFIXES_VAR)
   public Set<Prefix> getSrcPrefixes() {
      return _srcPrefixes;
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
         if (_dstPrefixes != null && _dstPrefixes.size() != 0) {
            retString += String.format(" | dstPrefixes=%s", _dstPrefixes);
         }
         if (_dstPortRange != null && _dstPortRange.size() != 0) {
            retString += String.format(" | dstPorts=%s", _dstPortRange);
         }
         if (_srcPrefixes != null && _srcPrefixes.size() != 0) {
            retString += String.format(" | srcPrefixes=%s", _srcPrefixes);
         }
         if (_srcPortRange != null && _srcPortRange.size() != 0) {
            retString += String.format(" | srcPorts=%s", _srcPortRange);
         }
         if (_ipProtocolRange != null && _ipProtocolRange.size() != 0) {
            retString += String.format(" | ipProtocols=%s",
                  _ipProtocolRange.toString());
         }
         if (_icmpCode != IcmpCode.UNSET) {
            retString += String.format(" | icmpCode=%s", _icmpCode);
         }
         if (_icmpType != IcmpType.UNSET) {
            retString += String.format(" | icmpType=%s", _icmpType);
         }
         return retString;
      }
      catch (Exception e) {
         try {
            return "Pretty printing failed. Printing Json\n" + toJsonString();
         }
         catch (JsonProcessingException e1) {
            throw new BatfishException("Both pretty and json printing failed\n");
         }
      }
   }

   @JsonProperty(ACTIONS_VAR)
   public void setActions(Set<ForwardingAction> actionSet) {
      _actions = actionSet;
   }

   @JsonProperty(DST_PORT_RANGE_VAR)
   public void setDstPortRange(Set<SubRange> rangeSet) {
      _dstPortRange = rangeSet;
   }

   @JsonProperty(DST_PREFIXES_VAR)
   public void setDstPrefixes(Set<Prefix> prefixSet) {
      _dstPrefixes = prefixSet;
   }

   @JsonProperty(FINAL_NODE_REGEX_VAR)
   public void setFinalNodeRegex(String regex) {
      _finalNodeRegex = regex;
   }

   @JsonProperty(ICMP_CODE_VAR)
   public void setIcmpCode(int icmpCode) {
      _icmpCode = icmpCode;
   }

   @JsonProperty(ICMP_TYPE_VAR)
   public void setIcmpType(int icmpType) {
      _icmpType = icmpType;
   }

   @JsonProperty(INGRESS_NODE_REGEX_VAR)
   public void setIngressNodeRegex(String regex) {
      _ingressNodeRegex = regex;
   }

   @JsonProperty(IP_PROTO_RANGE_VAR)
   public void setIpProtocolRange(Set<SubRange> rangeSet) {
      _ipProtocolRange = rangeSet;
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
               setActions(new ObjectMapper().<Set<ForwardingAction>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<ForwardingAction>>() {
                     }));
               break;
            case DST_PORT_RANGE_VAR:
               setDstPortRange(new ObjectMapper().<Set<SubRange>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<SubRange>>() {
                     }));
               break;
            case DST_PREFIXES_VAR:
               setDstPrefixes(new ObjectMapper().<Set<Prefix>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<Prefix>>() {
                     }));
               break;
            case FINAL_NODE_REGEX_VAR:
               setFinalNodeRegex(parameters.getString(paramKey));
               break;
            case ICMP_CODE_VAR:
               setIcmpCode(parameters.getInt(paramKey));
               break;
            case ICMP_TYPE_VAR:
               setIcmpType(parameters.getInt(paramKey));
               break;
            case INGRESS_NODE_REGEX_VAR:
               setIngressNodeRegex(parameters.getString(paramKey));
               break;
            case IP_PROTO_RANGE_VAR:
               setIpProtocolRange(new ObjectMapper().<Set<SubRange>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<SubRange>>() {
                     }));
               break;
            case REACHABILITY_TYPE_VAR:
               setReachabilityType(ReachabilityType.fromName(parameters
                     .getString(paramKey)));
               break;
            case SRC_PORT_RANGE_VAR:
               setSrcPortRange(new ObjectMapper().<Set<SubRange>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<SubRange>>() {
                     }));
               break;
            case SRC_PREFIXES_VAR:
               setSrcPrefixes(new ObjectMapper().<Set<Prefix>> readValue(
                     parameters.getString(paramKey),
                     new TypeReference<Set<Prefix>>() {
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

   @JsonProperty(SRC_PORT_RANGE_VAR)
   public void setSrcPortRange(Set<SubRange> rangeSet) {
      _srcPortRange = rangeSet;
   }

   @JsonProperty(SRC_PREFIXES_VAR)
   public void setSrcPrefixes(Set<Prefix> prefixSet) {
      _srcPrefixes = prefixSet;
   }

}
