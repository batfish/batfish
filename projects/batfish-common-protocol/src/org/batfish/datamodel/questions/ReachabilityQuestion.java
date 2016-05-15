package org.batfish.datamodel.questions;

import java.io.IOException;
import java.util.Iterator;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.collections.ForwardingActionSet;
import org.batfish.datamodel.collections.PrefixSet;
import org.batfish.datamodel.collections.SubRangeSet;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
   private static final String SRC_PORT_RANGE_VAR = "srcPortRange";
   private static final String SRC_PREFIXES_VAR = "srcPrefixes";

   private ForwardingActionSet _actions = new ForwardingActionSet(
         ForwardingAction.ACCEPT);
   private SubRangeSet _dstPortRange = new SubRangeSet();
   private PrefixSet _dstPrefixes = new PrefixSet();
   private String _finalNodeRegex = ".*";
   private int _icmpCode = IcmpCode.UNSET;
   private int _icmpType = IcmpType.UNSET;
   private String _ingressNodeRegex = ".*";
   private SubRangeSet _ipProtocolRange = new SubRangeSet();
   private SubRangeSet _srcPortRange = new SubRangeSet();
   private PrefixSet _srcPrefixes = new PrefixSet();

   // private int _tcpFlags;

   public ReachabilityQuestion() {
      super(QuestionType.REACHABILITY);
   }

   public ReachabilityQuestion(QuestionParameters parameters) {
      this();
      setParameters(parameters);
   }

   @JsonProperty(ACTIONS_VAR)
   public ForwardingActionSet getActions() {
      return _actions;
   }

   @Override
   @JsonIgnore
   public boolean getDataPlane() {
      return true;
   }

   @Override
   @JsonIgnore
   public boolean getDifferential() {
      return false;
   }

   @JsonProperty(DST_PORT_RANGE_VAR)
   public SubRangeSet getDstPortRange() {
      return _dstPortRange;
   }

   @JsonProperty(DST_PREFIXES_VAR)
   public PrefixSet getDstPrefixes() {
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
   public SubRangeSet getIpProtocolRange() {
      return _ipProtocolRange;
   }

   @JsonProperty(SRC_PORT_RANGE_VAR)
   public SubRangeSet getSrcPortRange() {
      return _srcPortRange;
   }

   @JsonProperty(SRC_PREFIXES_VAR)
   public PrefixSet getSrcPrefixes() {
      return _srcPrefixes;
   }

   public void setActions(ForwardingActionSet actionSet) {
      _actions = actionSet;
   }

   public void setDstPortRange(SubRangeSet rangeSet) {
      _dstPortRange = rangeSet;
   }

   public void setDstPrefixes(PrefixSet prefixSet) {
      _dstPrefixes = prefixSet;
   }

   public void setFinalNodeRegex(String regex) {
      _finalNodeRegex = regex;
   }

   public void setIcmpCode(int icmpCode) {
      _icmpCode = icmpCode;
   }

   public void setIcmpType(int icmpType) {
      _icmpType = icmpType;
   }

   public void setIngressNodeRegex(String regex) {
      _ingressNodeRegex = regex;
   }

   public void setIpProtocolRange(SubRangeSet rangeSet) {
      _ipProtocolRange = rangeSet;
   }

   @Override
   public void setJsonParameters(JSONObject parameters) {
      super.setJsonParameters(parameters);

      Iterator<?> paramKeys = parameters.keys();
      ObjectMapper mapper = new ObjectMapper();

      while (paramKeys.hasNext()) {
         String paramKey = (String) paramKeys.next();

         try {
            switch (paramKey) {
            case ACTIONS_VAR:
               setActions(mapper.readValue(parameters.getString(paramKey),
                     ForwardingActionSet.class));
               break;
            case DST_PORT_RANGE_VAR:
               setDstPortRange(mapper.readValue(parameters.getString(paramKey),
                     SubRangeSet.class));
               break;
            case DST_PREFIXES_VAR:
               setDstPrefixes(mapper.readValue(parameters.getString(paramKey),
                     PrefixSet.class));
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
               setIpProtocolRange(mapper.readValue(
                     parameters.getString(paramKey), SubRangeSet.class));
               break;
            case SRC_PORT_RANGE_VAR:
               setSrcPortRange(mapper.readValue(parameters.getString(paramKey),
                     SubRangeSet.class));
               break;
            case SRC_PREFIXES_VAR:
               setSrcPrefixes(mapper.readValue(parameters.getString(paramKey),
                     PrefixSet.class));
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

   public void setSrcPortRange(SubRangeSet rangeSet) {
      _srcPortRange = rangeSet;
   }

   public void setSrcPrefixes(PrefixSet prefixSet) {
      _srcPrefixes = prefixSet;
   }

}
