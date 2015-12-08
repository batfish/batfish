package org.batfish.question.prefix_space_expr;

import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;
import org.batfish.representation.Configuration;
import org.batfish.representation.PrefixSpace;

public class BgpOriginationSpaceExplicitExpr extends BasePrefixSpaceExpr {

   private NodeExpr _caller;

   public BgpOriginationSpaceExplicitExpr(NodeExpr caller) {
      _caller = caller;
   }

   @Override
   public PrefixSpace evaluate(Environment environment) {
      environment.initBgpOriginationSpaceExplicit();
      Configuration caller = _caller.evaluate(environment);
      return caller.getBgpProcess().getOriginationSpace();
   }

}
