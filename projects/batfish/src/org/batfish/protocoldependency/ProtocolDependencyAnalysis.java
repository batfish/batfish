package org.batfish.protocoldependency;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.representation.Configuration;
import org.batfish.representation.LineAction;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchType;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.util.SubRange;

public class ProtocolDependencyAnalysis {

   private Map<String, Configuration> _configurations;

   private final DependencyMap _dependencyMap;

   @SuppressWarnings("unused")
   private BatfishLogger _logger;

   @SuppressWarnings("unused")
   private Settings _settings;

   public ProtocolDependencyAnalysis(Map<String, Configuration> configurations,
         Settings settings, BatfishLogger logger) {
      _configurations = configurations;
      _settings = settings;
      _logger = logger;
      _dependencyMap = new DependencyMap(_configurations);
   }

   private void initOspfE2Dependencies() {
      for (Configuration c : _configurations.values()) {
         DependentNode dependentNode = _dependencyMap.getDependentNodes().get(
               c.getHostname());
         DependentProtocol ospf = new DependentProtocol(RoutingProtocol.OSPF_E2);
         dependentNode.getProtocols().put(RoutingProtocol.OSPF_E2, ospf);
         Set<Dependent> dependents = ospf.getDependents();
         if (c.getOspfProcess() != null) {
            for (PolicyMap outboundPolicy : c.getOspfProcess()
                  .getOutboundPolicyMaps()) {
               for (PolicyMapClause clause : outboundPolicy.getClauses()) {
                  if (clause.getAction() == PolicyMapAction.PERMIT) {
                     Set<RoutingProtocol> protocols = new HashSet<RoutingProtocol>();
                     boolean foundMatchProtocol = false;
                     boolean foundMatchRouteFilter = false;
                     for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                        if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
                           foundMatchProtocol = true;
                           PolicyMapMatchProtocolLine matchProtocolLine = (PolicyMapMatchProtocolLine) matchLine;
                           protocols.add(matchProtocolLine.getProtocol());
                        }
                        if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
                           foundMatchRouteFilter = true;
                           PolicyMapMatchRouteFilterListLine matchRouteFilterLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                           for (RouteFilterList list : matchRouteFilterLine
                                 .getLists()) {
                              for (RouteFilterLine line : list.getLines()) {
                                 Prefix prefix = line.getPrefix();
                                 SubRange lengthRange = line.getLengthRange();
                                 if (line.getAction() == LineAction.ACCEPT) {
                                    for (RoutingProtocol protocol : protocols) {
                                       dependents.add(new Dependent(protocol,
                                             prefix, lengthRange));
                                    }
                                    if (!foundMatchProtocol) {
                                       RoutingProtocol protocol = null;
                                       dependents.add(new Dependent(protocol,
                                             prefix, lengthRange));
                                    }
                                 }
                              }
                           }
                        }
                     }
                     if (!foundMatchRouteFilter) {
                        Prefix prefix = Prefix.ZERO;
                        SubRange lengthRange = new SubRange(0, 32);
                        for (RoutingProtocol protocol : protocols) {
                           dependents.add(new Dependent(protocol, prefix,
                                 lengthRange));
                        }
                        if (!foundMatchProtocol) {
                           RoutingProtocol protocol = null;
                           dependents.add(new Dependent(protocol, prefix,
                                 lengthRange));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void run() {
      initOspfE2Dependencies();
   }

}
