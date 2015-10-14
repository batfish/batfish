package org.batfish.question;

import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.representation.Ip;

public class NumIpsIntExpr extends BaseIntExpr {

   private final String _caller;

   public NumIpsIntExpr(String caller) {
      _caller = caller;
   }

   @Override
   public int evaluate(Environment environment) {
      Set<Ip> ipSet = environment.getIpSets().get(_caller);
      if (ipSet == null) {
         throw new BatfishException(
               "Attempt to get size of undefined ip set: \"" + _caller + "\"");
      }
      else {
         return ipSet.size();
      }
   }

}
