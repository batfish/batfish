package org.batfish.question;

//import org.batfish.representation.Configuration;
import org.batfish.main.BatfishException;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Ip;

public enum BgpNeighborIpExpr implements IpExpr {
   LOCAL_IP,
   REMOTE_IP;

   @Override
   public Ip evaluate(Environment environment) {
      // Configuration node = context.getNode();
      BgpNeighbor neighbor = environment.getBgpNeighbor();
      switch (this) {

      case LOCAL_IP:
         return neighbor.getLocalIp();

      case REMOTE_IP:
         return neighbor.getAddress();

      default:
         throw new BatfishException("Invalid bgp neighbor ip expression");
      }
   }

   @Override
   public String print(Environment environment) {
      return evaluate(environment).toString();
   }

}
