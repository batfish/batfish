package org.batfish.question.ip_expr;

import org.batfish.common.datamodel.Ip;
import org.batfish.question.Environment;

public final class LiteralIpExpr extends BaseIpExpr {

   private final Ip _ip;

   public LiteralIpExpr(Ip ip) {
      _ip = ip;
   }

   @Override
   public Ip evaluate(Environment environment) {
      return _ip;
   }

}
