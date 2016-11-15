package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Comment extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @JsonCreator
   public Comment() {
   }

   public Comment(String text) {
      setComment(text);
   }

   @Override
   public Result execute(Environment environment) {
      return new Result();
   }

}
