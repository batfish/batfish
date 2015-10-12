package org.batfish.question;

import java.util.Set;

import org.batfish.main.BatfishException;

public class NumStringsIntExpr extends BaseIntExpr {

   private final String _caller;

   public NumStringsIntExpr(String caller) {
      _caller = caller;
   }

   @Override
   public int evaluate(Environment environment) {
      Set<String> stringSet = environment.getStringSets().get(_caller);
      if (stringSet == null) {
         throw new BatfishException(
               "Attempt to get size of undefined string set: \"" + _caller
                     + "\"");
      }
      else {
         return stringSet.size();
      }
   }

}
