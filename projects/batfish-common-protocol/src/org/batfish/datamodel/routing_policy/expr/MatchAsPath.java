package org.batfish.datamodel.routing_policy.expr;

public class MatchAsPath extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _list;

   public MatchAsPath(String list) {
      _list = list;
   }

   public String getList() {
      return _list;
   }

   public void setList(String list) {
      _list = list;
   }

}
