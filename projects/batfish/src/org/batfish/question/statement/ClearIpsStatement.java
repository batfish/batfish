package org.batfish.question.statement;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.representation.Ip;

public class ClearIpsStatement implements Statement {

   private final String _caller;

   public ClearIpsStatement(String caller) {
      _caller = caller;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Map<String, Set<Ip>> ipSets = environment.getIpSets();
      Set<Ip> ipSet = ipSets.get(_caller);
      if (ipSet == null) {
         ipSet = new HashSet<Ip>();
         ipSets.put(_caller, ipSet);
      }
      else {
         ipSet.clear();
      }
   }

}
