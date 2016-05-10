package org.batfish.question.string_expr.protocol;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.question.Environment;
import org.batfish.question.string_expr.BaseStringExpr;
import org.batfish.question.string_expr.StringExpr;

public enum ProtocolStringExpr implements StringExpr {
   PROTOCOL_NAME;

   @Override
   public String evaluate(Environment environment) {
      RoutingProtocol protocol = environment.getProtocol();
      switch (this) {

      case PROTOCOL_NAME:
         return protocol.protocolName();

      default:
         throw new BatfishException("invalid protocol string expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseStringExpr.print(this, environment);
   }

}
