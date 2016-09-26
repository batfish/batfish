package org.batfish.datamodel.routing_policy.expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedAsPathSet implements AsPathSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   public NamedAsPathSet() {
   }

   public NamedAsPathSet(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public boolean matches(Environment environment) {
      throw new BatfishException("unimplemented");
   }

   public void setName(String name) {
      _name = name;
   }

}
