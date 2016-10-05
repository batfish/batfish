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

   private List<AsExpr> _list;

   @JsonCreator
   public PassesThroughAsPath() {
   }

   public PassesThroughAsPath(List<AsExpr> list, boolean exact) {
      _list = list;
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

   public List<AsExpr> getList() {
      return _list;
   }

   public void setExact(boolean exact) {
      _exact = exact;
   }

   public void setList(List<AsExpr> list) {
      _list = list;
   }

}
