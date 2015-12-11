package org.batfish.question.statement;

import java.util.HashSet;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.grammar.question.VariableType;
import org.batfish.main.Settings;
import org.batfish.question.Environment;
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

public class SetDeclarationStatement implements Statement {

   private final VariableType _type;

   private final String _var;

   public SetDeclarationStatement(String var, VariableType type) {
      _var = var;
      _type = type;
   }

   @Override
   public void execute(Environment environment, BatfishLogger logger,
         Settings settings) {
      switch (_type) {

      case SET_BGP_NEIGHBOR:
         initBgpNeighborSet(environment, logger, settings);
         break;

      case SET_INT:
         initIntSet(environment, logger, settings);
         break;

      case SET_INTERFACE:
         initInterfaceSet(environment, logger, settings);
         break;

      case SET_IP:
         initIpSet(environment, logger, settings);
         break;

      case SET_IPSEC_VPN:
         initIpsecVpnSet(environment, logger, settings);
         break;

      case SET_NODE:
         initNodeSet(environment, logger, settings);
         break;

      case SET_POLICY_MAP:
         initPolicyMapSet(environment, logger, settings);
         break;

      case SET_POLICY_MAP_CLAUSE:
         initPolicyMapClauseSet(environment, logger, settings);
         break;

      case SET_PREFIX:
         initPrefixSet(environment, logger, settings);
         break;

      case SET_PREFIX_SPACE:
         initPrefixSpaceSet(environment, logger, settings);
         break;

      case SET_ROUTE_FILTER:
         initRouteFilterSet(environment, logger, settings);
         break;

      case SET_ROUTE_FILTER_LINE:
         initRouteFilterLineSet(environment, logger, settings);
         break;

      case SET_STATIC_ROUTE:
         initStaticRouteSet(environment, logger, settings);
         break;

      case SET_STRING:
         initStringSet(environment, logger, settings);
         break;

      // $CASES-OMITTED$
      default:
         throw new BatfishException("invalid set type: " + _type.toString());
      }
   }

   private void initBgpNeighborSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getBgpNeighborSets().get(_var) == null) {
         environment.getBgpNeighborSets().put(_var, new HashSet<BgpNeighbor>());
      }
   }

   private void initInterfaceSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getInterfaceSets().get(_var) == null) {
         environment.getInterfaceSets().put(_var, new HashSet<Interface>());
      }
   }

   private void initIntSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getIntegerSets().get(_var) == null) {
         environment.getIntegerSets().put(_var, new HashSet<Integer>());
      }
   }

   private void initIpsecVpnSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getIpsecVpnSets().get(_var) == null) {
         environment.getIpsecVpnSets().put(_var, new HashSet<IpsecVpn>());
      }
   }

   private void initIpSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getIpSets().get(_var) == null) {
         environment.getIpSets().put(_var, new HashSet<Ip>());
      }
   }

   private void initNodeSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getNodeSets().get(_var) == null) {
         environment.getNodeSets().put(_var, new HashSet<Configuration>());
      }
   }

   private void initPolicyMapClauseSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getPolicyMapClauseSets().get(_var) == null) {
         environment.getPolicyMapClauseSets().put(_var,
               new HashSet<PolicyMapClause>());
      }
   }

   private void initPolicyMapSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getPolicyMapSets().get(_var) == null) {
         environment.getPolicyMapSets().put(_var, new HashSet<PolicyMap>());
      }
   }

   private void initPrefixSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getPrefixSets().get(_var) == null) {
         environment.getPrefixSets().put(_var, new HashSet<Prefix>());
      }
   }

   private void initPrefixSpaceSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getPrefixSpaceSets().get(_var) == null) {
         environment.getPrefixSpaceSets().put(_var, new HashSet<PrefixSpace>());
      }
   }

   private void initRouteFilterLineSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getRouteFilterLineSets().get(_var) == null) {
         environment.getRouteFilterLineSets().put(_var,
               new HashSet<RouteFilterLine>());
      }
   }

   private void initRouteFilterSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getRouteFilterSets().get(_var) == null) {
         environment.getRouteFilterSets().put(_var,
               new HashSet<RouteFilterList>());
      }
   }

   private void initStaticRouteSet(Environment environment,
         BatfishLogger logger, Settings settings) {
      if (environment.getStaticRouteSets().get(_var) == null) {
         environment.getStaticRouteSets().put(_var, new HashSet<StaticRoute>());
      }
   }

   private void initStringSet(Environment environment, BatfishLogger logger,
         Settings settings) {
      if (environment.getStringSets().get(_var) == null) {
         environment.getStringSets().put(_var, new HashSet<String>());
      }
   }

}
