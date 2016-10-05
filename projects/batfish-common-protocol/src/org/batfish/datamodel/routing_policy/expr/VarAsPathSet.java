package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class VarAsPathSet implements AsPathSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   @JsonCreator
   public VarAsPathSet() {
   }

   public VarAsPathSet(String var) {
      _var = var;
   }

   public String getVar() {
      return _var;
   }

   @Override
   public boolean matches(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setVar(String var) {
      _var = var;
   }

}
