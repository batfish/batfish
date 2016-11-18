package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedPrefix6Set implements Prefix6SetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   public NamedPrefix6Set() {
   }

   public NamedPrefix6Set(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public boolean matches(Prefix6 prefix, Environment environment) {
      Route6FilterList list = environment.getConfiguration()
            .getRoute6FilterLists().get(_name);
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
