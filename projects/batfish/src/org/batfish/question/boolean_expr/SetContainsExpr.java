package org.batfish.question.boolean_expr;

import org.batfish.common.BatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.int_expr.IntExpr;
import org.batfish.question.ip_expr.IpExpr;
import org.batfish.question.prefix_expr.PrefixExpr;
import org.batfish.question.route_filter_expr.RouteFilterExpr;
import org.batfish.question.string_expr.StringExpr;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterList;

public class SetContainsExpr extends BaseBooleanExpr {

   private final String _caller;

   private final Expr _expr;

   private final VariableType _type;

   public SetContainsExpr(Expr expr, String caller, VariableType type) {
      _expr = expr;
      _caller = caller;
      _type = type;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      switch (_type) {
      case SET_INT:
         IntExpr intExpr = (IntExpr) _expr;
         int intVal = intExpr.evaluate(environment);
         return environment.getIntegerSets().get(_caller).contains(intVal);

      case SET_IP:
         IpExpr ipExpr = (IpExpr) _expr;
         Ip ipVal = ipExpr.evaluate(environment);
         return environment.getIpSets().get(_caller).contains(ipVal);

      case SET_PREFIX:
         PrefixExpr prefixExpr = (PrefixExpr) _expr;
         Prefix prefixVal = prefixExpr.evaluate(environment);
         return environment.getPrefixSets().get(_caller).contains(prefixVal);

      case SET_ROUTE_FILTER:
         RouteFilterExpr routeFilterExpr = (RouteFilterExpr) _expr;
         RouteFilterList routeFilterVal = routeFilterExpr.evaluate(environment);
         return environment.getRouteFilterSets().get(_caller)
               .contains(routeFilterVal);

      case SET_STRING:
         StringExpr stringExpr = (StringExpr) _expr;
         String stringVal = stringExpr.evaluate(environment);
         return environment.getStringSets().get(_caller).contains(stringVal);

      case ACTION:
      case INT:
      case IP:
      case PREFIX:
      case RANGE:
      case REGEX:
      case ROUTE_FILTER:
      case STRING:
      default:
         throw new BatfishException("invalid set type");
      }
   }

}
