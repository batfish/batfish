package org.batfish.question.prefix_space_expr.node;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.question.Environment;
import org.batfish.question.node_expr.NodeExpr;

public class BgpOriginationSpaceExplicitNodePrefixSpaceExpr extends
      NodePrefixSpaceExpr {

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
