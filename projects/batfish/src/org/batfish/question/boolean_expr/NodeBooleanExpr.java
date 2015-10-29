package org.batfish.question.boolean_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;

public enum NodeBooleanExpr implements BooleanExpr {
   NODE_BGP_CONFIGURED,
   NODE_BGP_HAS_GENERATED_ROUTE,
   NODE_HAS_GENERATED_ROUTE,
   NODE_ISIS_CONFIGURED,
   NODE_OSPF_CONFIGURED,
   NODE_STATIC_CONFIGURED;

   @Override
   public Boolean evaluate(Environment environment) {
      Configuration node = environment.getNode();
      BgpProcess bgpProcess = node.getBgpProcess();
      switch (this) {

      case NODE_BGP_CONFIGURED:
         return node.getBgpProcess() != null;

      case NODE_BGP_HAS_GENERATED_ROUTE:
         if (bgpProcess == null) {
            return false;
         }
         else {
            return bgpProcess.getGeneratedRoutes().size() > 0;
         }

      case NODE_HAS_GENERATED_ROUTE:
         return node.getGeneratedRoutes().size() > 0;

      case NODE_ISIS_CONFIGURED:
         return node.getIsisProcess() != null;

      case NODE_OSPF_CONFIGURED:
         return node.getOspfProcess() != null;

      case NODE_STATIC_CONFIGURED:
         return node.getStaticRoutes().size() > 0;

      default:
         throw new BatfishException("invalid node boolean expr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseBooleanExpr.print(this, environment);
   }

}
