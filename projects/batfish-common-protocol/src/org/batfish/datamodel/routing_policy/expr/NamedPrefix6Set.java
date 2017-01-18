package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedPrefix6Set extends Prefix6SetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   private NamedPrefix6Set() {
   }

   public NamedPrefix6Set(String name) {
      _name = name;
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
      NamedPrefix6Set other = (NamedPrefix6Set) obj;
      if (_name == null) {
         if (other._name != null) {
            return false;
         }
      }
      else if (!_name.equals(other._name)) {
         return false;
      }
      return true;
   }

   public String getName() {
      return _name;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_name == null) ? 0 : _name.hashCode());
      return result;
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
