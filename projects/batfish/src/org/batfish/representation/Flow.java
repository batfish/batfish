package org.batfish.representation;

import org.batfish.main.BatfishException;

public class Flow implements Comparable<Flow> {

   private final Ip _dstIp;

   private final Integer _dstPort;

   private final String _ingressNode;

   private final IpProtocol _ipProtocol;

   private final Ip _srcIp;

   private final Integer _srcPort;

   public Flow(String ingressNode, Ip srcIp, Ip dstIp, Integer srcPort,
         Integer dstPort, IpProtocol ipProtocol) {
      _ingressNode = ingressNode;
      _srcIp = srcIp;
      _dstIp = dstIp;
      _srcPort = srcPort;
      _dstPort = dstPort;
      _ipProtocol = ipProtocol;
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
      return _dstPort.compareTo(rhs._dstPort);
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
      return true;
   }

   public Ip getDstIp() {
      return _dstIp;
   }

   public Integer getDstPort() {
      return _dstPort;
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
      return result;
   }

   public String toLBLine() {
      long src_ip = _srcIp.asLong();
      long dst_ip = _dstIp.asLong();
      long src_port = _srcPort == null ? 0 : _srcPort;
      long dst_port = _dstPort == null ? 0 : _dstPort;
      long protocol = _ipProtocol.number();
      String line = _ingressNode + "|" + src_ip + "|" + dst_ip + "|" + src_port
            + "|" + dst_port + "|" + protocol + "\n";
      return line;
   }

   @Override
   public String toString() {
      return "Flow<ingressNode:" + _ingressNode + ", srcIp:" + _srcIp
            + ", dstIp:" + _dstIp + ", IpProtocol:" + _ipProtocol
            + ", srcPort:" + _srcPort + ", dstPort:" + _dstPort + ">";
   }

}
