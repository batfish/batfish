package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedPrefixSet implements PrefixSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   public NamedPrefixSet() {
   }

   public NamedPrefixSet(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public boolean matches(Environment environment) {
      Prefix prefix = environment.getOriginalRoute().getNetwork();
      RouteFilterList list = environment.getConfiguration()
            .getRouteFilterLists().get(_name);
      if (list != null) {
         return list.permits(prefix);
      }
      else {
         environment.setError(true);
         return false;
      }
   }

   public void setName(String name) {
      _name = name;
   }

}
