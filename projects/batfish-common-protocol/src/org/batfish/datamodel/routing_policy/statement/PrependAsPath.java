package org.batfish.datamodel.routing_policy.statement;

import java.util.List;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PrependAsPath extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<Integer> _asList;

   @JsonCreator
   public PrependAsPath() {
   }

   public PrependAsPath(List<Integer> asList) {
      _asList = asList;
   }

   @Override
   public Result execute(Environment environment,
         AbstractRouteBuilder<?> route) {
      Result result = new Result();
      return result;
   }

   public List<Integer> getAsList() {
      return _asList;
   }

   public void setAsList(List<Integer> asList) {
      _asList = asList;
   }

}
