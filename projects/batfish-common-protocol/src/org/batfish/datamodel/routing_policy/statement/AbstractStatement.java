package org.batfish.datamodel.routing_policy.statement;

public abstract class AbstractStatement implements Statement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _comment;

   public String getComment() {
      return _comment;
   }

   public void setComment(String comment) {
      _comment = comment;
   }

}
