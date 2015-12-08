package org.batfish.question.ip_expr;

import org.batfish.question.Environment;
import org.batfish.representation.Ip;

public class VarIpExpr extends BaseIpExpr {

   private final String _var;

   public VarIpExpr(String var) {
      _var = var;
   }

   @Override
   public Ip evaluate(Environment environment) {
      return environment.getIps().get(_var);
   }

}
