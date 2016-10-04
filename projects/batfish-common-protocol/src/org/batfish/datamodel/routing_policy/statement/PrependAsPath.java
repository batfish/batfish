package org.batfish.datamodel.routing_policy.statement;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PrependAsPath extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AsPathListExpr _expr;

   @JsonCreator
   public PrependAsPath() {
   }

   public PrependAsPath(AsPathListExpr expr) {
      _expr = expr;
   }

   @Override
   public Result execute(Environment environment) {
      throw new BatfishException("unimplemented");
   }

   public AsPathListExpr getExpr() {
      return _expr;
   }

   public void setExpr(AsPathListExpr expr) {
      _expr = expr;
   }

}
