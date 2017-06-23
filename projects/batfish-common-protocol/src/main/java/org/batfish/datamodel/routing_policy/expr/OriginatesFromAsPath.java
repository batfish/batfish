package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OriginatesFromAsPath extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<SubRangeExpr> _asRange;

   private boolean _exact;

   @JsonCreator
   private OriginatesFromAsPath() {
   }

   public OriginatesFromAsPath(List<SubRangeExpr> asRange, boolean exact) {
      _asRange = asRange;
      _exact = exact;
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
      OriginatesFromAsPath other = (OriginatesFromAsPath) obj;
      if (_asRange == null) {
         if (other._asRange != null) {
            return false;
         }
      }
      else if (!_asRange.equals(other._asRange)) {
         return false;
      }
      if (_exact != other._exact) {
         return false;
      }
      return true;
   }

   @Override
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public List<SubRangeExpr> getAsRange() {
      return _asRange;
   }

   public boolean getExact() {
      return _exact;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_asRange == null) ? 0 : _asRange.hashCode());
      result = prime * result + (_exact ? 1231 : 1237);
      return result;
   }

   public void setAsRange(List<SubRangeExpr> asRange) {
      _asRange = asRange;
   }

   public void setExact(boolean exact) {
      _exact = exact;
   }

}
