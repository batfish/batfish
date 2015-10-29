package org.batfish.question.ip_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.Ip;

public interface IpExpr extends Expr {

   @Override
   Ip evaluate(Environment environment);

}
