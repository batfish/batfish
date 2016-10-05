package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MultipliedAs implements AsPathListExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AsExpr _expr;

   private IntExpr _number;

   @JsonCreator
   public MultipliedAs() {
   }

   public MultipliedAs(AsExpr expr, IntExpr number) {
      _expr = expr;
      _number = number;
   }

   @Override
   public List<Integer> evaluate(Environment environment) {
      List<Integer> list = new ArrayList<>();
      int as = _expr.evaluate(environment);
      int number = _number.evaluate(environment);
      for (int i = 0; i < number; i++) {
         list.add(as);
      }
      return list;
   }

   public AsExpr getExpr() {
      return _expr;
   }

   public IntExpr getNumber() {
      return _number;
   }

   public void setExpr(AsExpr expr) {
      _expr = expr;
   }

   public void setNumber(IntExpr number) {
      _number = number;
   }

}
