package org.batfish.z3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.node.AcceptExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.BooleanExpr;
import org.batfish.z3.node.DebugExpr;
import org.batfish.z3.node.DropExpr;
import org.batfish.z3.node.NodeAcceptExpr;
import org.batfish.z3.node.NodeDropExpr;
import org.batfish.z3.node.OrExpr;
import org.batfish.z3.node.OriginateExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.QueryRelationExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class ReachabilityQuerySynthesizer extends BaseQuerySynthesizer {

   private Set<ForwardingAction> _actions;

   private Set<String> _finalNodes;

   private HeaderSpace _headerSpace;

   private Set<String> _ingressNodes;

   public ReachabilityQuerySynthesizer(Set<ForwardingAction> actions,
         HeaderSpace headerSpace, Set<String> finalNodes,
         Set<String> ingressNodes) {
      _actions = actions;
      _finalNodes = finalNodes;
      _headerSpace = headerSpace;
      _ingressNodes = ingressNodes;
   }

   @Override
   public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
      NodProgram program = new NodProgram(baseProgram.getContext());

      // create rules for injecting symbolic packets into ingress node(s)
      List<RuleExpr> originateRules = new ArrayList<>();
      for (String ingressNode : _ingressNodes) {
         OriginateExpr originate = new OriginateExpr(ingressNode);
         RuleExpr originateRule = new RuleExpr(originate);
         originateRules.add(originateRule);
      }

      AndExpr queryConditions = new AndExpr();

      // create query condition for action at final node(s)
      OrExpr finalActions = new OrExpr();
      for (ForwardingAction action : _actions) {
         switch (action) {
         case ACCEPT:
            if (_finalNodes.size() > 0) {
               for (String finalNode : _finalNodes) {
                  NodeAcceptExpr accept = new NodeAcceptExpr(finalNode);
                  finalActions.addDisjunct(accept);
               }
            }
            else {
               finalActions.addDisjunct(AcceptExpr.INSTANCE);
            }
            break;

         case DEBUG:
            finalActions.addDisjunct(DebugExpr.INSTANCE);
            break;

         case DROP:
            if (_finalNodes.size() > 0) {
               for (String finalNode : _finalNodes) {
                  NodeDropExpr drop = new NodeDropExpr(finalNode);
                  finalActions.addDisjunct(drop);
               }
            }
            else {
               finalActions.addDisjunct(DropExpr.INSTANCE);
            }
            break;

         case DROP_ACL:
         case DROP_ACL_IN:
         case DROP_ACL_OUT:
         case DROP_NO_ROUTE:
         case DROP_NULL_ROUTE:
         case FORWARD:
         default:
            throw new BatfishException("unsupported action");
         }
      }
      queryConditions.addConjunct(finalActions);
      queryConditions.addConjunct(SaneExpr.INSTANCE);

      // add headerSpace constraints
      BooleanExpr matchHeaderSpace = Synthesizer.matchHeaderSpace(_headerSpace);
      queryConditions.addConjunct(matchHeaderSpace);

      RuleExpr queryRule = new RuleExpr(queryConditions,
            QueryRelationExpr.INSTANCE);
      List<BoolExpr> rules = program.getRules();
      for (RuleExpr originateRule : originateRules) {
         BoolExpr originateBoolExpr = originateRule.toBoolExpr(baseProgram);
         rules.add(originateBoolExpr);
      }
      rules.add(queryRule.toBoolExpr(baseProgram));
      QueryExpr query = new QueryExpr(QueryRelationExpr.INSTANCE);
      BoolExpr queryBoolExpr = query.toBoolExpr(baseProgram);
      program.getQueries().add(queryBoolExpr);
      return program;
   }

}
