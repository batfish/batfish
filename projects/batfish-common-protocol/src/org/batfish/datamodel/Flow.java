package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public final class Flow implements Comparable<Flow> {

   private final String DSCP_VAR = "dscp";
   private final String DST_IP_VAR = "dstIp";
   private final String DST_PORT_VAR = "dstPort";
   private final String ECN_VAR = "ecn";
   private final String ICMP_CODE_VAR = "icmpCode";
   private final String ICMP_TYPE_VAR = "icmpVar";
   private final String INGRESS_NODE_VAR = "ingressNode";
   private final String IP_PROTOCOL_VAR = "ipProtocol";
   private final String SRC_IP_VAR = "srcIp";
   private final String SRC_PORT_VAR = "srcPort";
   private final String STATE_VAR = "state";
   private final String TAG_VAR = "tag";
   private final String TCP_FLAGS_ACK_VAR = "tcpFlagsAck";
   private final String TCP_FLAGS_CWR_VAR = "tcpFlagsCwr";
   private final String TCP_FLAGS_ECE_VAR = "tcpFlagsEce";
   private final String TCP_FLAGS_FIN_VAR = "tcpFlagsFin";
   private final String TCP_FLAGS_PSH_VAR = "tcpFlagsPsh";
   private final String TCP_FLAGS_RST_VAR = "tcpFlagsRst";
   private final String TCP_FLAGS_SYN_VAR = "tcpFlagsSyn";
   private final String TCP_FLAGS_URG_VAR = "tcpFlagsUrg";
   
   private final int _dscp;

   private final Ip _dstIp;

   private final int _dstPort;

   private final int _ecn;

   private final int _icmpCode;

   private final int _icmpType;

   private final String _ingressNode;

   private final IpProtocol _ipProtocol;

   private final Ip _srcIp;

   private final int _srcPort;

   private final int _state;

   private final String _tag;

   private final int _tcpFlagsAck;

   private final int _tcpFlagsCwr;

   private final int _tcpFlagsEce;

   private final int _tcpFlagsFin;

   private final int _tcpFlagsPsh;

   private final int _tcpFlagsRst;

   private final int _tcpFlagsSyn;

   private final int _tcpFlagsUrg;

   @JsonCreator
   Flow(@JsonProperty(INGRESS_NODE_VAR) String ingressNode, 
        @JsonProperty(SRC_IP_VAR) Ip srcIp, 
        @JsonProperty(DST_IP_VAR) Ip dstIp, 
        @JsonProperty(SRC_PORT_VAR) int srcPort, 
        @JsonProperty(DST_PORT_VAR) int dstPort,
        @JsonProperty(IP_PROTOCOL_VAR) IpProtocol ipProtocol, 
        @JsonProperty(DSCP_VAR) int dscp, 
        @JsonProperty(ECN_VAR) int ecn, 
        @JsonProperty(ICMP_TYPE_VAR) int icmpType, 
        @JsonProperty(ICMP_CODE_VAR) int icmpCode,
        @JsonProperty(STATE_VAR) int state, 
        @JsonProperty(TCP_FLAGS_CWR_VAR) int tcpFlagsCwr, 
        @JsonProperty(TCP_FLAGS_ECE_VAR) int tcpFlagsEce, 
        @JsonProperty(TCP_FLAGS_URG_VAR) int tcpFlagsUrg,
        @JsonProperty(TCP_FLAGS_ACK_VAR) int tcpFlagsAck,
        @JsonProperty(TCP_FLAGS_PSH_VAR) int tcpFlagsPsh,
        @JsonProperty(TCP_FLAGS_RST_VAR) int tcpFlagsRst,
        @JsonProperty(TCP_FLAGS_SYN_VAR) int tcpFlagsSyn,
        @JsonProperty(TCP_FLAGS_FIN_VAR) int tcpFlagsFin,
        @JsonProperty(TAG_VAR) String tag) {
      _ingressNode = ingressNode;
      _srcIp = srcIp;
      _dstIp = dstIp;
      _srcPort = srcPort;
      _dstPort = dstPort;
      _ipProtocol = ipProtocol;
      _dscp = dscp;
      _ecn = ecn;
      _icmpType = icmpType;
      _icmpCode = icmpCode;
      _state = state;
      _tcpFlagsCwr = tcpFlagsCwr;
      _tcpFlagsEce = tcpFlagsEce;
      _tcpFlagsUrg = tcpFlagsUrg;
      _tcpFlagsAck = tcpFlagsAck;
      _tcpFlagsPsh = tcpFlagsPsh;
      _tcpFlagsRst = tcpFlagsRst;
      _tcpFlagsSyn = tcpFlagsSyn;
      _tcpFlagsFin = tcpFlagsFin;
      _tag = tag;
   }

   @Override
   public int compareTo(Flow rhs) {
      int ret;
      ret = _ingressNode.compareTo(rhs._ingressNode);
      if (ret != 0) {
         return ret;
      }
      ret = _srcIp.compareTo(rhs._srcIp);
      if (ret != 0) {
         return ret;
      }
      ret = _dstIp.compareTo(rhs._dstIp);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_ipProtocol.number(), rhs._ipProtocol.number());
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_srcPort, rhs._srcPort);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_dstPort, rhs._dstPort);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_dscp, rhs._dscp);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_ecn, rhs._ecn);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_icmpType, rhs._icmpType);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_icmpCode, rhs._icmpCode);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_state, rhs._state);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsCwr, rhs._tcpFlagsCwr);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsEce, rhs._tcpFlagsEce);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsUrg, rhs._tcpFlagsUrg);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsAck, rhs._tcpFlagsAck);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsPsh, rhs._tcpFlagsPsh);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsRst, rhs._tcpFlagsRst);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_tcpFlagsSyn, rhs._tcpFlagsSyn);
      if (ret != 0) {
         return ret;
      }
      return Integer.compare(_tcpFlagsFin, rhs._tcpFlagsFin);
   }

   @Override
   public boolean equals(Object obj) {
      Flow other = (Flow) obj;
      if (_dscp != other._dscp) {
         return false;
      }
      if (!_dstIp.equals(other._dstIp)) {
         return false;
      }
      if (_dstPort != other._dstPort) {
         return false;
      }
      if (_ecn != other._ecn) {
         return false;
      }
      if (!_ingressNode.equals(other._ingressNode)) {
         return false;
      }
      if (_ipProtocol != other._ipProtocol) {
         return false;
      }
      if (!_srcIp.equals(other._srcIp)) {
         return false;
      }
      if (_srcPort != other._srcPort) {
         return false;
      }
      if (_icmpType != other._icmpType) {
         return false;
      }
      if (_icmpCode != other._icmpCode) {
         return false;
      }
      if (_state != other._state) {
         return false;
      }
      if (_tcpFlagsCwr != other._tcpFlagsCwr) {
         return false;
      }
      if (_tcpFlagsEce != other._tcpFlagsEce) {
         return false;
      }
      if (_tcpFlagsUrg != other._tcpFlagsUrg) {
         return false;
      }
      if (_tcpFlagsAck != other._tcpFlagsAck) {
         return false;
      }
      if (_tcpFlagsPsh != other._tcpFlagsPsh) {
         return false;
      }
      if (_tcpFlagsRst != other._tcpFlagsRst) {
         return false;
      }
      if (_tcpFlagsSyn != other._tcpFlagsSyn) {
         return false;
      }
      if (_tcpFlagsFin != other._tcpFlagsFin) {
         return false;
      }
      return _tag.equals(other._tag);
   }

   @JsonProperty(DSCP_VAR) 
   public int getDscp() {
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

   @JsonProperty(STATE_VAR) 
   public int getState() {
      return _state;
   }

   @JsonProperty(TAG_VAR) 
   public String getTag() {
      return _tag;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _dscp;
      result = prime * result + _dstIp.hashCode();
      result = prime * result + _dstPort;
      result = prime * result + _ecn;
      result = prime * result + _ingressNode.hashCode();
      result = prime * result + _ipProtocol.hashCode();
      result = prime * result + _srcIp.hashCode();
      result = prime * result + _srcPort;
      result = prime * result + _icmpType;
      result = prime * result + _icmpCode;
      result = prime * result + _state;
      result = prime * result + _tag.hashCode();
      result = prime * result + _tcpFlagsCwr;
      result = prime * result + _tcpFlagsEce;
      result = prime * result + _tcpFlagsUrg;
      result = prime * result + _tcpFlagsAck;
      result = prime * result + _tcpFlagsPsh;
      result = prime * result + _tcpFlagsRst;
      result = prime * result + _tcpFlagsSyn;
      result = prime * result + _tcpFlagsFin;
      return result;
   }

   public String toLBLine() {
      long src_ip = _srcIp.asLong();
      long dst_ip = _dstIp.asLong();
      long src_port = _srcPort;
      long dst_port = _dstPort;
      long protocol = _ipProtocol.number();
      long icmpType = _icmpType;
      long icmpCode = _icmpCode;
      String line = _ingressNode + "|" + src_ip + "|" + dst_ip + "|" + src_port
            + "|" + dst_port + "|" + protocol + "|" + _dscp + "|" + _ecn + "|"
            + icmpType + "|" + icmpCode + "|" + _state + "|" + _tcpFlagsCwr
            + "|" + _tcpFlagsEce + "|" + _tcpFlagsUrg + "|" + _tcpFlagsAck
            + "|" + _tcpFlagsPsh + "|" + _tcpFlagsRst + "|" + _tcpFlagsSyn
            + "|" + _tcpFlagsFin + "|" + _tag + "\n";
      return line;
   }

   @Override
   public String toString() {
      boolean icmp = _ipProtocol == IpProtocol.ICMP;
      boolean tcp = _ipProtocol == IpProtocol.TCP;
      boolean udp = _ipProtocol == IpProtocol.UDP;
      String srcPort;
      String dstPort;
      String icmpType;
      String icmpCode;
      String tcpFlags;
      if (tcp || udp) {
         srcPort = NamedPort.nameFromNumber(_srcPort);
         dstPort = NamedPort.nameFromNumber(_dstPort);
      }
      else {
         srcPort = "N/A";
         dstPort = "N/A";
      }
      if (tcp) {
         tcpFlags = String.format("%d%d%d%d%d%d%d%d", _tcpFlagsCwr,
               _tcpFlagsEce, _tcpFlagsUrg, _tcpFlagsAck, _tcpFlagsPsh,
               _tcpFlagsRst, _tcpFlagsSyn, _tcpFlagsFin);
      }
      else {
         tcpFlags = "N/A";
      }
      if (icmp) {
         icmpCode = Integer.toString(_icmpCode);
         icmpType = Integer.toString(_icmpType);
      }
      else {
         icmpCode = "N/A";
         icmpType = "N/A";
      }
      return "Flow<ingress_node:" + _ingressNode + ", src_ip:" + _srcIp
            + ", dst_ip:" + _dstIp + ", ip_protocol:" + _ipProtocol
            + ", src_port:" + srcPort + ", dst_port:" + dstPort + ", dscp: "
            + _dscp + ", ecn:" + _ecn + ", icmp_type:" + icmpType
            + ", icmp_code:" + icmpCode + ", state:" + _state + ", tcp_flags:"
            + tcpFlags + ", tag:" + _tag + ">";
   }

}
