package org.batfish.question.statement;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.grammar.question.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.IpsecVpn;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.Prefix;
import org.batfish.representation.PrefixSpace;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.StaticRoute;

public class Assignment implements Statement {

   private final Expr _expr;

   private final VariableType _type;

   private final String _variable;

   public Assignment(String variable, Expr expr, VariableType type) {
      _variable = variable;
      _expr = expr;
      _type = type;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Object value = _expr.evaluate(environment);
      switch (_type) {

      case BGP_NEIGHBOR:
         environment.getBgpNeighbors().put(_variable, (BgpNeighbor) value);
         break;

      case BOOLEAN:
         environment.getBooleans().put(_variable, (Boolean) value);
         break;

      case INT:
         environment.getIntegers().put(_variable, (Integer) value);
         break;

      case INTERFACE:
         environment.getInterfaces().put(_variable, (Interface) value);
         break;

      case IP:
         environment.getIps().put(_variable, (Ip) value);
         break;

      case IPSEC_VPN:
         environment.getIpsecVpns().put(_variable, (IpsecVpn) value);
         break;

      case NODE:
         environment.getNodes().put(_variable, (Configuration) value);
         break;

      case POLICY_MAP:
         environment.getPolicyMaps().put(_variable, (PolicyMap) value);
         break;

      case POLICY_MAP_CLAUSE:
         environment.getPolicyMapClauses().put(_variable,
               (PolicyMapClause) value);
         break;

      case PREFIX:
         environment.getPrefixes().put(_variable, (Prefix) value);
         break;

      case PREFIX_SPACE:
         environment.getPrefixSpaces().put(_variable, (PrefixSpace) value);
         break;

      case ROUTE_FILTER:
         environment.getRouteFilters().put(_variable, (RouteFilterList) value);
         break;

      case ROUTE_FILTER_LINE:
         environment.getRouteFilterLines().put(_variable,
               (RouteFilterLine) value);
         break;

      case STATIC_ROUTE:
         environment.getStaticRoutes().put(_variable, (StaticRoute) value);
         break;

      case STRING:
         break;

      case ACTION:
      case RANGE:
      case REGEX:
      case SET_BGP_NEIGHBOR:
      case SET_INT:
      case SET_INTERFACE:
      case SET_IP:
      case SET_IPSEC_VPN:
      case SET_NODE:
      case SET_POLICY_MAP:
      case SET_POLICY_MAP_CLAUSE:
      case SET_PREFIX:
      case SET_PREFIX_SPACE:
      case SET_ROUTE_FILTER:
      case SET_ROUTE_FILTER_LINE:
      case SET_STATIC_ROUTE:
      case SET_STRING:
      default:
         throw new BatfishException("Unsupported variable type: " + _type);

      }

   }

}
