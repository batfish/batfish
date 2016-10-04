package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetVarMetricType extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   @JsonCreator
   public SetVarMetricType() {
   }

   public SetVarMetricType(String var) {
      _var = var;
   }

   @Override
   public Result execute(Environment environment) {
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
