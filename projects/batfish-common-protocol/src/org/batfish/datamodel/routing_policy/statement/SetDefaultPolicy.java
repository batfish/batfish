package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetDefaultPolicy extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _defaultPolicy;

   @JsonCreator
   public SetDefaultPolicy() {
   }

   public SetDefaultPolicy(String defaultPolicy) {
      _defaultPolicy = defaultPolicy;
   }

   @Override
   public Result execute(Environment environment) {
      environment.setDefaultPolicy(_defaultPolicy);
      Result result = new Result();
      return result;
   }

   public String getDefaultPolicy() {
      return _defaultPolicy;
   }

   public void setDefaultPolicy(String defaultPolicy) {
      _defaultPolicy = defaultPolicy;
   }

}
