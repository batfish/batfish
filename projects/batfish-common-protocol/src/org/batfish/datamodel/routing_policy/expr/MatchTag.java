package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchTag extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _tag;

   public MatchTag(int tag) {
      _tag = tag;
   }

   @Override
   public Result evaluate(Environment environment, Route route) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public int getTag() {
      return _tag;
   }

   public void setTag(int tag) {
      _tag = tag;
   }

}
