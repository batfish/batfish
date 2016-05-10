package org.batfish.question.ip_set_expr;

import java.util.Set;

import org.batfish.datamodel.Ip;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface IpSetExpr extends Expr {

   @Override
   public Set<Ip> evaluate(Environment environment);

}
