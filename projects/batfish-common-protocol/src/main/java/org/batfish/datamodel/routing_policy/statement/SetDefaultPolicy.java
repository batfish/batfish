package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetDefaultPolicy extends Statement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _defaultPolicy;

   @JsonCreator
   private SetDefaultPolicy() {
   }

   public SetDefaultPolicy(String defaultPolicy) {
      _defaultPolicy = defaultPolicy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      SetDefaultPolicy other = (SetDefaultPolicy) obj;
      if (_defaultPolicy == null) {
         if (other._defaultPolicy != null) {
            return false;
         }
      }
      else if (!_defaultPolicy.equals(other._defaultPolicy)) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_defaultPolicy == null) ? 0 : _defaultPolicy.hashCode());
      return result;
   }

   public void setDefaultPolicy(String defaultPolicy) {
      _defaultPolicy = defaultPolicy;
   }

}
