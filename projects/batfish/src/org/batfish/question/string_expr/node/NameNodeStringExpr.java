package org.batfish.question.string_expr.node;

import org.batfish.datamodel.Configuration;
import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public class NameNodeStringExpr extends BaseStringExpr {

   private NodeExpr _caller;

   public NameNodeStringExpr(NodeExpr caller) {
      _caller = caller;
   }

   @Override
   public String evaluate(Environment environment) {
      Configuration node = _caller.evaluate(environment);
      return node.getHostname();
   }

}
