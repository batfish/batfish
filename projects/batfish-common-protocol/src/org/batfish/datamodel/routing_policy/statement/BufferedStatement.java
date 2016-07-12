package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;

public class BufferedStatement extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Statement _statement;

   @JsonCreator
   public BufferedStatement() {
   }

   public BufferedStatement(Statement statement) {
      _statement = statement;
   }

   public Statement getStatement() {
      return _statement;
   }

   public void setStatement(Statement statement) {
      _statement = statement;
   }

}
