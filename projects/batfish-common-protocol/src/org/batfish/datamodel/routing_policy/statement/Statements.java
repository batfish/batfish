package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Statements {
   DefaultAction,
   DeleteAllCommunities,
   ExitAccept,
   ExitReject,
   FallThrough,
   Return,
   ReturnFalse,
   ReturnLocalDefaultAction,
   ReturnTrue,
   SetDefaultActionAccept,
   SetDefaultActionReject,
   SetLocalDefaultActionAccept,
   SetLocalDefaultActionReject,
   SetReadIntermediateBgpAttributes,
   SetWriteIntermediateBgpAttributes,
   UnsetWriteIntermediateBgpAttributes;

   public static class StaticStatement extends Statement {
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

      @Override
      public Result execute(Environment environment) {
         Result result = new Result();
         switch (this._type) {
         case DefaultAction:
            result.setExit(true);
            result.setBooleanValue(environment.getDefaultAction());
            break;

         case DeleteAllCommunities:
            break;

         case ExitAccept:
            result.setExit(true);
            result.setBooleanValue(true);
            break;

         case ExitReject:
            result.setExit(true);
            result.setBooleanValue(false);
            break;

         case FallThrough:
            result.setReturn(true);
            result.setFallThrough(true);
            break;

         case Return:
            result.setReturn(true);
            break;

         case ReturnFalse:
            result.setReturn(true);
            result.setBooleanValue(false);
            break;

         case ReturnLocalDefaultAction:
            result.setReturn(true);
            result.setBooleanValue(environment.getLocalDefaultAction());
            break;

         case ReturnTrue:
            result.setReturn(true);
            result.setBooleanValue(true);
            break;

         case SetDefaultActionAccept:
            environment.setDefaultAction(true);
            break;

         case SetDefaultActionReject:
            environment.setDefaultAction(false);
            break;

         case SetLocalDefaultActionAccept:
            environment.setLocalDefaultAction(true);
            break;

         case SetLocalDefaultActionReject:
            environment.setLocalDefaultAction(false);
            break;

         case SetReadIntermediateBgpAttributes:
            environment.setReadFromIntermediateBgpAttributes(true);
            break;

         case SetWriteIntermediateBgpAttributes:
            if (environment.getIntermediateBgpAttributes() == null) {
               BgpRoute.Builder ir = new BgpRoute.Builder();
               environment.setIntermediateBgpAttributes(ir);
               AbstractRoute or = environment.getOriginalRoute();
               ir.setMetric(or.getMetric());
               ir.setTag(or.getTag());
            }
            environment.setWriteToIntermediateBgpAttributes(true);
            break;

         case UnsetWriteIntermediateBgpAttributes:
            environment.setWriteToIntermediateBgpAttributes(false);
            break;

         default:
            break;
         }
         return result;
      }

      @JsonProperty(TYPE_VAR)
      public Statements getType() {
         return _type;
      }

      @Override
      public int hashCode() {
         return _type.hashCode();
      }

      @Override
      public String toString() {
         return _type.toString();
      }

   }

   public StaticStatement toStaticStatement() {
      return new StaticStatement(this);
   }

}
