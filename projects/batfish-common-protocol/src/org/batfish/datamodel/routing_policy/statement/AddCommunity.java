package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AddCommunity extends AbstractStatement implements Statement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private long _community;

   @JsonCreator
   public AddCommunity() {
   }

   public AddCommunity(Long community) {
      _community = community;
   }

   public long getCommunity() {
      return _community;
   }

   public void setCommunity(long community) {
      _community = community;
   }

}
