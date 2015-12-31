package org.batfish.question.map_expr.map;

import org.batfish.question.map_expr.BaseMapExpr;
import org.batfish.question.map_expr.MapExpr;

public abstract class MapMapExpr extends BaseMapExpr {

   protected final MapExpr _caller;

   public MapMapExpr(MapExpr caller) {
      _caller = caller;
   }

}
