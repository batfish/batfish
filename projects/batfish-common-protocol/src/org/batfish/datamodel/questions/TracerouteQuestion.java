package org.batfish.datamodel.questions;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.FlowBuilder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TracerouteQuestion extends Question {

   private static final String DSCP_VAR = "dscp";

   private static final String DST_IP_VAR = "dstIp";

   private static final String DST_PORT_VAR = "dstPort";

   private static final String ECN_VAR = "ecn";

   private static final String ICMP_CODE_VAR = "icmpCode";

   private static final String ICMP_TYPE_VAR = "icmpType";

   private static final String INGRESS_NODE_VAR = "ingressNode";

   private static final String IP_PROTOCOL_VAR = "ipProtocol";

   private static final String SRC_IP_VAR = "srcIp";

   private static final String SRC_PORT_VAR = "srcPort";

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

   private Integer _ecn;

   private Integer _icmpCode;

   private Integer _icmpType;

   private String _ingressNode;

   private IpProtocol _ipProtocol;

   private Ip _srcIp;

   private Integer _srcPort;

   private Boolean _tcpFlagsAck;

   private Boolean _tcpFlagsCwr;

   private Boolean _tcpFlagsEce;

   private Boolean _tcpFlagsFin;

   private Boolean _tcpFlagsPsh;

   private Boolean _tcpFlagsRst;

   private Boolean _tcpFlagsSyn;

   private Boolean _tcpFlagsUrg;

   public TracerouteQuestion() {
      super(QuestionType.TRACEROUTE);
   }

   public FlowBuilder createFlowBuilder() {
      FlowBuilder flowBuilder = new FlowBuilder();
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
      if (_ipProtocol != null) {
         flowBuilder.setIpProtocol(_ipProtocol);
      }
      if (_srcIp != null) {
         flowBuilder.setSrcIp(_srcIp);
      }
      else {
         flowBuilder.setSrcIp(Ip.AUTO);
      }
      if (_srcPort != null) {
         flowBuilder.setSrcPort(_srcPort);
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

   @JsonProperty(ECN_VAR)
   public Integer getEcn() {
      return _ecn;
   }

   @JsonIgnore
   public Set<FlowBuilder> getFlowBuilders() {
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

   @JsonProperty(INGRESS_NODE_VAR)
   public String getIngressNode() {
      return _ingressNode;
   }

   @JsonProperty(IP_PROTOCOL_VAR)
   public IpProtocol getIpProtocol() {
      return _ipProtocol;
   }

   @JsonProperty(SRC_IP_VAR)
   public Ip getSrcIp() {
      return _srcIp;
   }

   @JsonProperty(SRC_PORT_VAR)
   public Integer getSrcPort() {
      return _srcPort;
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
         String retString = String.format("traceroute %singressNode=%s", 
               prettyPrintBase(), _ingressNode);
         //we only print "interesting" values
         if (_dscp != null) 
            retString += String.format(" | dscp=%s", _dscp); 
         if (_dstIp != null) 
            retString += String.format(" | dstIp=%s", _dstIp); 
         if (_dstPort != null) 
            retString += String.format(" | dstPort=%s", _dstPort); 
         if (_ecn != null) 
            retString += String.format(" | ecn=%s", _ecn); 
         if (_icmpCode != null) 
            retString += String.format(" | icmpCode=%s", _icmpCode); 
         if (_icmpType != null) 
            retString += String.format(" | icmpType=%s", _icmpType); 
         if (_ipProtocol != null) 
            retString += String.format(" | ipProtocol=%s", _ipProtocol); 
         if (_srcIp != null) 
            retString += String.format(" | srcIp=%s", _srcIp); 
         if (_srcPort != null) 
            retString += String.format(" | srcPort=%s", _srcPort); 
         if (_tcpFlagsAck != null) 
            retString += String.format(" | tcpFlagsAck=%s", _tcpFlagsAck); 
         if (_tcpFlagsCwr != null) 
            retString += String.format(" | tcpFlagsCwr=%s", _tcpFlagsCwr); 
         if (_tcpFlagsEce != null) 
            retString += String.format(" | tcpFlagsEce=%s", _tcpFlagsEce); 
         if (_tcpFlagsFin != null) 
            retString += String.format(" | tcpFlagsFin=%s", _tcpFlagsFin); 
         if (_tcpFlagsPsh != null) 
            retString += String.format(" | tcpFlagsPsh=%s", _tcpFlagsPsh); 
         if (_tcpFlagsRst != null) 
            retString += String.format(" | tcpFlagsRst=%s", _tcpFlagsRst); 
         if (_tcpFlagsSyn != null) 
            retString += String.format(" | tcpFlagsSyn=%s", _tcpFlagsSyn);          
         if (_tcpFlagsUrg != null) 
            retString += String.format(" | tcpFlagsUrg=%s", _tcpFlagsUrg); 
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
   
   public void setDscp(Integer dscp) {
      _dscp = dscp;
   }

   public void setDstIp(Ip dstIp) {
      _dstIp = dstIp;
   }

   public void setDstPort(Integer dstPort) {
      _dstPort = dstPort;
   }

   public void setEcn(Integer ecn) {
      _ecn = ecn;
   }

   public void setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
   }

   public void setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
   }

   public void setIngressNode(String ingressNode) {
      _ingressNode = ingressNode;
   }

   public void setIpProtocol(IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
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
            case DSCP_VAR:
               setDscp(parameters.getInt(paramKey));
               break;
            case DST_IP_VAR:
               setDstIp(new Ip(parameters.getString(paramKey)));
               break;
            case DST_PORT_VAR:
               setDstPort(parameters.getInt(paramKey));
               break;
            case ECN_VAR:
               setEcn(parameters.getInt(paramKey));
               break;
            case ICMP_CODE_VAR:
               setIcmpCode(parameters.getInt(paramKey));
               break;
            case ICMP_TYPE_VAR:
               setIcmpType(parameters.getInt(paramKey));
               break;
            case INGRESS_NODE_VAR:
               setIngressNode(parameters.getString(paramKey));
               break;
            case IP_PROTOCOL_VAR:
               setIpProtocol(IpProtocol.fromString(parameters
                     .getString(paramKey)));
               break;
            case SRC_IP_VAR:
               setSrcIp(new Ip(parameters.getString(paramKey)));
               break;
            case SRC_PORT_VAR:
               setSrcPort(parameters.getInt(paramKey));
               break;
            case TCP_FLAGS_ACK_VAR:
               setTcpFlagsAck(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_CWR_VAR:
               setTcpFlagsCwr(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_ECE_VAR:
               setTcpFlagsEce(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_FIN_VAR:
               setTcpFlagsFin(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_PSH_VAR:
               setTcpFlagsPsh(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_RST_VAR:
               setTcpFlagsRst(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_SYN_VAR:
               setTcpFlagsSyn(parameters.getBoolean(paramKey));
               break;
            case TCP_FLAGS_URG_VAR:
               setTcpFlagsUrg(parameters.getBoolean(paramKey));
               break;
            default:
               throw new BatfishException("Unknown key in "
                     + getClass().getSimpleName() + ": " + paramKey);
            }
         }
         catch (JSONException e) {
            throw new BatfishException("JSONException in parameters", e);
         }
      }
   }

   public void setSrcIp(Ip srcIp) {
      _srcIp = srcIp;
   }

   public void setSrcPort(Integer srcPort) {
      _srcPort = srcPort;
   }

   public void setTcpFlagsAck(Boolean tcpFlagsAck) {
      _tcpFlagsAck = tcpFlagsAck;
   }

   public void setTcpFlagsCwr(Boolean tcpFlagsCwr) {
      _tcpFlagsCwr = tcpFlagsCwr;
   }

   public void setTcpFlagsEce(Boolean tcpFlagsEce) {
      _tcpFlagsEce = tcpFlagsEce;
   }

   public void setTcpFlagsFin(Boolean tcpFlagsFin) {
      _tcpFlagsFin = tcpFlagsFin;
   }

   public void setTcpFlagsPsh(Boolean tcpFlagsPsh) {
      _tcpFlagsPsh = tcpFlagsPsh;
   }

   public void setTcpFlagsRst(Boolean tcpFlagsRst) {
      _tcpFlagsRst = tcpFlagsRst;
   }

   public void setTcpFlagsSyn(Boolean tcpFlagsSyn) {
      _tcpFlagsSyn = tcpFlagsSyn;
   }

   public void setTcpFlagsUrg(Boolean tcpFlagsUrg) {
      _tcpFlagsUrg = tcpFlagsUrg;
   }
}
