package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DeleteCommunity extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _list;

   @JsonCreator
   public DeleteCommunity() {
   }

   public DeleteCommunity(String list) {
      _list = list;
   }

   public String getList() {
      return _list;
   }

   public void setList(String list) {
      _list = list;
   }

}
