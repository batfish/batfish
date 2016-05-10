package org.batfish.question.ip_expr;

import org.batfish.datamodel.Ip;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface IpExpr extends Expr {

   @Override
   Ip evaluate(Environment environment);

}
