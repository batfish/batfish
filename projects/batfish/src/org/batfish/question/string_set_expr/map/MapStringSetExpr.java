package org.batfish.question.string_set_expr.map;

import org.batfish.question.map_expr.MapExpr;
import org.batfish.question.string_set_expr.BaseStringSetExpr;

public abstract class MapStringSetExpr extends BaseStringSetExpr {

   protected final MapExpr _caller;

   public MapStringSetExpr(MapExpr caller) {
      _caller = caller;
   }

}
