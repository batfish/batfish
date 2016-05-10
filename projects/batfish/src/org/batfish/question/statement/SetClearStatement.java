package org.batfish.question.statement;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.questions.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;

public class SetClearStatement implements Statement {

   private VariableType _type;

   private String _var;

   public SetClearStatement(String var, VariableType type) {
      _var = var;
      _type = type;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      switch (_type) {
      case SET_NODE:
         environment.getNodeSets().get(_var).clear();
         break;

      case SET_PREFIX:
         environment.getPrefixSets().get(_var).clear();
         break;

      case SET_BGP_NEIGHBOR:
      case SET_INT:
      case SET_INTERFACE:
      case SET_IP:
      case SET_IPSEC_VPN:
      case SET_POLICY_MAP:
      case SET_POLICY_MAP_CLAUSE:
      case SET_PREFIX_SPACE:
      case SET_ROUTE_FILTER:
      case SET_ROUTE_FILTER_LINE:
      case SET_STATIC_ROUTE:
      case SET_STRING:
         throw new BatfishException("Not implemented for set type: " + _type);
         // $CASES-OMITTED$
      default:
         throw new BatfishException("Not a set type or not implemented");

      }
   }

}
