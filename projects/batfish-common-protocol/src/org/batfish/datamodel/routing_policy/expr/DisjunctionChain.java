package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Juniper subroutine chain
 */
public class DisjunctionChain extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<BooleanExpr> _subroutines;

   @JsonCreator
   public DisjunctionChain() {
   }

   public DisjunctionChain(List<BooleanExpr> subroutines) {
      _subroutines = subroutines;
   }

   @Override
   public Result evaluate(Environment environment) {
      Result subroutineResult = new Result();
      subroutineResult.setFallThrough(true);
      for (BooleanExpr subroutine : _subroutines) {
         subroutineResult = subroutine.evaluate(environment);
         if (subroutineResult.getExit()) {
            return subroutineResult;
         }
         else if (!subroutineResult.getFallThrough()
               && subroutineResult.getBooleanValue()) {
            subroutineResult.setReturn(true);
            return subroutineResult;
         }
      }
      if (!subroutineResult.getFallThrough()) {
         return subroutineResult;
      }
      else {
         String defaultPolicy = environment.getDefaultPolicy();
         if (defaultPolicy != null) {
            CallExpr callDefaultPolicy = new CallExpr(
                  environment.getDefaultPolicy());
            Result defaultPolicyResult = callDefaultPolicy
                  .evaluate(environment);
            return defaultPolicyResult;
         }
         else {
            throw new BatfishException("Default policy not set");
         }
      }
   }

   public List<BooleanExpr> getSubroutines() {
      return _subroutines;
   }

   public void setSubroutines(List<BooleanExpr> subroutines) {
      _subroutines = subroutines;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "<" + _subroutines.toString() + ">";
   }

}
