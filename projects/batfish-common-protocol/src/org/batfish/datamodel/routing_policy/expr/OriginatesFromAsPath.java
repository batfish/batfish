package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class OriginatesFromAsPath extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<SubRangeExpr> _asRange;

   private boolean _exact;

   @JsonCreator
   public OriginatesFromAsPath() {
   }

   public OriginatesFromAsPath(List<SubRangeExpr> asRange, boolean exact) {
      _asRange = asRange;
      _exact = exact;
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

   public void setAsRange(List<SubRangeExpr> asRange) {
      _asRange = asRange;
   }

   public void setExact(boolean exact) {
      _exact = exact;
   }

}
