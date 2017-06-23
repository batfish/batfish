package org.batfish.datamodel;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public final class Flow implements Comparable<Flow> {

   public static class Builder {

      private Integer _dscp;

      private Ip _dstIp;

      private Integer _dstPort;

      private Integer _ecn;

      private Integer _fragmentOffset;

      private Integer _icmpCode;

      private Integer _icmpType;

      private String _ingressInterface;

      private String _ingressNode;

      private String _ingressVrf;

      private IpProtocol _ipProtocol;

      private Integer _packetLength;

      private Ip _srcIp;

      private Integer _srcPort;

      private State _state;

      private String _tag;

      private Integer _tcpFlagsAck;

      private Integer _tcpFlagsCwr;

      private Integer _tcpFlagsEce;

      private Integer _tcpFlagsFin;

      private Integer _tcpFlagsPsh;

      private Integer _tcpFlagsRst;

      private Integer _tcpFlagsSyn;

      private Integer _tcpFlagsUrg;

      public Builder() {
         _dscp = 0;
         _dstIp = Ip.ZERO;
         _dstPort = 0;
         _ecn = 0;
         _fragmentOffset = 0;
         _ipProtocol = IpProtocol.IP;
         _srcIp = Ip.ZERO;
         _srcPort = 0;
         _icmpType = IcmpType.UNSET;
         _icmpCode = IcmpCode.UNSET;
         _ingressVrf = Configuration.DEFAULT_VRF_NAME;
         _packetLength = 0;
         _state = State.NEW;
         _tcpFlagsCwr = 0;
         _tcpFlagsEce = 0;
         _tcpFlagsUrg = 0;
         _tcpFlagsAck = 0;
         _tcpFlagsPsh = 0;
         _tcpFlagsRst = 0;
         _tcpFlagsSyn = 0;
         _tcpFlagsFin = 0;
      }

      public Builder(Flow flow) {
         _ingressNode = flow._ingressNode;
         _ingressInterface = flow._ingressInterface;
         _ingressVrf = flow._ingressVrf;
         _srcIp = flow._srcIp;
         _dstIp = flow._dstIp;
         _srcPort = flow._srcPort;
         _dstPort = flow._dstPort;
         _ipProtocol = flow._ipProtocol;
         _dscp = flow._dscp;
         _ecn = flow._ecn;
         _fragmentOffset = flow._fragmentOffset;
         _icmpType = flow._icmpType;
         _icmpCode = flow._icmpCode;
         _packetLength = flow._packetLength;
         _state = flow._state;
         _tcpFlagsCwr = flow._tcpFlagsCwr;
         _tcpFlagsEce = flow._tcpFlagsEce;
         _tcpFlagsUrg = flow._tcpFlagsUrg;
         _tcpFlagsAck = flow._tcpFlagsAck;
         _tcpFlagsPsh = flow._tcpFlagsPsh;
         _tcpFlagsRst = flow._tcpFlagsRst;
         _tcpFlagsSyn = flow._tcpFlagsSyn;
         _tcpFlagsFin = flow._tcpFlagsFin;
         _tag = flow._tag;
      }

      public Flow build() {
         if (_ingressNode == null) {
            throw new BatfishException(
                  "Cannot build flow without at least specifying ingress node");
         }
         if (_tag == null) {
            throw new BatfishException(
                  "Cannot build flow without specifying tag");
         }
         return new Flow(_ingressNode, _ingressInterface, _ingressVrf, _srcIp,
               _dstIp, _srcPort, _dstPort, _ipProtocol, _dscp, _ecn,
               _fragmentOffset, _icmpType, _icmpCode, _packetLength, _state,
               _tcpFlagsCwr, _tcpFlagsEce, _tcpFlagsUrg, _tcpFlagsAck,
               _tcpFlagsPsh, _tcpFlagsRst, _tcpFlagsSyn, _tcpFlagsFin, _tag);
      }

      public Integer getDscp() {
         return _dscp;
      }

      public Ip getDstIp() {
         return _dstIp;
      }

      public Integer getDstPort() {
         return _dstPort;
      }

      public Integer getEcn() {
         return _ecn;
      }

      public Integer getIcmpCode() {
         return _icmpCode;
      }

      public Integer getIcmpType() {
         return _icmpType;
      }

      public String getIngressInterface() {
         return _ingressInterface;
      }

      public String getIngressNode() {
         return _ingressNode;
      }

      public String getIngressVrf() {
         return _ingressVrf;
      }

      public IpProtocol getIpProtocol() {
         return _ipProtocol;
      }

      public Integer getPacketLength() {
         return _packetLength;
      }

      public Ip getSrcIp() {
         return _srcIp;
      }

      public Integer getSrcPort() {
         return _srcPort;
      }

      public State getState() {
         return _state;
      }

      public String getTag() {
         return _tag;
      }

      public Integer getTcpFlagsAck() {
         return _tcpFlagsAck;
      }

      public Integer getTcpFlagsCwr() {
         return _tcpFlagsCwr;
      }

      public Integer getTcpFlagsEce() {
         return _tcpFlagsEce;
      }

      public Integer getTcpFlagsFin() {
         return _tcpFlagsFin;
      }

      public Integer getTcpFlagsPsh() {
         return _tcpFlagsPsh;
      }

      public Integer getTcpFlagsRst() {
         return _tcpFlagsRst;
      }

      public Integer getTcpFlagsSyn() {
         return _tcpFlagsSyn;
      }

      public Integer getTcpFlagsUrg() {
         return _tcpFlagsUrg;
      }

      public void setDscp(Integer dscp) {
         _dscp = dscp;
      }

      public void setDstIp(Ip dstIp) {
         _dstIp = dstIp;
      }

      public void setDstPort(int dstPort) {
         _dstPort = dstPort;
      }

      public void setDstPort(Integer dstPort) {
         _dstPort = dstPort;
      }

      public void setEcn(Integer ecn) {
         _ecn = ecn;
      }

      public void setFragmentOffset(int fragmentOffset) {
         _fragmentOffset = fragmentOffset;
      }

      public void setIcmpCode(Integer icmpCode) {
         _icmpCode = icmpCode;
      }

      public void setIcmpType(Integer icmpType) {
         _icmpType = icmpType;
      }

      public void setIngressInterface(String ingressInterface) {
         _ingressInterface = ingressInterface;
      }

      public void setIngressNode(String ingressNode) {
         _ingressNode = ingressNode;
      }

      public void setIngressVrf(String ingressVrf) {
         _ingressVrf = ingressVrf;
      }

      public void setIpProtocol(IpProtocol ipProtocol) {
         _ipProtocol = ipProtocol;
      }

      public void setPacketLength(Integer packetLength) {
         _packetLength = packetLength;
      }

      public void setSrcIp(Ip srcIp) {
         _srcIp = srcIp;
      }

      public void setSrcPort(int srcPort) {
         _srcPort = srcPort;
      }

      public void setSrcPort(Integer srcPort) {
         _srcPort = srcPort;
      }

      public void setState(State state) {
         _state = state;
      }

      public void setTag(String tag) {
         _tag = tag;
      }

      public void setTcpFlagsAck(Integer tcpFlagsAck) {
         _tcpFlagsAck = tcpFlagsAck;
      }

      public void setTcpFlagsCwr(Integer tcpFlagsCwr) {
         _tcpFlagsCwr = tcpFlagsCwr;
      }

      public void setTcpFlagsEce(Integer tcpFlagsEce) {
         _tcpFlagsEce = tcpFlagsEce;
      }

      public void setTcpFlagsFin(Integer tcpFlagsFin) {
         _tcpFlagsFin = tcpFlagsFin;
      }

      public void setTcpFlagsPsh(Integer tcpFlagsPsh) {
         _tcpFlagsPsh = tcpFlagsPsh;
      }

      public void setTcpFlagsRst(Integer tcpFlagsRst) {
         _tcpFlagsRst = tcpFlagsRst;
      }

      public void setTcpFlagsSyn(Integer tcpFlagsSyn) {
         _tcpFlagsSyn = tcpFlagsSyn;
      }

      public void setTcpFlagsUrg(Integer tcpFlagsUrg) {
         _tcpFlagsUrg = tcpFlagsUrg;
      }

   }

   private static final String DSCP_VAR = "dscp";

   private static final String DST_IP_VAR = "dstIp";

   private static final String DST_PORT_VAR = "dstPort";

   private static final String ECN_VAR = "ecn";

   private static final String FRAGMENT_OFFSET_VAR = "fragmentOffset";

   private static final String ICMP_CODE_VAR = "icmpCode";

   private static final String ICMP_TYPE_VAR = "icmpVar";

   private static final String INGRESS_INTERFACE_VAR = "ingressInterface";

   private static final String INGRESS_NODE_VAR = "ingressNode";

   private static final String INGRESS_VRF_VAR = "ingressVrf";

   private static final String IP_PROTOCOL_VAR = "ipProtocol";

   private static final String PACKET_LENGTH_VAR = "packetLength";

   private static final String SRC_IP_VAR = "srcIp";

   private static final String SRC_PORT_VAR = "srcPort";

   private static final String STATE_VAR = "state";

   private static final String TAG_VAR = "tag";

   private static final String TCP_FLAGS_ACK_VAR = "tcpFlagsAck";

   private static final String TCP_FLAGS_CWR_VAR = "tcpFlagsCwr";

   private static final String TCP_FLAGS_ECE_VAR = "tcpFlagsEce";

   private static final String TCP_FLAGS_FIN_VAR = "tcpFlagsFin";

   private static final String TCP_FLAGS_PSH_VAR = "tcpFlagsPsh";

   private static final String TCP_FLAGS_RST_VAR = "tcpFlagsRst";

   private static final String TCP_FLAGS_SYN_VAR = "tcpFlagsSyn";

   private static final String TCP_FLAGS_URG_VAR = "tcpFlagsUrg";

   private final int _dscp;

   private final Ip _dstIp;

   private final int _dstPort;

   private final int _ecn;

   private final int _fragmentOffset;

   private final int _icmpCode;

   private final int _icmpType;

   private String _ingressInterface;

   private final String _ingressNode;

   private final String _ingressVrf;

   private final IpProtocol _ipProtocol;

   private final int _packetLength;

   private final Ip _srcIp;

   private final int _srcPort;

   private final State _state;

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
   public Flow(@JsonProperty(INGRESS_NODE_VAR) String ingressNode,
         @JsonProperty(INGRESS_INTERFACE_VAR) String ingressInterface,
         @JsonProperty(INGRESS_VRF_VAR) String ingressVrf,
         @JsonProperty(SRC_IP_VAR) Ip srcIp, @JsonProperty(DST_IP_VAR) Ip dstIp,
         @JsonProperty(SRC_PORT_VAR) int srcPort,
         @JsonProperty(DST_PORT_VAR) int dstPort,
         @JsonProperty(IP_PROTOCOL_VAR) IpProtocol ipProtocol,
         @JsonProperty(DSCP_VAR) int dscp, @JsonProperty(ECN_VAR) int ecn,
         @JsonProperty(FRAGMENT_OFFSET_VAR) int fragmentOffset,
         @JsonProperty(ICMP_TYPE_VAR) int icmpType,
         @JsonProperty(ICMP_CODE_VAR) int icmpCode,
         @JsonProperty(PACKET_LENGTH_VAR) int packetLength,
         @JsonProperty(STATE_VAR) State state,
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
      _ingressInterface = ingressInterface;
      _ingressVrf = ingressVrf;
      _srcIp = srcIp;
      _dstIp = dstIp;
      _srcPort = srcPort;
      _dstPort = dstPort;
      _ipProtocol = ipProtocol;
      _dscp = dscp;
      _ecn = ecn;
      _fragmentOffset = fragmentOffset;
      _icmpType = icmpType;
      _icmpCode = icmpCode;
      _packetLength = packetLength;
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
      ret = _ingressVrf.compareTo(rhs._ingressVrf);
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
      ret = Integer.compare(_fragmentOffset, rhs._fragmentOffset);
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
      ret = Integer.compare(_packetLength, rhs._packetLength);
      if (ret != 0) {
         return ret;
      }
      ret = _state.compareTo(rhs._state);
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
      if (_fragmentOffset != other._fragmentOffset) {
         return false;
      }
      if (!_ingressNode.equals(other._ingressNode)) {
         return false;
      }
      if (!_ingressVrf.equals(other._ingressVrf)) {
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
      if (_packetLength != other._packetLength) {
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

   @JsonProperty(ECN_VAR)
   public int getEcn() {
      return _ecn;
   }

   @JsonProperty(FRAGMENT_OFFSET_VAR)
   public int getFragmentOffset() {
      return _fragmentOffset;
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

   @JsonProperty(PACKET_LENGTH_VAR)
   public int getPacketLength() {
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

   @JsonProperty(STATE_VAR)
   public State getState() {
      return _state;
   }

   @JsonProperty(TAG_VAR)
   public String getTag() {
      return _tag;
   }

   @JsonProperty(TCP_FLAGS_ACK_VAR)
   public int getTcpFlagsAck() {
      return _tcpFlagsAck;
   }

   @JsonProperty(TCP_FLAGS_CWR_VAR)
   public int getTcpFlagsCwr() {
      return _tcpFlagsCwr;
   }

   @JsonProperty(TCP_FLAGS_ECE_VAR)
   public int getTcpFlagsEce() {
      return _tcpFlagsEce;
   }

   @JsonProperty(TCP_FLAGS_FIN_VAR)
   public int getTcpFlagsFin() {
      return _tcpFlagsFin;
   }

   @JsonProperty(TCP_FLAGS_PSH_VAR)
   public int getTcpFlagsPsh() {
      return _tcpFlagsPsh;
   }

   @JsonProperty(TCP_FLAGS_RST_VAR)
   public int getTcpFlagsRst() {
      return _tcpFlagsRst;
   }

   @JsonProperty(TCP_FLAGS_SYN_VAR)
   public int getTcpFlagsSyn() {
      return _tcpFlagsSyn;
   }

   @JsonProperty(TCP_FLAGS_URG_VAR)
   public int getTcpFlagsUrg() {
      return _tcpFlagsUrg;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _dscp;
      result = prime * result + _dstIp.hashCode();
      result = prime * result + _dstPort;
      result = prime * result + _ecn;
      result = prime * result + _fragmentOffset;
      result = prime * result + _ingressNode.hashCode();
      result = prime * result + _ingressVrf.hashCode();
      result = prime * result + _ipProtocol.ordinal();
      result = prime * result + _srcIp.hashCode();
      result = prime * result + _srcPort;
      result = prime * result + _icmpType;
      result = prime * result + _icmpCode;
      result = prime * result + _packetLength;
      result = prime * result + _state.ordinal();
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

   public String prettyPrint(String prefixString) {
      boolean icmp = _ipProtocol == IpProtocol.ICMP;
      boolean tcp = _ipProtocol == IpProtocol.TCP;
      boolean udp = _ipProtocol == IpProtocol.UDP;
      String srcPortStr = "";
      String dstPortStr = "";
      String icmpTypeStr = "";
      String icmpCodeStr = "";
      String tcpFlagsStr = "";
      if (tcp || udp) {
         srcPortStr = " sport:" + NamedPort.nameFromNumber(_srcPort);
         dstPortStr = " dport:" + NamedPort.nameFromNumber(_dstPort);
      }
      if (tcp) {
         tcpFlagsStr = String.format(" tcpFlags:%d%d%d%d%d%d%d%d", _tcpFlagsCwr,
               _tcpFlagsEce, _tcpFlagsUrg, _tcpFlagsAck, _tcpFlagsPsh,
               _tcpFlagsRst, _tcpFlagsSyn, _tcpFlagsFin);
      }
      if (icmp) {
         icmpCodeStr = " icmpCode:" + Integer.toString(_icmpCode);
         icmpTypeStr = " icmpType:" + Integer.toString(_icmpType);
      }
      String dscpStr = (_dscp != 0) ? " dscp:" + _dscp : "";
      String ecnStr = (_ecn != 0) ? " ecn:" + _ecn : "";

      return prefixString + "Flow: ingress:" + _ingressNode + " vrf:"
            + _ingressVrf + " " + _srcIp + "->" + _dstIp + " " + _ipProtocol
            + srcPortStr + dstPortStr + dscpStr + ecnStr + icmpTypeStr
            + icmpCodeStr + " packetLength:" + _packetLength + " state:"
            + _state + tcpFlagsStr;
   }

   @JsonProperty(INGRESS_INTERFACE_VAR)
   public void setIngressInterface(String ingressInterface) {
      _ingressInterface = ingressInterface;
   }

   @Override
   public String toString() {
      boolean icmp = _ipProtocol == IpProtocol.ICMP;
      boolean tcp = _ipProtocol == IpProtocol.TCP;
      boolean udp = _ipProtocol == IpProtocol.UDP;
      String srcPortStr = "";
      String dstPortStr = "";
      String icmpTypeStr = "";
      String icmpCodeStr = "";
      String tcpFlagsStr = "";
      if (tcp || udp) {
         srcPortStr = " srcPort:" + NamedPort.nameFromNumber(_srcPort);
         dstPortStr = " dstPort:" + NamedPort.nameFromNumber(_dstPort);
      }
      if (tcp) {
         tcpFlagsStr = String.format(" tcpFlags:%d%d%d%d%d%d%d%d", _tcpFlagsCwr,
               _tcpFlagsEce, _tcpFlagsUrg, _tcpFlagsAck, _tcpFlagsPsh,
               _tcpFlagsRst, _tcpFlagsSyn, _tcpFlagsFin);
      }
      if (icmp) {
         icmpCodeStr = " icmpCode:" + Integer.toString(_icmpCode);
         icmpTypeStr = " icmpType:" + Integer.toString(_icmpType);
      }
      return "Flow<ingressNode:" + _ingressNode + " ingressVrf:" + _ingressVrf
            + " srcIp:" + _srcIp + " dstIp:" + _dstIp + " ipProtocol:"
            + _ipProtocol + srcPortStr + dstPortStr + " dscp: " + _dscp
            + " ecn:" + _ecn + " fragmentOffset:" + _fragmentOffset
            + icmpTypeStr + icmpCodeStr + " packetLength:" + _packetLength
            + " state:" + _state + tcpFlagsStr + " tag:" + _tag + ">";
   }

}
