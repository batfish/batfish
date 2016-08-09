package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

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

   @Override
   public Result execute(Environment environment, Route route) {
      environment.setBuffered(true);
      Result result = _statement.execute(environment, route);
      return result;
   }

   public Statement getStatement() {
      return _statement;
   }

   public void setStatement(Statement statement) {
      _statement = statement;
   }

}
