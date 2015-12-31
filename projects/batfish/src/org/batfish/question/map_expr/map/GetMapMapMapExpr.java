package org.batfish.question.map_expr.map;

import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.QMap;
import org.batfish.question.map_expr.MapExpr;

public final class GetMapMapMapExpr extends MapMapExpr {

   private Expr _key;

   public GetMapMapMapExpr(MapExpr caller, Expr key) {
      super(caller);
      _key = key;
   }

   @Override
   public QMap evaluate(Environment environment) {
      String key = _key.print(environment);
      if (key == null) {
         throw new BatfishException("Cannot add map with null key");
      }
      QMap caller = _caller.evaluate(environment);
      Map<String, QMap> maps = caller.getMaps();
      QMap map = maps.get(key);
      if (map == null) {
         map = new QMap();
         caller.getTypeBindings().put(key, VariableType.MAP);
         maps.put(key, map);
      }
      else {
         VariableType actualType = caller.getTypeBindings().get(key);
         if (actualType != VariableType.MAP) {
            throw new BatfishException("Field: \"" + key + "\" is of type: \""
                  + actualType + "\" instead of expected type: \"MAP\"");
         }
      }
      return map;
   }

}
