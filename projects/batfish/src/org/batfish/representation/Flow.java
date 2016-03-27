package org.batfish.representation;

import org.batfish.common.BatfishException;

public class Flow implements Comparable<Flow> {

   private final Ip _dstIp;

   private final Integer _dstPort;

   private final Integer _icmpCode;

   private final Integer _icmpType;

   private final String _ingressNode;

   private final IpProtocol _ipProtocol;

   private final Ip _srcIp;

   private final Integer _srcPort;

   private final String _tag;

   private final Integer _tcpFlags;

   public Flow(String ingressNode, Ip srcIp, Ip dstIp, Integer srcPort,
         Integer dstPort, IpProtocol ipProtocol, int icmpType, int icmpCode,
         int tcpFlags, String tag) {
      _ingressNode = ingressNode;
      _srcIp = srcIp;
      _dstIp = dstIp;
      _srcPort = srcPort;
      _dstPort = dstPort;
      _ipProtocol = ipProtocol;
      _icmpType = icmpType;
      _icmpCode = icmpCode;
      _tcpFlags = tcpFlags;
      _tag = tag;
      if ((srcPort == null || dstPort == null)
            && (srcPort != null || dstPort != null)) {
         throw new BatfishException(
               "Invalid flow: exactly one of srcPort and DstPort is null");
      }
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
      if (_srcPort == null) {
         return 0;
      }
      ret = _srcPort.compareTo(rhs._srcPort);
      if (ret != 0) {
         return ret;
      }
      ret = _dstPort.compareTo(rhs._dstPort);
      if (ret != 0) {
         return ret;
      }
      ret = _icmpType.compareTo(rhs._icmpType);
      if (ret != 0) {
         return ret;
      }
      ret = _icmpCode.compareTo(rhs._icmpCode);
      if (ret != 0) {
         return ret;
      }
      return _tcpFlags.compareTo(rhs._tcpFlags);
   }

   @Override
   public boolean equals(Object obj) {
      Flow other = (Flow) obj;
      if (!_dstIp.equals(other._dstIp)) {
         return false;
      }
      if (_dstPort == null) {
         if (other._dstPort != null) {
            return false;
         }
      }
      else if (!_dstPort.equals(other._dstPort)) {
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
      if (_srcPort == null) {
         if (other._srcPort != null) {
            return false;
         }
      }
      else if (!_srcPort.equals(other._srcPort)) {
         return false;
      }
      if (_icmpType == null) {
         if (other._icmpType != null) {
            return false;
         }
      }
      else if (!_icmpType.equals(other._icmpType)) {
         return false;
      }
      if (_icmpCode == null) {
         if (other._icmpCode != null) {
            return false;
         }
      }
      else if (!_icmpCode.equals(other._icmpCode)) {
         return false;
      }
      if (_tcpFlags == null) {
         if (other._tcpFlags != null) {
            return false;
         }
      }
      else if (!_tcpFlags.equals(other._tcpFlags)) {
         return false;
      }
      return _tag.equals(other._tag);
   }

   public Ip getDstIp() {
      return _dstIp;
   }

   public Integer getDstPort() {
      return _dstPort;
   }

   public Integer getIcmpCode() {
      return _icmpCode;
   }

   public Integer getIcmpType() {
      return _icmpType;
   }

   public String getIngressNode() {
      return _ingressNode;
   }

   public IpProtocol getIpProtocol() {
      return _ipProtocol;
   }

   public Ip getSrcIp() {
      return _srcIp;
   }

   public Integer getSrcPort() {
      return _srcPort;
   }

   public String getTag() {
      return _tag;
   }

   public Integer getTcpFlags() {
      return _tcpFlags;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _dstIp.hashCode();
      result = prime * result + ((_dstPort == null) ? 0 : _dstPort.hashCode());
      result = prime * result + _ingressNode.hashCode();
      result = prime * result + _ipProtocol.hashCode();
      result = prime * result + _srcIp.hashCode();
      result = prime * result + ((_srcPort == null) ? 0 : _srcPort.hashCode());
      result = prime * result + _tag.hashCode();
      result = prime * result
            + ((_icmpType == null) ? 0 : _icmpType.hashCode());
      result = prime * result
            + ((_icmpCode == null) ? 0 : _icmpCode.hashCode());
      result = prime * result
            + ((_tcpFlags == null) ? 0 : _tcpFlags.hashCode());
      return result;
   }

   public String toLBLine() {
      long src_ip = _srcIp.asLong();
      long dst_ip = _dstIp.asLong();
      long src_port = _srcPort == null ? 0 : _srcPort;
      long dst_port = _dstPort == null ? 0 : _dstPort;
      long protocol = _ipProtocol.number();
      long icmpType = _icmpType == null ? -1 : _icmpType;
      long icmpCode = _icmpCode == null ? -1 : _icmpCode;
      long tcpFlags = _tcpFlags == null ? -1 : _tcpFlags;
      String line = _ingressNode + "|" + src_ip + "|" + dst_ip + "|" + src_port
            + "|" + dst_port + "|" + protocol + "|" + icmpType + "|" + icmpCode
            + "|" + tcpFlags + "|" + _tag + "\n";
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
         tcpFlags = _tcpFlags.toString();
      }
      else {
         tcpFlags = "N/A";
      }
      if (icmp) {
         icmpCode = _icmpCode.toString();
         icmpType = _icmpType.toString();
      }
      else {
         icmpCode = "N/A";
         icmpType = "N/A";
      }
      return "Flow<ingress_node:" + _ingressNode + ", src_ip:" + _srcIp
            + ", dst_ip:" + _dstIp + ", ip_protocol:" + _ipProtocol
            + ", src_port:" + srcPort + ", dst_port:" + dstPort
            + ", icmp_type:" + icmpType + ", icmp_code:" + icmpCode
            + ", tcp_flags:" + tcpFlags + ", tag:" + _tag + ">";
   }

}
