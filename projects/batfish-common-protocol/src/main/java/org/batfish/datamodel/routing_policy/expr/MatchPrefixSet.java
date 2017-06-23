package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchPrefixSet extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixExpr _prefix;

   private PrefixSetExpr _prefixSet;

   @JsonCreator
   private MatchPrefixSet() {
   }

   public MatchPrefixSet(PrefixExpr prefix, PrefixSetExpr prefixSet) {
      _prefix = prefix;
      _prefixSet = prefixSet;
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
      MatchPrefixSet other = (MatchPrefixSet) obj;
      if (_prefix == null) {
         if (other._prefix != null) {
            return false;
         }
      }
      else if (!_prefix.equals(other._prefix)) {
         return false;
      }
      if (_prefixSet == null) {
         if (other._prefixSet != null) {
            return false;
         }
      }
      else if (!_prefixSet.equals(other._prefixSet)) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_prefix == null) ? 0 : _prefix.hashCode());
      result = prime * result
            + ((_prefixSet == null) ? 0 : _prefixSet.hashCode());
      return result;
   }

   public void setPrefix(PrefixExpr prefix) {
      _prefix = prefix;
   }

   public void setPrefixSet(PrefixSetExpr prefixSet) {
      _prefixSet = prefixSet;
   }

}
