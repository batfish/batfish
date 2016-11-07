package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchPrefixSet extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixExpr _prefix;

   private PrefixSetExpr _prefixSet;

   @JsonCreator
   public MatchPrefixSet() {
   }

   public MatchPrefixSet(PrefixExpr prefix, PrefixSetExpr prefixSet) {
      _prefix = prefix;
      _prefixSet = prefixSet;
   }

   @Override
   public Result evaluate(Environment environment) {
      Prefix prefix = _prefix.evaluate(environment);
      boolean match = _prefixSet.matches(prefix, environment);
      Result result = new Result();
      result.setBooleanValue(match);
      return result;
   }

   public PrefixExpr getPrefix() {
      return _prefix;
   }

   public PrefixSetExpr getPrefixSet() {
      return _prefixSet;
   }

   public void setPrefix(PrefixExpr prefix) {
      _prefix = prefix;
   }

   public void setPrefixSet(PrefixSetExpr prefixSet) {
      _prefixSet = prefixSet;
   }

}
