package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.Environment;

public class SubRangeExpr implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _first;

   private IntExpr _last;

   public SubRangeExpr(IntExpr first, IntExpr last) {
      _first = first;
      _last = last;
   }

   public SubRange evaluate(Environment env) {
      int first = _first.evaluate(env);
      int last = _last.evaluate(env);
      return new SubRange(first, last);
   }

}
