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
   private SubRangeExpr() {
   }

   public SubRangeExpr(IntExpr first, IntExpr last) {
      _first = first;
      _last = last;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      SubRangeExpr other = (SubRangeExpr) obj;
      if (_first == null) {
         if (other._first != null) {
            return false;
         }
      }
      else if (!_first.equals(other._first)) {
         return false;
      }
      if (_last == null) {
         if (other._last != null) {
            return false;
         }
      }
      else if (!_last.equals(other._last)) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_first == null) ? 0 : _first.hashCode());
      result = prime * result + ((_last == null) ? 0 : _last.hashCode());
      return result;
   }

   public void setFirst(IntExpr first) {
      _first = first;
   }

   public void setLast(IntExpr last) {
      _last = last;
   }

}
