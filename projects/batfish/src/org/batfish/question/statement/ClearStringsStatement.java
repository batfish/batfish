package org.batfish.question.statement;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public class ClearStringsStatement implements Statement {

   private final String _caller;

   public ClearStringsStatement(String caller) {
      _caller = caller;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Map<String, Set<String>> stringSets = environment.getStringSets();
      Set<String> stringSet = stringSets.get(_caller);
      if (stringSet == null) {
         stringSet = new HashSet<String>();
         stringSets.put(_caller, stringSet);
      }
      else {
         stringSet.clear();
      }
   }

}
