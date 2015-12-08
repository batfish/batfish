package org.batfish.question.prefix_space_expr.node;

import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;
import org.batfish.representation.Configuration;
import org.batfish.representation.PrefixSpace;

public class BgpOriginationSpaceExplicitNodePrefixSpaceExpr extends NodePrefixSpaceExpr {

   public BgpOriginationSpaceExplicitNodePrefixSpaceExpr(NodeExpr caller) {
      super(caller);
   }

   @Override
   public PrefixSpace evaluate(Environment environment) {
      environment.initBgpOriginationSpaceExplicit();
      Configuration caller = _caller.evaluate(environment);
      return caller.getBgpProcess().getOriginationSpace();
   }

}
