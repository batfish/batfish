package org.batfish.question;

//import org.batfish.representation.Configuration;
import org.batfish.main.BatfishException;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Ip;

public enum BgpNeighborIpExpr implements IpExpr {
   REMOTE_IP;

   @Override
   public Ip evaluate(Environment environment) {
      // Configuration node = context.getNode();
      BgpNeighbor neighbor = environment.getBgpNeighbor();
      switch (this) {

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
