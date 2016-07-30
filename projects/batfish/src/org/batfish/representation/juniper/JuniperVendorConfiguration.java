package org.batfish.representation.juniper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.batfish.common.VendorConversionException;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IkeProposal;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.IsisInterfaceMode;
import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.IsisProcess;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchIpAccessListLine;
import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.PolicyMapSetNextHopLine;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.juniper.BgpGroup.BgpGroupType;

public final class JuniperVendorConfiguration extends JuniperConfiguration
      implements VendorConfiguration {

   private static final int DEFAULT_AGGREGATE_ROUTE_PREFERENCE = 130;

   private static final String FIRST_LOOPBACK_INTERFACE_NAME = "lo0";

   private static final long serialVersionUID = 1L;

   private static String communityRegexToJavaRegex(String regex) {
      String out = regex;
      out = out.replace(":*", ":.*");
      out = out.replaceFirst("^\\*", ".*");
      return out;
   }

   private Configuration _c;

   private transient Interface _lo0;

   private transient boolean _lo0Initialized;

   private final RoleSet _roles;

   private transient Set<String> _unimplementedFeatures;

   private ConfigurationFormat _vendor;

   private transient Warnings _w;

   public JuniperVendorConfiguration(Set<String> unimplementedFeatures) {
      _roles = new RoleSet();
      _unimplementedFeatures = unimplementedFeatures;
   }

   private BgpProcess createBgpProcess() {
      BgpProcess proc = new BgpProcess();
      BgpGroup mg = _defaultRoutingInstance.getMasterBgpGroup();
      if (mg.getLocalAs() == null) {
         Integer defaultRoutingInstanceAs = _defaultRoutingInstance.getAs();
         if (defaultRoutingInstanceAs == null) {
            _w.redFlag("BGP BROKEN FOR THIS ROUTER: Cannot determine local autonomous system");
         }
         else {
            mg.setLocalAs(_defaultRoutingInstance.getAs());
         }
      }
      for (Entry<Ip, IpBgpGroup> e : _defaultRoutingInstance.getIpBgpGroups()
            .entrySet()) {
         Ip ip = e.getKey();
         IpBgpGroup ig = e.getValue();
         ig.cascadeInheritance();
         BgpNeighbor neighbor = new BgpNeighbor(ip, _c);
         Boolean ebgpMultihop = ig.getEbgpMultihop();
         if (ebgpMultihop == null) {
            ebgpMultihop = false;
         }
         neighbor.setEbgpMultihop(ebgpMultihop);
         Integer loops = ig.getLoops();
         boolean allowLocalAsIn = loops != null && loops > 0;
         neighbor.setAllowLocalAsIn(allowLocalAsIn);
         Boolean advertisePeerAs = ig.getAdvertisePeerAs();
         if (advertisePeerAs == null) {
            advertisePeerAs = false;
         }
         neighbor.setAllowRemoteAsOut(advertisePeerAs);
         Boolean advertiseInactive = ig.getAdvertiseInactive();
         if (advertiseInactive == null) {
            advertiseInactive = false;
         }
         neighbor.setAdvertiseInactive(advertiseInactive);
         neighbor.setGroup(ig.getGroupName());
         // import policies
         for (String importPolicyName : ig.getImportPolicies()) {
            PolicyMap importPolicy = _c.getPolicyMaps().get(importPolicyName);
            if (importPolicy == null) {
               _w.redFlag("missing bgp import policy: '" + importPolicyName
                     + "'\n");
            }
            else {
               setPolicyStatementReferent(importPolicyName,
                     ig.getImportPolicies(), "BGP import policy for neighbor: "
                           + ig.getRemoteAddress().toString());
               neighbor.getInboundPolicyMaps().add(importPolicy);
            }
         }
         // export policies
         for (String exportPolicyName : ig.getExportPolicies()) {
            PolicyMap exportPolicy = _c.getPolicyMaps().get(exportPolicyName);
            if (exportPolicy == null) {
               _w.redFlag("missing bgp export policy: '" + exportPolicyName
                     + "'");
            }
            else {
               setPolicyStatementReferent(exportPolicyName,
                     ig.getExportPolicies(), "BGP export policy for neighbor: "
                           + ig.getRemoteAddress().toString());
               neighbor.getOutboundPolicyMaps().add(exportPolicy);
            }
         }
         // inherit local-as
         neighbor.setLocalAs(ig.getLocalAs());

         // inherit peer-as, or use local-as if internal
         if (ig.getType() == BgpGroupType.INTERNAL) {
            neighbor.setRemoteAs(ig.getLocalAs());
         }
         else {
            neighbor.setRemoteAs(ig.getPeerAs());
         }

         // TODO: implement better behavior than setting default metric to 0
         neighbor.setDefaultMetric(0);

         // TODO: find out if there is a juniper equivalent of cisco
         // send-community
         neighbor.setSendCommunity(true);

         // inherit update-source
         Ip localAddress = ig.getLocalAddress();
         if (localAddress == null) {
            // assign the ip of the interface that is likely connected to this
            // peer
            outerloop: for (org.batfish.datamodel.Interface iface : _c
                  .getInterfaces().values()) {
               for (Prefix prefix : iface.getAllPrefixes()) {
                  if (prefix.contains(ip)) {
                     localAddress = prefix.getAddress();
                     break outerloop;
                  }
               }
            }
         }
         if (localAddress == null && _defaultAddressSelection) {
            initFirstLoopbackInterface();
            if (_lo0 != null) {
               Prefix lo0_0Prefix = _lo0.getPrimaryPrefix();
               if (lo0_0Prefix != null) {
                  localAddress = lo0_0Prefix.getAddress();
               }
            }
         }
         if (localAddress == null) {
            _w.redFlag("Could not determine local ip for bgp peering with neighbor ip: "
                  + ip);
         }
         else {
            neighbor.setLocalIp(localAddress);
         }
         proc.getNeighbors().put(neighbor.getPrefix(), neighbor);
      }
      return proc;
   }

   private IsisProcess createIsisProcess(IsoAddress netAddress) {
      IsisProcess newProc = new IsisProcess();
      newProc.setNetAddress(netAddress);
      IsisSettings settings = _defaultRoutingInstance.getIsisSettings();
      for (String policyName : settings.getExportPolicies()) {
         PolicyMap policy = _c.getPolicyMaps().get(policyName);
         if (policy == null) {
            _w.redFlag("Reference to undefined to is-is export policy: '"
                  + policyName + "'", UNDEFINED);
         }
         else {
            setPolicyStatementReferent(policyName,
                  settings.getExportPolicies(), "IS-IS export policies");
            newProc.getOutboundPolicyMaps().add(policy);
         }
      }
      boolean l1 = settings.getLevel1Settings().getEnabled();
      boolean l2 = settings.getLevel2Settings().getEnabled();
      if (l1 && l2) {
         newProc.setLevel(IsisLevel.LEVEL_1_2);
      }
      else if (l1) {
         newProc.setLevel(IsisLevel.LEVEL_1);
      }
      else if (l2) {
         newProc.setLevel(IsisLevel.LEVEL_2);
      }
      else {
         return null;
      }
      return newProc;
   }

   private OspfProcess createOspfProcess() {
      OspfProcess newProc = new OspfProcess();
      // export policies
      for (String exportPolicyName : _defaultRoutingInstance
            .getOspfExportPolicies()) {
         PolicyMap exportPolicy = _c.getPolicyMaps().get(exportPolicyName);
         if (exportPolicy == null) {
            _w.redFlag("Reference to undefined to OSPF export policy: '"
                  + exportPolicyName + "'", UNDEFINED);
         }
         else {
            setPolicyStatementReferent(exportPolicyName,
                  _defaultRoutingInstance.getOspfExportPolicies(),
                  "OSPF export policies");
            newProc.getOutboundPolicyMaps().add(exportPolicy);
            // TODO: support type E1
            newProc.getPolicyMetricTypes().put(exportPolicy.getName(),
                  OspfMetricType.E2);
         }
      }
      // areas
      Map<Long, org.batfish.datamodel.OspfArea> newAreas = newProc.getAreas();
      for (Entry<Ip, OspfArea> e : _defaultRoutingInstance.getOspfAreas()
            .entrySet()) {
         Ip areaIp = e.getKey();
         long areaLong = areaIp.asLong();
         // OspfArea area = e.getValue();
         org.batfish.datamodel.OspfArea newArea = new org.batfish.datamodel.OspfArea(
               areaLong);
         newAreas.put(areaLong, newArea);
      }
      // place interfaces into areas
      for (Entry<String, Interface> e : _defaultRoutingInstance.getInterfaces()
            .entrySet()) {
         String name = e.getKey();
         Interface iface = e.getValue();
         placeInterfaceIntoArea(newAreas, name, iface);
         for (Entry<String, Interface> eUnit : iface.getUnits().entrySet()) {
            String unitName = eUnit.getKey();
            Interface unitIface = eUnit.getValue();
            placeInterfaceIntoArea(newAreas, unitName, unitIface);
         }
      }
      newProc.setRouterId(_defaultRoutingInstance.getRouterId());
      newProc.setReferenceBandwidth(_defaultRoutingInstance
            .getOspfReferenceBandwidth());
      return newProc;
   }

   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public Warnings getWarnings() {
      return _w;
   }

   private void initFirstLoopbackInterface() {
      if (_lo0Initialized) {
         return;
      }
      _lo0Initialized = true;
      _lo0 = _defaultRoutingInstance.getInterfaces().get(
            FIRST_LOOPBACK_INTERFACE_NAME);
      Pattern p = Pattern
            .compile("[A-Za-z0-9][A-Za-z0-9]*:lo[0-9][0-9]*\\.[0-9][0-9]*");
      if (_lo0 == null) {
         for (NodeDevice nd : _defaultRoutingInstance.getNodeDevices().values()) {
            for (Interface iface : nd.getInterfaces().values()) {
               for (Interface unit : iface.getUnits().values()) {
                  if (p.matcher(unit.getName()).matches()) {
                     _lo0 = unit;
                  }
               }
            }
         }
      }

   }

   private void placeInterfaceIntoArea(
         Map<Long, org.batfish.datamodel.OspfArea> newAreas, String name,
         Interface iface) {
      org.batfish.datamodel.Interface newIface = _c.getInterfaces().get(name);
      Ip ospfArea = iface.getOspfActiveArea();
      boolean setCost = false;
      if (ospfArea != null) {
         setCost = true;
         long ospfAreaLong = ospfArea.asLong();
         org.batfish.datamodel.OspfArea newArea = newAreas.get(ospfAreaLong);
         newArea.getInterfaces().add(newIface);
         newIface.setOspfEnabled(true);
      }
      for (Ip passiveArea : iface.getOspfPassiveAreas()) {
         setCost = true;
         long ospfAreaLong = passiveArea.asLong();
         org.batfish.datamodel.OspfArea newArea = newAreas.get(ospfAreaLong);
         newArea.getInterfaces().add(newIface);
         newIface.setOspfEnabled(true);
         newIface.setOspfPassive(true);
      }
      if (setCost) {
         Integer ospfCost = iface.getOspfCost();
         if (ospfCost == null
               && newIface.isLoopback(ConfigurationFormat.FLAT_JUNIPER)) {
            ospfCost = 0;
         }
         newIface.setOspfCost(ospfCost);
      }
   }

   private void setPolicyStatementReferent(String policyName, Object referer,
         String description) {
      PolicyStatement policy = _policyStatements.get(policyName);
      if (policy == null) {
         return;
      }
      policy.getReferers().put(referer, description);
      List<PsTerm> terms = new ArrayList<PsTerm>();
      terms.add(policy.getSingletonTerm());
      terms.addAll(policy.getTerms().values());
      for (PsTerm term : terms) {
         for (PsFrom from : term.getFroms()) {
            if (from instanceof PsFromPolicyStatement) {
               PsFromPolicyStatement fromPolicyStatement = (PsFromPolicyStatement) from;
               String subPolicyName = fromPolicyStatement.getPolicyStatement();
               setPolicyStatementReferent(subPolicyName, referer, description);
            }
            else if (from instanceof PsFromPolicyStatementConjunction) {
               PsFromPolicyStatementConjunction fromPolicyStatementConjunction = (PsFromPolicyStatementConjunction) from;
               for (String subPolicyName : fromPolicyStatementConjunction
                     .getConjuncts()) {
                  setPolicyStatementReferent(subPolicyName, referer,
                        description);
               }
            }
         }
      }
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _vendor = format;
   }

   private org.batfish.datamodel.GeneratedRoute toAggregateRoute(
         AggregateRoute route) {
      Prefix prefix = route.getPrefix();
      int prefixLength = prefix.getPrefixLength();
      int administrativeCost = route.getMetric();
      String policyNameSuffix = route.getPrefix().toString().replace('/', '_')
            .replace('.', '_');
      String policyName = "~AGGREGATE_" + policyNameSuffix + "~";
      PolicyMap policy = new PolicyMap(policyName);
      PolicyMapClause clause = new PolicyMapClause();
      policy.getClauses().add(clause);
      clause.setAction(PolicyMapAction.PERMIT);
      String rflName = "~AGGREGATE_" + policyNameSuffix + "_RF~";
      RouteFilterList rfList = new RouteFilterList(rflName);
      rfList.addLine(new org.batfish.datamodel.RouteFilterLine(
            LineAction.ACCEPT, prefix, new SubRange(prefixLength + 1, 32)));
      PolicyMapMatchLine matchLine = new PolicyMapMatchRouteFilterListLine(
            Collections.singleton(rfList));
      clause.getMatchLines().add(matchLine);
      Set<PolicyMap> policies = Collections.singleton(policy);
      org.batfish.datamodel.GeneratedRoute newRoute = new org.batfish.datamodel.GeneratedRoute(
            prefix, administrativeCost, policies);
      newRoute.setDiscard(true);
      _c.getPolicyMaps().put(policyName, policy);
      _c.getRouteFilterLists().put(rflName, rfList);
      return newRoute;
   }

   private org.batfish.datamodel.CommunityList toCommunityList(CommunityList cl) {
      String name = cl.getName();
      List<org.batfish.datamodel.CommunityListLine> newLines = new ArrayList<org.batfish.datamodel.CommunityListLine>();
      for (CommunityListLine line : cl.getLines()) {
         String regex = line.getRegex();
         String javaRegex = communityRegexToJavaRegex(regex);
         org.batfish.datamodel.CommunityListLine newLine = new org.batfish.datamodel.CommunityListLine(
               LineAction.ACCEPT, javaRegex);
         newLines.add(newLine);
      }
      org.batfish.datamodel.CommunityList newCl = new org.batfish.datamodel.CommunityList(
            name, newLines);
      return newCl;
   }

   private org.batfish.datamodel.GeneratedRoute toGeneratedRoute(
         GeneratedRoute route) {
      Prefix prefix = route.getPrefix();
      Integer administrativeCost = route.getPreference();
      if (administrativeCost == null) {
         administrativeCost = DEFAULT_AGGREGATE_ROUTE_PREFERENCE;
      }
      Integer metric = route.getMetric();
      Set<PolicyMap> policies = new LinkedHashSet<PolicyMap>();
      for (String policyName : route.getPolicies()) {
         PolicyMap policy = _c.getPolicyMaps().get(policyName);
         if (policy == null) {
            _w.redFlag("missing generated route policy: '" + policyName + "'");
         }
         else {
            setPolicyStatementReferent(policyName, route.getPolicies(),
                  "Generated route policy for prefix: "
                        + route.getPrefix().toString());
            policies.add(policy);
         }
      }
      org.batfish.datamodel.GeneratedRoute newRoute = new org.batfish.datamodel.GeneratedRoute(
            prefix, administrativeCost, policies);
      newRoute.setMetric(metric);
      return newRoute;
   }

   private org.batfish.datamodel.IkeGateway toIkeGateway(
         IkeGateway oldIkeGateway) {
      String name = oldIkeGateway.getName();
      org.batfish.datamodel.IkeGateway newIkeGateway = new org.batfish.datamodel.IkeGateway(
            name);

      // address
      newIkeGateway.setAddress(oldIkeGateway.getAddress());
      newIkeGateway.setLocalAddress(oldIkeGateway.getLocalAddress());

      // external interface
      Interface oldExternalInterface = oldIkeGateway.getExternalInterface();
      if (oldExternalInterface != null) {
         String externalInterfaceName = oldExternalInterface.getName();
         org.batfish.datamodel.Interface newExternalInterface = _c
               .getInterfaces().get(externalInterfaceName);
         if (newExternalInterface == null) {
            _w.redFlag("Reference to undefined interface: '"
                  + externalInterfaceName + "' in ike gateway: '" + name + "'",
                  UNDEFINED);
         }
         else {
            newIkeGateway.setExternalInterface(newExternalInterface);
         }
      }
      else {
         _w.redFlag("No external interface set for ike gateway: '" + name + "'");
      }

      // ike policy
      String ikePolicyName = oldIkeGateway.getIkePolicy();
      org.batfish.datamodel.IkePolicy newIkePolicy = _c.getIkePolicies().get(
            ikePolicyName);
      if (newIkePolicy == null) {
         _w.redFlag("Reference to undefined ike policy: '" + ikePolicyName
               + "' in ike gateway: '" + name + "'", UNDEFINED);
      }
      else {
         _ikePolicies
               .get(ikePolicyName)
               .getReferers()
               .put(oldIkeGateway,
                     "IKE policy for IKE gateway: " + oldIkeGateway);
         newIkeGateway.setIkePolicy(newIkePolicy);
      }

      return newIkeGateway;
   }

   private org.batfish.datamodel.IkePolicy toIkePolicy(IkePolicy oldIkePolicy) {
      String name = oldIkePolicy.getName();
      org.batfish.datamodel.IkePolicy newIkePolicy = new org.batfish.datamodel.IkePolicy(
            name);

      // pre-shared-key
      newIkePolicy.setPreSharedKeyHash(oldIkePolicy.getPreSharedKeyHash());

      // ike proposals
      for (String ikeProposalName : oldIkePolicy.getProposals()) {
         IkeProposal ikeProposal = _c.getIkeProposals().get(ikeProposalName);
         if (ikeProposal == null) {
            _w.redFlag("Reference to undefined ike proposal: '"
                  + ikeProposalName + "' in ike policy: '" + name + "'",
                  UNDEFINED);
         }
         else {
            _ikeProposals
                  .get(ikeProposalName)
                  .getReferers()
                  .put(oldIkePolicy,
                        "IKE proposal for IKE policy: " + oldIkePolicy);
            newIkePolicy.getProposals().put(ikeProposalName, ikeProposal);
         }
      }

      return newIkePolicy;
   }

   private org.batfish.datamodel.Interface toInterface(Interface iface) {
      String name = iface.getName();
      org.batfish.datamodel.Interface newIface = new org.batfish.datamodel.Interface(
            name, _c);
      Zone zone = _interfaceZones.get(iface);
      if (zone != null) {
         String zoneName = zone.getName();
         if (zone != null) {
            // filter for interface in zone
            FirewallFilter zoneInboundInterfaceFilter = zone
                  .getInboundInterfaceFilters().get(iface);
            if (zoneInboundInterfaceFilter != null) {
               String zoneInboundInterfaceFilterName = zoneInboundInterfaceFilter
                     .getName();
               zoneInboundInterfaceFilter
                     .getReferers()
                     .put(iface,
                           "Interface: '"
                                 + iface.getName()
                                 + "' refers to inbound filter for interface in zone : '"
                                 + zoneName + "'");
               IpAccessList zoneInboundInterfaceFilterList = _c
                     .getIpAccessLists().get(zoneInboundInterfaceFilterName);
               newIface.setInboundFilter(zoneInboundInterfaceFilterList);
            }
            else {
               // filter for zone
               FirewallFilter zoneInboundFilter = zone.getInboundFilter();
               String zoneInboundFilterName = zoneInboundFilter.getName();
               zoneInboundFilter.getReferers().put(
                     iface,
                     "Interface: '" + iface.getName()
                           + "' refers to inbound filter for zone : '"
                           + zoneName + "'");
               IpAccessList zoneInboundFilterList = _c.getIpAccessLists().get(
                     zoneInboundFilterName);
               newIface.setInboundFilter(zoneInboundFilterList);
            }
         }
      }
      String inAclName = iface.getIncomingFilter();
      if (inAclName != null) {
         IpAccessList inAcl = _c.getIpAccessLists().get(inAclName);
         if (inAcl == null) {
            _w.redFlag("missing incoming acl: '" + inAclName + "'");
         }
         else {
            FirewallFilter inFilter = _filters.get(inAclName);
            inFilter.getReferers().put(iface,
                  "Incoming ACL for interface: " + iface.getName());
            newIface.setIncomingFilter(inAcl);
            if (inFilter.getRoutingPolicy()) {
               PolicyMap routingPolicy = _c.getPolicyMaps().get(inAclName);
               newIface.setRoutingPolicy(routingPolicy);
            }
         }
      }
      String outAclName = iface.getOutgoingFilter();
      if (outAclName != null) {
         IpAccessList outAcl = _c.getIpAccessLists().get(outAclName);
         if (outAcl == null) {
            _w.redFlag("missing outgoing acl: '" + outAclName + "'");
         }
         else {
            _filters.get(outAclName).getReferers()
                  .put(iface, "Outgoing ACL for interface: " + iface.getName());
            newIface.setOutgoingFilter(outAcl);
         }
      }

      // Prefix primaryPrefix = iface.getPrimaryPrefix();
      // Set<Prefix> allPrefixes = iface.getAllPrefixes();
      // if (primaryPrefix != null) {
      // newIface.setPrefix(primaryPrefix);
      // }
      // else {
      // if (!allPrefixes.isEmpty()) {
      // Prefix firstOfAllPrefixes = allPrefixes.toArray(new Prefix[] {})[0];
      // newIface.setPrefix(firstOfAllPrefixes);
      // }
      // }
      // newIface.getAllPrefixes().addAll(allPrefixes);

      if (iface.getPrimaryPrefix() != null) {
         newIface.setPrefix(iface.getPrimaryPrefix());
      }
      newIface.getAllPrefixes().addAll(iface.getAllPrefixes());
      newIface.setActive(iface.getActive());
      newIface.setAccessVlan(iface.getAccessVlan());
      newIface.setNativeVlan(iface.getNativeVlan());
      newIface.setSwitchportMode(iface.getSwitchportMode());
      newIface.setSwitchportTrunkEncapsulation(iface
            .getSwitchportTrunkEncapsulation());
      newIface.setBandwidth(iface.getBandwidth());
      // isis settings
      IsisInterfaceSettings isisSettings = iface.getIsisSettings();
      IsisInterfaceLevelSettings isisL1Settings = isisSettings
            .getLevel1Settings();
      newIface.setIsisL1InterfaceMode(IsisInterfaceMode.UNSET);
      if (isisL1Settings.getEnabled()) {
         if (isisSettings.getPassive()) {
            newIface.setIsisL1InterfaceMode(IsisInterfaceMode.PASSIVE);
         }
         else if (isisSettings.getEnabled()) {
            newIface.setIsisL1InterfaceMode(IsisInterfaceMode.ACTIVE);
         }
      }
      IsisInterfaceLevelSettings isisL2Settings = isisSettings
            .getLevel2Settings();
      newIface.setIsisL2InterfaceMode(IsisInterfaceMode.UNSET);
      if (isisL2Settings.getEnabled()) {
         if (isisSettings.getPassive()) {
            newIface.setIsisL2InterfaceMode(IsisInterfaceMode.PASSIVE);
         }
         else if (isisSettings.getEnabled()) {
            newIface.setIsisL2InterfaceMode(IsisInterfaceMode.ACTIVE);
         }
      }
      Integer l1Metric = isisSettings.getLevel1Settings().getMetric();
      Integer l2Metric = isisSettings.getLevel2Settings().getMetric();
      if (l1Metric != l2Metric && l1Metric != null && l2Metric != null) {
         _w.unimplemented("distinct metrics for is-is level1 and level2 on an interface");
      }
      else if (l1Metric != null) {
         newIface.setIsisCost(l1Metric);
      }
      else if (l2Metric != null) {
         newIface.setIsisCost(l2Metric);
      }
      // TODO: enable/disable individual levels
      return newIface;
   }

   private IpAccessList toIpAccessList(FirewallFilter filter)
         throws VendorConversionException {
      String name = filter.getName();
      List<IpAccessListLine> lines = new ArrayList<IpAccessListLine>();
      for (FwTerm term : filter.getTerms().values()) {
         // action
         LineAction action;
         if (term.getThens().contains(FwThenAccept.INSTANCE)) {
            action = LineAction.ACCEPT;
         }
         else if (term.getThens().contains(FwThenDiscard.INSTANCE)) {
            action = LineAction.REJECT;
         }
         else if (term.getThens().contains(FwThenNextTerm.INSTANCE)) {
            // TODO: throw error if any transformation is being done
            continue;
         }
         else if (term.getThens().contains(FwThenNop.INSTANCE)) {
            // we assume for now that any 'nop' operations imply acceptance
            action = LineAction.ACCEPT;
         }
         else {
            _w.redFlag("missing action in firewall filter: '" + name
                  + "', term: '" + term.getName() + "'");
            action = LineAction.REJECT;
         }
         IpAccessListLine line = new IpAccessListLine();
         line.setName(term.getName());
         line.setAction(action);
         for (FwFrom from : term.getFroms()) {
            from.applyTo(line, this, _w, _c);
         }
         boolean addLine = term.getFromApplications().isEmpty()
               && term.getFromHostProtocols().isEmpty()
               && term.getFromHostServices().isEmpty();
         for (FwFromHostProtocol from : term.getFromHostProtocols()) {
            from.applyTo(lines, _w);
         }
         for (FwFromHostService from : term.getFromHostServices()) {
            from.applyTo(lines, _w);
         }
         for (FwFromApplication fromApplication : term.getFromApplications()) {
            fromApplication.applyTo(line, lines, _w);
         }
         if (addLine) {
            lines.add(line);
         }
      }
      IpAccessList list = new IpAccessList(name, lines);
      return list;
   }

   private org.batfish.datamodel.IpsecPolicy toIpsecPolicy(
         IpsecPolicy oldIpsecPolicy) {
      String name = oldIpsecPolicy.getName();
      org.batfish.datamodel.IpsecPolicy newIpsecPolicy = new org.batfish.datamodel.IpsecPolicy(
            name);

      // ipsec proposals
      for (String ipsecProposalName : oldIpsecPolicy.getProposals()) {
         IpsecProposal ipsecProposal = _c.getIpsecProposals().get(
               ipsecProposalName);
         if (ipsecProposal == null) {
            _w.redFlag("Reference to undefined ipsec proposal: '"
                  + ipsecProposalName + "' in ipsec policy: '" + name + "'",
                  UNDEFINED);
         }
         else {
            _ipsecProposals
                  .get(ipsecProposalName)
                  .getReferers()
                  .put(oldIpsecPolicy,
                        "IPSEC proposal for IPSEC policy: " + oldIpsecPolicy);
            newIpsecPolicy.getProposals().put(ipsecProposalName, ipsecProposal);
         }
      }

      // perfect-forward-secrecy diffie-hellman key group
      newIpsecPolicy.setPfsKeyGroup(oldIpsecPolicy.getPfsKeyGroup());

      return newIpsecPolicy;
   }

   private org.batfish.datamodel.IpsecVpn toIpsecVpn(IpsecVpn oldIpsecVpn) {
      String name = oldIpsecVpn.getName();
      org.batfish.datamodel.IpsecVpn newIpsecVpn = new org.batfish.datamodel.IpsecVpn(
            name, _c);

      // bind interface
      Interface oldBindInterface = oldIpsecVpn.getBindInterface();
      if (oldBindInterface != null) {
         String bindInterfaceName = oldBindInterface.getName();
         org.batfish.datamodel.Interface newBindInterface = _c.getInterfaces()
               .get(bindInterfaceName);
         if (newBindInterface == null) {
            _w.redFlag("Reference to undefined interface: '"
                  + bindInterfaceName + "' in ipsec vpn: '" + name + "'",
                  UNDEFINED);
         }
         else {
            oldBindInterface.getReferers().put(oldIpsecVpn,
                  "Bind interface for IPSEC VPN: " + name);
            newIpsecVpn.setBindInterface(newBindInterface);
         }
      }
      else {
         _w.redFlag("No bind interface set for ipsec vpn: '" + name + "'");
      }

      // ike gateway
      String ikeGatewayName = oldIpsecVpn.getGateway();
      org.batfish.datamodel.IkeGateway ikeGateway = _c.getIkeGateways().get(
            ikeGatewayName);
      if (ikeGateway == null) {
         _w.redFlag("Reference to undefined ike gateway: '" + ikeGatewayName
               + "' in ipsec vpn: '" + name + "'", UNDEFINED);
      }
      else {
         _ikeGateways.get(ikeGatewayName).getReferers()
               .put(oldIpsecVpn, "IKE gateway for IPSEC VPN: " + name);
         newIpsecVpn.setGateway(ikeGateway);
      }

      // ipsec policy
      String ipsecPolicyName = oldIpsecVpn.getIpsecPolicy();
      org.batfish.datamodel.IpsecPolicy ipsecPolicy = _c.getIpsecPolicies()
            .get(ipsecPolicyName);
      if (ipsecPolicy == null) {
         _w.redFlag("Reference to undefined ipsec policy: '" + ipsecPolicyName
               + "' in ipsec vpn: '" + name + "'", UNDEFINED);
      }
      else {
         _ipsecPolicies.get(ipsecPolicyName).getReferers()
               .put(oldIpsecVpn, "IPSEC policy for IPSEC VPN: " + name);
         newIpsecVpn.setIpsecPolicy(ipsecPolicy);
      }

      return newIpsecVpn;
   }

   private PolicyMap toPolicyMap(PolicyStatement ps) {
      String name = ps.getName();
      PolicyMap map = new PolicyMap(name);
      boolean singleton = ps.getSingletonTerm().getFroms().size() > 0
            || ps.getSingletonTerm().getThens().size() > 0;
      Collection<PsTerm> terms = singleton ? Collections.singleton(ps
            .getSingletonTerm()) : ps.getTerms().values();
      for (PsTerm term : terms) {
         PolicyMapClause clause = new PolicyMapClause();
         clause.setName(term.getName());
         for (PsFrom from : term.getFroms()) {
            if (from instanceof PsFromRouteFilter) {
               int actionLineCounter = 0;
               PsFromRouteFilter fromRouteFilter = (PsFromRouteFilter) from;
               String routeFilterName = fromRouteFilter.getRouteFilterName();
               RouteFilter rf = _routeFilters.get(routeFilterName);
               for (RouteFilterLine line : rf.getLines()) {
                  if (line.getThens().size() > 0) {
                     String lineListName = name + "_ACTION_LINE_"
                           + actionLineCounter;
                     RouteFilterList lineSpecificList = new RouteFilterList(
                           lineListName);
                     line.applyTo(lineSpecificList);
                     actionLineCounter++;
                     _c.getRouteFilterLists().put(lineListName,
                           lineSpecificList);
                     PolicyMapClause lineSpecificClause = new PolicyMapClause();
                     String lineSpecificClauseName = routeFilterName
                           + "_ACTION_LINE_" + actionLineCounter;
                     lineSpecificClause.setName(lineSpecificClauseName);
                     PolicyMapMatchRouteFilterListLine matchRflLine = new PolicyMapMatchRouteFilterListLine(
                           Collections.singleton(lineSpecificList));
                     lineSpecificClause.getMatchLines().add(matchRflLine);
                     for (PsThen then : line.getThens()) {
                        then.applyTo(lineSpecificClause, _c, _w);
                     }
                     map.getClauses().add(lineSpecificClause);
                  }
               }
            }
            from.applyTo(clause, ps, this, _c, _w);
         }
         for (PsThen then : term.getThens()) {
            then.applyTo(clause, _c, _w);
         }
         map.getClauses().add(clause);
      }
      return map;
   }

   private PolicyMap toRoutingPolicy(FirewallFilter filter) {
      String name = filter.getName();
      PolicyMap routingPolicy = new PolicyMap(name);
      for (Entry<String, FwTerm> e : filter.getTerms().entrySet()) {
         String termName = e.getKey();
         FwTerm term = e.getValue();
         PolicyMapClause clause = new PolicyMapClause();
         clause.setName(termName);
         routingPolicy.getClauses().add(clause);
         Set<Prefix> destinationPrefixes = new TreeSet<Prefix>();
         ;
         List<SubRange> destinationPortRanges = new ArrayList<SubRange>();
         Set<Prefix> sourcePrefixes = new TreeSet<Prefix>();
         List<SubRange> sourcePortRanges = new ArrayList<SubRange>();

         for (FwFrom from : term.getFroms()) {
            if (from instanceof FwFromDestinationAddress) {
               FwFromDestinationAddress fromDestinationAddress = (FwFromDestinationAddress) from;
               Prefix destinationPrefix = fromDestinationAddress.getPrefix();
               destinationPrefixes.add(destinationPrefix);
            }
            if (from instanceof FwFromDestinationPort) {
               FwFromDestinationPort fromDestinationPort = (FwFromDestinationPort) from;
               SubRange destinationPortRange = fromDestinationPort
                     .getPortRange();
               destinationPortRanges.add(destinationPortRange);
            }
            else if (from instanceof FwFromSourceAddress) {
               FwFromSourceAddress fromSourceAddress = (FwFromSourceAddress) from;
               Prefix sourcePrefix = fromSourceAddress.getPrefix();
               sourcePrefixes.add(sourcePrefix);
            }
            if (from instanceof FwFromSourcePort) {
               FwFromSourcePort fromSourcePort = (FwFromSourcePort) from;
               SubRange sourcePortRange = fromSourcePort.getPortRange();
               sourcePortRanges.add(sourcePortRange);
            }
         }
         if (!destinationPrefixes.isEmpty() || !destinationPortRanges.isEmpty()
               || !sourcePrefixes.isEmpty() || !sourcePortRanges.isEmpty()) {
            String termIpAccessListName = "~" + name + ":" + termName + "~";
            IpAccessListLine line = new IpAccessListLine();
            for (Prefix dstPrefix : destinationPrefixes) {
               IpWildcard dstWildcard = new IpWildcard(dstPrefix);
               line.getDstIps().add(dstWildcard);
            }
            line.getDstPorts().addAll(destinationPortRanges);
            for (Prefix srcPrefix : sourcePrefixes) {
               IpWildcard srcWildcard = new IpWildcard(srcPrefix);
               line.getSrcIps().add(srcWildcard);
            }
            line.getDstPorts().addAll(sourcePortRanges);
            line.setAction(LineAction.ACCEPT);
            IpAccessList termIpAccessList = new IpAccessList(
                  termIpAccessListName, Collections.singletonList(line));
            _c.getIpAccessLists().put(termIpAccessListName, termIpAccessList);
            PolicyMapMatchIpAccessListLine matchListLine = new PolicyMapMatchIpAccessListLine(
                  Collections.singleton(termIpAccessList));
            clause.getMatchLines().add(matchListLine);
         }
         List<Prefix> nextPrefixes = new ArrayList<Prefix>();
         for (FwThen then : term.getThens()) {
            if (then instanceof FwThenNextIp) {
               FwThenNextIp thenNextIp = (FwThenNextIp) then;
               Prefix nextIp = thenNextIp.getNextPrefix();
               nextPrefixes.add(nextIp);
            }
            else if (then == FwThenDiscard.INSTANCE) {
               clause.setAction(PolicyMapAction.DENY);
            }
            else if (then == FwThenAccept.INSTANCE) {
               clause.setAction(PolicyMapAction.PERMIT);
            }
         }
         if (!nextPrefixes.isEmpty()) {
            List<Ip> nextHopIps = new ArrayList<Ip>();
            for (Prefix nextPrefix : nextPrefixes) {
               nextHopIps.add(nextPrefix.getAddress());
               int prefixLength = nextPrefix.getPrefixLength();
               if (prefixLength != 32) {
                  _w.redFlag("Not sure how to interpret nextIp with prefix-length not equal to 32: "
                        + prefixLength);
               }
            }
            PolicyMapSetNextHopLine setNextHop = new PolicyMapSetNextHopLine(
                  nextHopIps);
            clause.getSetLines().add(setNextHop);
         }
      }
      return routingPolicy;
   }

   private RoutingPolicy toRoutingPolicy(PolicyStatement ps) {
      String name = ps.getName();
      RoutingPolicy routingPolicy = new RoutingPolicy(name);
      List<Statement> statements = routingPolicy.getStatements();
      boolean singleton = ps.getSingletonTerm().getFroms().size() > 0
            || ps.getSingletonTerm().getThens().size() > 0;
      Collection<PsTerm> terms = singleton ? Collections.singleton(ps
            .getSingletonTerm()) : ps.getTerms().values();
      for (PsTerm term : terms) {
         if (!term.getFroms().isEmpty()) {
            If ifStatement = new If();
            ifStatement.setComment(term.getName());
            Conjunction conj = new Conjunction();
            for (PsFrom from : term.getFroms()) {
               if (from instanceof PsFromRouteFilter) {
                  int actionLineCounter = 0;
                  PsFromRouteFilter fromRouteFilter = (PsFromRouteFilter) from;
                  String routeFilterName = fromRouteFilter.getRouteFilterName();
                  RouteFilter rf = _routeFilters.get(routeFilterName);
                  for (RouteFilterLine line : rf.getLines()) {
                     if (line.getThens().size() > 0) {
                        String lineListName = name + "_ACTION_LINE_"
                              + actionLineCounter;
                        RouteFilterList lineSpecificList = new RouteFilterList(
                              lineListName);
                        line.applyTo(lineSpecificList);
                        actionLineCounter++;
                        _c.getRouteFilterLists().put(lineListName,
                              lineSpecificList);
                        If lineSpecificIfStatement = new If();
                        String lineSpecificClauseName = routeFilterName
                              + "_ACTION_LINE_" + actionLineCounter;
                        lineSpecificIfStatement
                              .setComment(lineSpecificClauseName);
                        MatchPrefixSet mrf = new MatchPrefixSet(
                              new NamedPrefixSet(lineListName));
                        lineSpecificIfStatement.setGuard(mrf);
                        lineSpecificIfStatement.getTrueStatements().addAll(
                              toStatements(line.getThens()));
                        statements.add(lineSpecificIfStatement);
                     }
                  }
               }
               BooleanExpr booleanExpr = from.toBooleanExpr(this, _c, _w);
               conj.getConjuncts().add(booleanExpr);
            }
            BooleanExpr guard = conj.simplify();
            ifStatement.setGuard(guard);
            ifStatement.getTrueStatements().addAll(
                  toStatements(term.getThens()));
            statements.add(ifStatement);
         }
      }
      If endOfPolicy = new If();
      endOfPolicy.setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
      endOfPolicy.setTrueStatements(Collections
            .singletonList(Statements.ReturnFalse.toStaticStatement()));
      endOfPolicy.setFalseStatements(Collections
            .singletonList(Statements.Return.toStaticStatement()));
      statements.add(endOfPolicy);
      return routingPolicy;
   }

   private List<Statement> toStatements(Set<PsThen> thens) {
      List<Statement> thenStatements = new ArrayList<Statement>();
      List<PsThen> reorderedThens = new LinkedList<PsThen>();
      for (PsThen then : thens) {
         if (then instanceof PsThenAccept || then instanceof PsThenReject
               || then instanceof PsThenDefaultActionAccept
               || then instanceof PsThenDefaultActionReject) {
            reorderedThens.add(then);
         }
         else {
            reorderedThens.add(0, then);
         }
      }
      for (PsThen then : reorderedThens) {
         then.applyTo(thenStatements, this, _c, _w);
      }
      return thenStatements;
   }

   private org.batfish.datamodel.StaticRoute toStaticRoute(StaticRoute route) {
      Prefix prefix = route.getPrefix();
      Ip nextHopIp = route.getNextHopIp();
      String nextHopInterface = route.getDrop() ? CommonUtil.NULL_INTERFACE_NAME
            : route.getNextHopInterface();
      int administrativeCost = route.getMetric();
      Integer oldTag = route.getTag();
      int tag;
      tag = oldTag != null ? oldTag : -1;
      org.batfish.datamodel.StaticRoute newStaticRoute = new org.batfish.datamodel.StaticRoute(
            prefix, nextHopIp, nextHopInterface, administrativeCost, tag);
      return newStaticRoute;
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      _w = warnings;
      String hostname = getHostname();
      _c = new Configuration(hostname);
      _c.setConfigurationFormat(_vendor);
      _c.setRoles(_roles);

      // convert prefix lists to route filter lists
      for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
         String name = e.getKey();
         PrefixList pl = e.getValue();
         RouteFilterList rfl = new RouteFilterList(name);
         for (Prefix prefix : pl.getPrefixes()) {
            int prefixLength = prefix.getPrefixLength();
            org.batfish.datamodel.RouteFilterLine line = new org.batfish.datamodel.RouteFilterLine(
                  LineAction.ACCEPT, prefix, new SubRange(prefixLength,
                        prefixLength));
            rfl.addLine(line);
         }
         _c.getRouteFilterLists().put(name, rfl);
      }

      // remove ipv6 lines from firewall filters
      for (FirewallFilter filter : _filters.values()) {
         Set<String> toRemove = new HashSet<String>();
         for (Entry<String, FwTerm> e2 : filter.getTerms().entrySet()) {
            String termName = e2.getKey();
            FwTerm term = e2.getValue();
            if (term.getIpv6()) {
               toRemove.add(termName);
            }
         }
         for (String termName : toRemove) {
            filter.getTerms().remove(termName);
         }
      }

      // remove empty firewall filters (ipv6-only filters)
      Map<String, FirewallFilter> allFilters = new LinkedHashMap<String, FirewallFilter>();
      allFilters.putAll(_filters);
      for (Entry<String, FirewallFilter> e : allFilters.entrySet()) {
         String name = e.getKey();
         FirewallFilter filter = e.getValue();
         if (filter.getTerms().size() == 0) {
            _filters.remove(name);
         }
      }

      // convert firewall filters to ipaccesslists
      for (Entry<String, FirewallFilter> e : _filters.entrySet()) {
         String name = e.getKey();
         FirewallFilter filter = e.getValue();
         // TODO: support other filter families
         if (filter.getFamily() != Family.INET) {
            continue;
         }
         IpAccessList list = toIpAccessList(filter);
         _c.getIpAccessLists().put(name, list);
      }

      // convert firewall filters implementing routing policy to policymaps
      for (Entry<String, FirewallFilter> e : _filters.entrySet()) {
         String name = e.getKey();
         FirewallFilter filter = e.getValue();
         if (filter.getRoutingPolicy()) {
            // TODO: support other filter families
            if (filter.getFamily() != Family.INET) {
               continue;
            }
            PolicyMap routingPolicy = toRoutingPolicy(filter);
            _c.getPolicyMaps().put(name, routingPolicy);
         }
      }

      // convert route filters to route filter lists
      for (Entry<String, RouteFilter> e : _routeFilters.entrySet()) {
         String name = e.getKey();
         RouteFilter rf = e.getValue();
         RouteFilterList rfl = new RouteFilterList(name);
         for (RouteFilterLine line : rf.getLines()) {
            if (line.getThens().size() == 0) {
               line.applyTo(rfl);
            }
         }
         _c.getRouteFilterLists().put(name, rfl);
      }

      // convert community lists
      for (Entry<String, CommunityList> e : _communityLists.entrySet()) {
         String name = e.getKey();
         CommunityList cl = e.getValue();
         org.batfish.datamodel.CommunityList newCl = toCommunityList(cl);
         _c.getCommunityLists().put(name, newCl);
      }

      // convert policy-statements to policymaps
      for (Entry<String, PolicyStatement> e : _policyStatements.entrySet()) {
         String name = e.getKey();
         PolicyStatement ps = e.getValue();
         PolicyMap map = toPolicyMap(ps);
         _c.getPolicyMaps().put(name, map);
      }

      // convert policy-statements to RoutingPolicy objects
      for (Entry<String, PolicyStatement> e : _policyStatements.entrySet()) {
         String name = e.getKey();
         PolicyStatement ps = e.getValue();
         RoutingPolicy routingPolicy = toRoutingPolicy(ps);
         _c.getRoutingPolicies().put(name, routingPolicy);
      }

      // convert interfaces
      Map<String, Interface> allInterfaces = new LinkedHashMap<String, Interface>();

      for (Interface iface : _defaultRoutingInstance.getInterfaces().values()) {
         allInterfaces.putAll(iface.getUnits());
      }
      for (NodeDevice nd : _defaultRoutingInstance.getNodeDevices().values()) {
         for (Interface iface : nd.getInterfaces().values()) {
            allInterfaces.putAll(iface.getUnits());
         }
      }
      for (Entry<String, Interface> eUnit : allInterfaces.entrySet()) {
         String unitName = eUnit.getKey();
         Interface unitIface = eUnit.getValue();
         org.batfish.datamodel.Interface newUnitIface = toInterface(unitIface);
         _c.getInterfaces().put(unitName, newUnitIface);
      }

      // set router-id
      if (_defaultRoutingInstance.getRouterId() == null) {
         Interface loopback0 = _defaultRoutingInstance.getInterfaces().get(
               FIRST_LOOPBACK_INTERFACE_NAME);
         if (loopback0 != null) {
            Interface loopback0unit0 = loopback0.getUnits().get(
                  FIRST_LOOPBACK_INTERFACE_NAME + ".0");
            if (loopback0unit0 != null) {
               Prefix prefix = loopback0unit0.getPrimaryPrefix();
               if (prefix != null) {
                  // now we should set router-id
                  Ip routerId = prefix.getAddress();
                  _defaultRoutingInstance.setRouterId(routerId);
               }
            }
         }
      }

      // copy ike proposals
      _c.getIkeProposals().putAll(_ikeProposals);

      // convert ike policies
      for (Entry<String, IkePolicy> e : _ikePolicies.entrySet()) {
         String name = e.getKey();
         IkePolicy oldIkePolicy = e.getValue();
         org.batfish.datamodel.IkePolicy newPolicy = toIkePolicy(oldIkePolicy);
         _c.getIkePolicies().put(name, newPolicy);
      }

      // convert ike gateways
      for (Entry<String, IkeGateway> e : _ikeGateways.entrySet()) {
         String name = e.getKey();
         IkeGateway oldIkeGateway = e.getValue();
         org.batfish.datamodel.IkeGateway newIkeGateway = toIkeGateway(oldIkeGateway);
         _c.getIkeGateways().put(name, newIkeGateway);
      }

      // copy ipsec proposals
      _c.getIpsecProposals().putAll(_ipsecProposals);

      // convert ipsec policies
      for (Entry<String, IpsecPolicy> e : _ipsecPolicies.entrySet()) {
         String name = e.getKey();
         IpsecPolicy oldIpsecPolicy = e.getValue();
         org.batfish.datamodel.IpsecPolicy newPolicy = toIpsecPolicy(oldIpsecPolicy);
         _c.getIpsecPolicies().put(name, newPolicy);
      }

      // convert ipsec vpns
      for (Entry<String, IpsecVpn> e : _ipsecVpns.entrySet()) {
         String name = e.getKey();
         IpsecVpn oldIpsecVpn = e.getValue();
         org.batfish.datamodel.IpsecVpn newIpsecVpn = toIpsecVpn(oldIpsecVpn);
         _c.getIpsecVpns().put(name, newIpsecVpn);
      }

      // static routes
      for (StaticRoute route : _defaultRoutingInstance.getRibs()
            .get(RoutingInformationBase.RIB_IPV4_UNICAST).getStaticRoutes()
            .values()) {
         org.batfish.datamodel.StaticRoute newStaticRoute = toStaticRoute(route);
         _c.getStaticRoutes().add(newStaticRoute);
      }

      // aggregate routes
      for (AggregateRoute route : _defaultRoutingInstance.getRibs()
            .get(RoutingInformationBase.RIB_IPV4_UNICAST).getAggregateRoutes()
            .values()) {
         org.batfish.datamodel.GeneratedRoute newAggregateRoute = toAggregateRoute(route);
         _c.getGeneratedRoutes().add(newAggregateRoute);
      }

      // generated routes
      for (GeneratedRoute route : _defaultRoutingInstance.getRibs()
            .get(RoutingInformationBase.RIB_IPV4_UNICAST).getGeneratedRoutes()
            .values()) {
         org.batfish.datamodel.GeneratedRoute newGeneratedRoute = toGeneratedRoute(route);
         _c.getGeneratedRoutes().add(newGeneratedRoute);
      }

      // zones
      for (Zone zone : _zones.values()) {
         org.batfish.datamodel.Zone newZone = toZone(zone);
         _c.getZones().put(zone.getName(), newZone);
      }

      // default zone behavior
      _c.setDefaultCrossZoneAction(_defaultCrossZoneAction);
      _c.setDefaultInboundAction(_defaultInboundAction);

      // create ospf process
      if (_defaultRoutingInstance.getOspfAreas().size() > 0) {
         OspfProcess oproc = createOspfProcess();
         _c.setOspfProcess(oproc);
      }

      // create is-is process
      // is-is runs only if iso address is configured on lo0 unit 0
      Interface loopback0 = _defaultRoutingInstance.getInterfaces().get(
            FIRST_LOOPBACK_INTERFACE_NAME);
      if (loopback0 != null) {
         Interface loopback0unit0 = loopback0.getUnits().get(
               FIRST_LOOPBACK_INTERFACE_NAME + ".0");
         if (loopback0unit0 != null) {
            IsoAddress isisNet = loopback0unit0.getIsoAddress();
            if (isisNet != null) {
               // now we should create is-is process
               IsisProcess proc = createIsisProcess(isisNet);
               _c.setIsisProcess(proc);
            }
         }
      }

      // create bgp process
      if (_defaultRoutingInstance.getNamedBgpGroups().size() > 0
            || _defaultRoutingInstance.getIpBgpGroups().size() > 0) {
         BgpProcess proc = createBgpProcess();
         _c.setBgpProcess(proc);
      }

      // mark forwarding table export policy if it exists
      String forwardingTableExportPolicyName = _defaultRoutingInstance
            .getForwardingTableExportPolicy();
      if (forwardingTableExportPolicyName != null) {
         PolicyStatement forwardingTableExportPolicy = _policyStatements
               .get(forwardingTableExportPolicyName);
         if (forwardingTableExportPolicy != null) {
            setPolicyStatementReferent(forwardingTableExportPolicyName,
                  _defaultRoutingInstance, "Forwarding-table export policy");
         }
         else {
            _w.redFlag(
                  "Reference to undefined forwarding-table export policy: '"
                        + forwardingTableExportPolicyName + "'", UNDEFINED);
         }
      }

      // copy literal standard communities
      _c.getCommunities().addAll(_allStandardCommunities);

      // warn about unreferenced data structures
      warnUnreferencedPolicyStatements();
      warnUnreferencedFirewallFilters();
      warnUnreferencedIkeProposals();
      warnUnreferencedIkePolicies();
      warnUnreferencedIkeGateways();
      warnUnreferencedIpsecProposals();
      warnUnreferencedIpsecPolicies();
      warnUnusedPrefixLists();
      warnEmptyPrefixLists();
      warnAndDisableUnreferencedStInterfaces();
      return _c;
   }

   private org.batfish.datamodel.Zone toZone(Zone zone) {

      FirewallFilter inboundFilter = zone.getInboundFilter();
      IpAccessList inboundFilterList = null;
      if (inboundFilter != null) {
         inboundFilter.getReferers().put(zone,
               "inbound filter for zone: '" + zone.getName() + "'");
         inboundFilterList = _c.getIpAccessLists().get(inboundFilter.getName());
      }

      FirewallFilter fromHostFilter = zone.getFromHostFilter();
      IpAccessList fromHostFilterList = null;
      if (fromHostFilter != null) {
         fromHostFilter.getReferers().put(zone,
               "filter from junos-host to zone: '" + zone.getName() + "'");
         fromHostFilterList = _c.getIpAccessLists().get(
               fromHostFilter.getName());
      }

      FirewallFilter toHostFilter = zone.getToHostFilter();
      IpAccessList toHostFilterList = null;
      if (toHostFilter != null) {
         toHostFilter.getReferers().put(zone,
               "filter from zone: '" + zone.getName() + "' to junos-host");
         toHostFilterList = _c.getIpAccessLists().get(toHostFilter.getName());
      }

      org.batfish.datamodel.Zone newZone = new org.batfish.datamodel.Zone(
            zone.getName(), inboundFilterList, fromHostFilterList,
            toHostFilterList);
      for (Entry<Interface, FirewallFilter> e : zone
            .getInboundInterfaceFilters().entrySet()) {
         Interface inboundInterface = e.getKey();
         FirewallFilter inboundInterfaceFilter = e.getValue();
         String inboundInterfaceName = inboundInterface.getName();
         inboundInterfaceFilter.getReferers().put(
               zone,
               "inbound interface filter for zone: '" + zone.getName()
                     + "', interface: '" + inboundInterfaceName + "'");
         String inboundInterfaceFilterName = inboundInterfaceFilter.getName();
         org.batfish.datamodel.Interface newIface = _c.getInterfaces().get(
               inboundInterfaceName);
         IpAccessList inboundInterfaceFilterList = _c.getIpAccessLists().get(
               inboundInterfaceFilterName);
         newZone.getInboundInterfaceFilters().put(newIface.getName(),
               inboundInterfaceFilterList);
      }

      for (Entry<String, FirewallFilter> e : zone.getToZonePolicies()
            .entrySet()) {
         String toZoneName = e.getKey();
         FirewallFilter toZoneFilter = e.getValue();
         toZoneFilter.getReferers().put(
               zone,
               "cross-zone firewall filter from zone: '" + zone.getName()
                     + "' to zone: '" + toZoneName + "'");
         String toZoneFilterName = toZoneFilter.getName();
         IpAccessList toZoneFilterList = _c.getIpAccessLists().get(
               toZoneFilterName);
         newZone.getToZonePolicies().put(toZoneName, toZoneFilterList);
      }

      for (Interface iface : zone.getInterfaces()) {
         String ifaceName = iface.getName();
         org.batfish.datamodel.Interface newIface = _c.getInterfaces().get(
               ifaceName);
         newIface.setZone(newZone);
         FirewallFilter inboundInterfaceFilter = zone
               .getInboundInterfaceFilters().get(iface);
         IpAccessList inboundInterfaceFilterList;
         if (inboundInterfaceFilter != null) {
            String name = inboundInterfaceFilter.getName();
            inboundInterfaceFilterList = _c.getIpAccessLists().get(name);
         }
         else {
            inboundInterfaceFilterList = inboundFilterList;
         }
         newZone.getInboundInterfaceFilters().put(newIface.getName(),
               inboundInterfaceFilterList);
      }

      return newZone;
   }

   private void warnAndDisableUnreferencedStInterfaces() {
      for (Interface i : _defaultRoutingInstance.getInterfaces().values()) {
         for (Entry<String, Interface> e : i.getUnits().entrySet()) {
            String name = e.getKey();
            Interface iface = e.getValue();
            if (org.batfish.datamodel.Interface.computeInterfaceType(name,
                  _vendor) == InterfaceType.VPN && iface.isUnused()) {
               _w.redFlag("Unused vpn tunnel interface: '" + name + "'", UNUSED);
               _c.getInterfaces().remove(name);
            }
         }
      }
   }

   private void warnEmptyPrefixLists() {
      for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
         String name = e.getKey();
         PrefixList prefixList = e.getValue();
         if (!prefixList.getIpv6() && prefixList.getPrefixes().isEmpty()) {
            _w.redFlag("Empty prefix-list: '" + name + "'");
         }
      }
   }

   private void warnUnreferencedFirewallFilters() {
      for (Entry<String, FirewallFilter> e : _filters.entrySet()) {
         String name = e.getKey();
         FirewallFilter filter = e.getValue();
         if (filter.getFamily().equals(Family.INET) && filter.isUnused()) {
            _w.redFlag("Unused firewall-filter: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnreferencedIkeGateways() {
      for (Entry<String, IkeGateway> e : _ikeGateways.entrySet()) {
         String name = e.getKey();
         IkeGateway ikeGateway = e.getValue();
         if (ikeGateway.isUnused()) {
            _w.redFlag("Unused IKE gateway: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnreferencedIkePolicies() {
      for (Entry<String, IkePolicy> e : _ikePolicies.entrySet()) {
         String name = e.getKey();
         IkePolicy ikePolicy = e.getValue();
         if (ikePolicy.isUnused()) {
            _w.redFlag("Unused IKE policy: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnreferencedIkeProposals() {
      for (Entry<String, IkeProposal> e : _ikeProposals.entrySet()) {
         String name = e.getKey();
         IkeProposal ikeProposal = e.getValue();
         if (ikeProposal.isUnused()) {
            _w.redFlag("Unused IKE proposal: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnreferencedIpsecPolicies() {
      for (Entry<String, IpsecPolicy> e : _ipsecPolicies.entrySet()) {
         String name = e.getKey();
         IpsecPolicy ipsecPolicy = e.getValue();
         if (ipsecPolicy.isUnused()) {
            _w.redFlag("Unused IKE policy: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnreferencedIpsecProposals() {
      for (Entry<String, IpsecProposal> e : _ipsecProposals.entrySet()) {
         String name = e.getKey();
         IpsecProposal ipsecProposal = e.getValue();
         if (ipsecProposal.isUnused()) {
            _w.redFlag("Unused IKE proposal: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnreferencedPolicyStatements() {
      for (Entry<String, PolicyStatement> e : _policyStatements.entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         PolicyStatement ps = e.getValue();
         if (!ps.getIpv6() && ps.isUnused()) {
            _w.redFlag("Unused policy-statement: '" + name + "'", UNUSED);
         }
      }
   }

   private void warnUnusedPrefixLists() {
      for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
         String name = e.getKey();
         PrefixList prefixList = e.getValue();
         if (!prefixList.getIpv6() && prefixList.isUnused()
               && !_ignoredPrefixLists.contains(name)) {
            _w.redFlag("Unused prefix-list: '" + name + "'", UNUSED);
         }
      }
   }

}
