package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PassesThroughAsPath extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _exact;

   private List<SubRangeExpr> _range;

   @JsonCreator
   public PassesThroughAsPath() {
   }

   public PassesThroughAsPath(List<SubRangeExpr> range, boolean exact) {
      _range = range;
      _exact = exact;
   }

   @Override
   public Result evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public boolean getExact() {
      return _exact;
   }

   public List<SubRangeExpr> getRange() {
      return _range;
   }

   public void setExact(boolean exact) {
      _exact = exact;
   }

   public void setRange(List<SubRangeExpr> range) {
      _range = range;
   }

}
