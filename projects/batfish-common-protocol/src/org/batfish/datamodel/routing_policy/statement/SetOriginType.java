package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetOriginType extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private OriginType _originType;

   @JsonCreator
   public SetOriginType() {
   }

   public SetOriginType(OriginType originType) {
      _originType = originType;
   }

   @Override
   public Result execute(Environment environment, Route route) {
      Result result = new Result();
      result.setReturn(false);
      return result;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   public void setOriginType(OriginType originType) {
      _originType = originType;
   }

}
