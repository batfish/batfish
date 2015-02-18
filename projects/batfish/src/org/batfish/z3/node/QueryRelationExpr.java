package org.batfish.z3.node;

public class QueryRelationExpr extends PacketRelExpr {

   public static final QueryRelationExpr INSTANCE = new QueryRelationExpr();

   public static final String NAME = "query_relation";

   private QueryRelationExpr() {
      super(NAME);
   }

}
