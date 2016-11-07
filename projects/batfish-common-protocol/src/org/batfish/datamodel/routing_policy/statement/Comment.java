package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class Comment extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public Comment(String text) {
      setComment(text);
   }

   @Override
   public Result execute(Environment environment) {
      return new Result();
   }

}
