package org.batfish.datamodel.routing_policy.statement;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetOrigin extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private OriginExpr _origin;

   @JsonCreator
   public SetOrigin() {
   }

   public SetOrigin(OriginExpr origin) {
      _origin = origin;
   }

   @Override
   public Result execute(Environment environment) {
      throw new BatfishException("unimplemented");
   }

   public OriginExpr getOriginType() {
      return _origin;
   }

   public void setOriginType(OriginExpr origin) {
      _origin = origin;
   }

}
