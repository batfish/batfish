package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchSourceInterface extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _srcInterface;

   @JsonCreator
   public MatchSourceInterface() {
   }

   public MatchSourceInterface(String srcInterface) {
      _srcInterface = srcInterface;
   }

   @Override
   public Result evaluate(Environment environment,
         AbstractRouteBuilder<?> outputRoute) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public String getList() {
      return _srcInterface;
   }

   public void setList(String srcInterface) {
      _srcInterface = srcInterface;
   }

}
