package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Statements {
   DefaultAction,
   DeleteAllCommunities,
   ExitAccept,
   ExitReject,
   Return,
   ReturnFalse,
   ReturnLocalDefaultAction,
   ReturnTrue,
   SetDefaultActionAccept,
   SetDefaultActionReject,
   SetLocalDefaultActionAccept,
   SetLocalDefaultActionReject;

   public static class StaticStatement extends AbstractStatement {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private static final String TYPE_VAR = "type";

      private Statements _type;

      @JsonCreator
      public StaticStatement(@JsonProperty(TYPE_VAR) Statements type) {
         _type = type;
      }

      @Override
      public boolean equals(Object rhs) {
         if (rhs instanceof StaticStatement) {
            return _type.equals(((StaticStatement) rhs)._type);
         }
         return false;
      }

      @JsonProperty(TYPE_VAR)
      public Statements getType() {
         return _type;
      }

   }

   public StaticStatement toStaticStatement() {
      return new StaticStatement(this);
   }

}
