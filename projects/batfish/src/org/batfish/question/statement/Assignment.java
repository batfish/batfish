package org.batfish.question.statement;

import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.questions.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.question.QMap;

public class Assignment implements Statement {

   private final Expr _expr;

   private final VariableType _type;

   private final String _variable;

   public Assignment(String variable, Expr expr, VariableType type) {
      _variable = variable;
      _expr = expr;
      _type = type;
   }

   @SuppressWarnings("unchecked")
   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      Object value = _expr.evaluate(environment);
      switch (_type) {

      case BGP_ADVERTISEMENT:
         environment.getBgpAdvertisements().put(_variable,
               (BgpAdvertisement) value);
         break;

      case BGP_NEIGHBOR:
         environment.getBgpNeighbors().put(_variable, (BgpNeighbor) value);
         break;

      case BOOLEAN:
         environment.getBooleans().put(_variable, (Boolean) value);
         break;

      case GENERATED_ROUTE:
         environment.getGeneratedRoutes()
               .put(_variable, (GeneratedRoute) value);
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

      case MAP:
         environment.getMaps().put(_variable, (QMap) value);
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

      case PROTOCOL:
         environment.getProtocols().put(_variable, (RoutingProtocol) value);
         break;

      case ROUTE:
         environment.getRoutes().put(_variable, (PrecomputedRoute) value);
         break;

      case ROUTE_FILTER:
         environment.getRouteFilters().put(_variable, (RouteFilterList) value);
         break;

      case ROUTE_FILTER_LINE:
         environment.getRouteFilterLines().put(_variable,
               (RouteFilterLine) value);
         break;

      case SET_PREFIX:
         environment.getPrefixSets().put(_variable, (Set<Prefix>) value);
         break;

      case SET_STRING:
         environment.getStringSets().put(_variable, (Set<String>) value);
         break;

      case STATIC_ROUTE:
         environment.getStaticRoutes().put(_variable, (StaticRoute) value);
         break;

      case STRING:
         environment.getStrings().put(_variable, (String) value);
         break;

      case ACTION:
      case NAMED_STRUCT_TYPE:
      case NEIGHBOR_TYPE:
      case NODE_TYPE:
      case RANGE:
      case REGEX:
      case SET_BGP_ADVERTISEMENT:
      case SET_BGP_NEIGHBOR:
      case SET_INT:
      case SET_INTERFACE:
      case SET_IP:
      case SET_IPSEC_VPN:
      case SET_NODE:
      case SET_NODE_TYPE:
      case SET_POLICY_MAP:
      case SET_POLICY_MAP_CLAUSE:
      case SET_PREFIX_SPACE:
      case SET_ROUTE:
      case SET_ROUTE_FILTER:
      case SET_ROUTE_FILTER_LINE:
      case SET_STATIC_ROUTE:
      case SET_NEIGHBOR_TYPE:
      default:
         throw new BatfishException("Unsupported variable type: " + _type);

      }

   }

}
