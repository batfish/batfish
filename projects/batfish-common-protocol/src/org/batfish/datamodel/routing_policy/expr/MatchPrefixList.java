package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchPrefixList extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _list;

   @JsonCreator
   public MatchPrefixList() {
   }

   public MatchPrefixList(String list) {
      _list = list;
   }

   public String getList() {
      return _list;
   }

   public void setList(String list) {
      _list = list;
   }

}
