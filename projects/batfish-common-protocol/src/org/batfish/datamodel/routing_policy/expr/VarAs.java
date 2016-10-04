package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class VarAs implements AsExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   @JsonCreator
   public VarAs() {
   }

   public VarAs(String var) {
      _var = var;
   }

   @Override
   public int evaluate(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public String getVar() {
      return _var;
   }

   public void setVar(String var) {
      _var = var;
   }

}
