package org.batfish.question.int_expr;

import org.batfish.common.BatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.question.Environment;

public class SetSizeIntExpr extends BaseIntExpr {

   private final String _caller;

   private final VariableType _type;

   public SetSizeIntExpr(String caller, VariableType type) {
      _caller = caller;
      _type = type;
   }

   @Override
   public Integer evaluate(Environment environment) {
      switch (_type) {
      case SET_INT:
         throw new BatfishException("todo");

      case SET_IP:
         return environment.getIpSets().get(_caller).size();

      case SET_PREFIX:
         return environment.getPrefixSets().get(_caller).size();

      case SET_ROUTE_FILTER:
         return environment.getRouteFilterSets().get(_caller).size();

      case SET_STRING:
         return environment.getStringSets().get(_caller).size();

      case ACTION:
      case INT:
      case IP:
      case PREFIX:
      case ROUTE_FILTER:
      case RANGE:
      case REGEX:
      case STRING:
      default:
         throw new BatfishException("invalid set type");
      }
   }

}
