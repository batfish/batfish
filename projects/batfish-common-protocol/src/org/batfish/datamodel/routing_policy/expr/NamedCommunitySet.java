package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedCommunitySet implements CommunitySetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   @JsonCreator
   public NamedCommunitySet() {
   }

   public NamedCommunitySet(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   public void setName(String name) {
      _name = name;
   }

}
