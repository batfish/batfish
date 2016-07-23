package org.batfish.client;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;

public class MyApplication {
   
   private IpProtocol _ipProtocol;
   private Integer _port;

   public MyApplication(String protocolStr) {
      if (protocolStr.equalsIgnoreCase("dns")) {
         _ipProtocol = IpProtocol.UDP;
         _port = NamedPort.DOMAIN.number();
      }
      else if (protocolStr.equalsIgnoreCase("ssh")) {
         _ipProtocol = IpProtocol.TCP;
         _port = NamedPort.SSH.number();
      }
      else if (protocolStr.equals("tcp")) {
         _ipProtocol = IpProtocol.TCP;
      }
      else if (protocolStr.equals("udp")) {
         _ipProtocol = IpProtocol.UDP;
      }
      else 
         throw new BatfishException("unsupported protocol string: " + protocolStr);
   }
   
   IpProtocol getIpProtocol() {
      return _ipProtocol;
   }
   
   Integer getPort() {
      return _port;
   }    
}
