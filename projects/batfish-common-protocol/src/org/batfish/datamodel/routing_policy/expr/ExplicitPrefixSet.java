package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitPrefixSet implements PrefixSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixSpace _prefixSpace;

   @JsonCreator
   public ExplicitPrefixSet() {
   }

   public ExplicitPrefixSet(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
   }

   public PrefixSpace getPrefixSpace() {
      return _prefixSpace;
   }

   @Override
   public boolean matches(Environment environment) {
      Prefix prefix = environment.getOriginalRoute().getNetwork();
      boolean value = _prefixSpace.containsPrefix(prefix);
      return value;
   }

   public void setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
   }

}
