package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchPrefix6Set extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Prefix6Expr _prefix;

   private Prefix6SetExpr _prefixSet;

   @JsonCreator
   public MatchPrefix6Set() {
   }

   public MatchPrefix6Set(Prefix6Expr prefix, Prefix6SetExpr prefixSet) {
      _prefix = prefix;
      _prefixSet = prefixSet;
   }

   @Override
   public Result evaluate(Environment environment) {
      Prefix6 prefix = _prefix.evaluate(environment);
      boolean match = _prefixSet.matches(prefix, environment);
      Result result = new Result();
      result.setBooleanValue(match);
      return result;
   }

   public Prefix6Expr getPrefix() {
      return _prefix;
   }

   public Prefix6SetExpr getPrefixSet() {
      return _prefixSet;
   }

   public void setPrefix(Prefix6Expr prefix) {
      _prefix = prefix;
   }

   public void setPrefixSet(Prefix6SetExpr prefixSet) {
      _prefixSet = prefixSet;
   }

}
