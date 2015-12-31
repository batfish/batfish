package org.batfish.question.string_set_expr.map;

import java.util.LinkedHashSet;
import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.question.QMap;
import org.batfish.question.map_expr.MapExpr;

public class KeysMapStringSetExpr extends MapStringSetExpr {

   public KeysMapStringSetExpr(MapExpr caller) {
      super(caller);
   }

   @Override
   public Set<String> evaluate(Environment environment) {
      QMap caller = _caller.evaluate(environment);
      Set<String> keys = new LinkedHashSet<String>();
      keys.addAll(caller.getStrings().keySet());
      keys.addAll(caller.getMaps().keySet());
      keys.remove("name");
      return keys;
   }

}
