package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetLocalPreference extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private int _localPreference;

   @JsonCreator
   public SetLocalPreference() {
   }

   public SetLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   public void setLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

}
