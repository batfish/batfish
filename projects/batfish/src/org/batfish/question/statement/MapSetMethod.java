package org.batfish.question.statement;

import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.questions.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.QMap;
import org.batfish.question.map_expr.MapExpr;

public final class MapSetMethod implements Statement {

   private final MapExpr _caller;

   private final Expr _key;

   private final Expr _value;

   public MapSetMethod(MapExpr caller, Expr key, Expr value) {
      _caller = caller;
      _key = key;
      _value = value;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      QMap caller = _caller.evaluate(environment);
      String key = _key.print(environment);
      String value = _value.print(environment);
      caller.getTypeBindings().put(key, VariableType.STRING);
      caller.getStrings().put(key, value);
   }

}
