package org.batfish.question.statement;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.grammar.question.VariableType;
import org.batfish.main.Settings;
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

public class SetAddStatement implements Statement {

   private final String _caller;

   private final Expr _expr;

   private final VariableType _type;

   public SetAddStatement(Expr expr, String caller, VariableType type) {
      _expr = expr;
      _caller = caller;
      _type = type;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      switch (_type) {
      case SET_INT:
         IntExpr intExpr = (IntExpr) _expr;
         int intVal = intExpr.evaluate(environment);
         environment.getIntegerSets().get(_caller).add(intVal);
         break;

      case SET_IP:
         IpExpr ipExpr = (IpExpr) _expr;
         Ip ipVal = ipExpr.evaluate(environment);
         environment.getIpSets().get(_caller).add(ipVal);
         break;

      case SET_PREFIX:
         PrefixExpr prefixExpr = (PrefixExpr) _expr;
         Prefix prefixVal = prefixExpr.evaluate(environment);
         environment.getPrefixSets().get(_caller).add(prefixVal);
         break;

      case SET_ROUTE_FILTER:
         RouteFilterExpr routeFilterExpr = (RouteFilterExpr) _expr;
         RouteFilterList routeFilterVal = routeFilterExpr.evaluate(environment);
         environment.getRouteFilterSets().get(_caller).add(routeFilterVal);
         break;

      case SET_STRING:
         StringExpr stringExpr = (StringExpr) _expr;
         String stringVal = stringExpr.evaluate(environment);
         environment.getStringSets().get(_caller).add(stringVal);
         break;

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
