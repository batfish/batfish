package org.batfish.question.string_expr.map;

import org.batfish.question.map_expr.MapExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public abstract class MapStringExpr extends BaseStringExpr {

   protected final MapExpr _caller;

   public MapStringExpr(MapExpr caller) {
      _caller = caller;
   }

}
