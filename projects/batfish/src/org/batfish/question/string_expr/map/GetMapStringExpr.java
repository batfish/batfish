package org.batfish.question.string_expr.map;

import org.batfish.common.BatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.QMap;
import org.batfish.question.map_expr.MapExpr;

public class GetMapStringExpr extends MapStringExpr {

   private final Expr _key;

   public GetMapStringExpr(MapExpr caller, Expr key) {
      super(caller);
      _key = key;
   }

   @Override
   public String evaluate(Environment environment) {
      QMap caller = _caller.evaluate(environment);
      String key = _key.print(environment);
      VariableType actualType = caller.getTypeBindings().get(key);
      if (actualType == null) {
         caller.getStrings().put(key, "");
         return "";
      }
      else if (actualType != VariableType.STRING) {
         throw new BatfishException("Field: \"" + key + "\" is of type: \""
               + actualType + "\" instead of expected type: \"STRING\"");
      }
      else {
         return caller.getStrings().get(key);
      }
   }

}
