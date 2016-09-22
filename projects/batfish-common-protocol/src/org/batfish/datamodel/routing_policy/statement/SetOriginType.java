package org.batfish.datamodel.routing_policy.statement;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.OriginType;
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
   public Result execute(Environment environment,
         AbstractRouteBuilder<?> route) {
      throw new BatfishException("unimplemented");
   }

   public OriginType getOriginType() {
      return _originType;
   }

   public void setOriginType(OriginType originType) {
      _originType = originType;
   }

}
