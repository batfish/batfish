package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class VarRouteType implements RouteTypeExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   @JsonCreator
   public VarRouteType() {
   }

   public VarRouteType(String var) {
      _var = var;
   }

   @Override
   public RouteType evaluate(Environment environment) {
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
