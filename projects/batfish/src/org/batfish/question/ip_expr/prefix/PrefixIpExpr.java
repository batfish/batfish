package org.batfish.question.ip_expr.prefix;

import org.batfish.question.ip_expr.BaseIpExpr;
import org.batfish.question.prefix_expr.PrefixExpr;

public abstract class PrefixIpExpr extends BaseIpExpr {

   protected final PrefixExpr _caller;

   public PrefixIpExpr(PrefixExpr caller) {
      _caller = caller;
   }

}
