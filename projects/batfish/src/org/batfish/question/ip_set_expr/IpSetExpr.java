package org.batfish.question.ip_set_expr;

import java.util.Set;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.Ip;

public interface IpSetExpr extends Expr {

   @Override
   public Set<Ip> evaluate(Environment environment);

}
