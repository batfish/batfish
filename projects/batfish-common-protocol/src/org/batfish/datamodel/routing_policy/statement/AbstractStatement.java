package org.batfish.datamodel.routing_policy.statement;

import java.util.Collections;
import java.util.List;

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

   @Override
   public List<Statement> simplify() {
      return Collections.singletonList(this);
   }

}
