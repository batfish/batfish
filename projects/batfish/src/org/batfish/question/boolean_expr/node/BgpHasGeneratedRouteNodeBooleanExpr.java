package org.batfish.question.boolean_expr.node;

import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;

public final class BgpHasGeneratedRouteNodeBooleanExpr extends NodeBooleanExpr {

   public BgpHasGeneratedRouteNodeBooleanExpr(NodeExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      Configuration node = _caller.evaluate(environment);
      BgpProcess bgpProcess = node.getBgpProcess();
      if (bgpProcess == null) {
         return false;
      }
      else {
         return bgpProcess.getGeneratedRoutes().size() > 0;
      }
   }

}
