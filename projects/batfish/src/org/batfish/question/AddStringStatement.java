package org.batfish.question;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;

public class AddStringStatement implements Statement {

   private PrintableExpr _pExpr;

   private String _target;

   public AddStringStatement(String target, PrintableExpr pExpr) {
      _target = target;
      _pExpr = pExpr;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Map<String, Set<String>> stringSets = environment.getStringSets();
      Set<String> stringSet = stringSets.get(_target);
      if (stringSet == null) {
         stringSet = new HashSet<String>();
         stringSets.put(_target, stringSet);
      }
      String string = _pExpr.print(environment);
      stringSet.add(string);
   }

}
