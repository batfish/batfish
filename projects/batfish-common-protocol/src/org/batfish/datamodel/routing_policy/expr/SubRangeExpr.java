package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SubRangeExpr implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntExpr _first;

   private IntExpr _last;

   @JsonCreator
   public SubRangeExpr() {
   }

   public SubRangeExpr(IntExpr first, IntExpr last) {
      _first = first;
      _last = last;
   }

   public SubRange evaluate(Environment env) {
      int first = _first.evaluate(env);
      int last = _last.evaluate(env);
      return new SubRange(first, last);
   }

   public IntExpr getFirst() {
      return _first;
   }

   public IntExpr getLast() {
      return _last;
   }

   public void setFirst(IntExpr first) {
      _first = first;
   }

   public void setLast(IntExpr last) {
      _last = last;
   }

}
