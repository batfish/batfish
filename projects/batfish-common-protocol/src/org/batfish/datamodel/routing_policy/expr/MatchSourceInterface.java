package org.batfish.datamodel.routing_policy.expr;

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

   public String getList() {
      return _srcInterface;
   }

   public void setList(String srcInterface) {
      _srcInterface = srcInterface;
   }

}
