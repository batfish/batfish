package org.batfish.representation.juniper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.collections.RoleSet;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.Warnings;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.IkeProposal;
import org.batfish.representation.Ip;
import org.batfish.representation.IpAccessList;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.IpsecProposal;
import org.batfish.representation.IsisInterfaceMode;
import org.batfish.representation.IsisLevel;
import org.batfish.representation.IsisProcess;
import org.batfish.representation.IsoAddress;
import org.batfish.representation.LineAction;
import org.batfish.representation.OspfMetricType;
import org.batfish.representation.OspfProcess;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.VendorConversionException;
import org.batfish.representation.juniper.BgpGroup.BgpGroupType;
import org.batfish.util.SubRange;
import org.batfish.util.Util;

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

   private boolean _defaultAddressSelection;

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
         mg.setLocalAs(_defaultRoutingInstance.getAs());
      }
      for (Entry<Ip, IpBgpGroup> e : _defaultRoutingInstance.getIpBgpGroups()
            .entrySet()) {
         Ip ip = e.getKey();
         IpBgpGroup ig = e.getValue();
         ig.cascadeInheritance();
         BgpNeighbor neighbor = new BgpNeighbor(ip);
         // import policies
         for (String importPolicyName : ig.getImportPolicies()) {
            PolicyMap importPolicy = _c.getPolicyMaps().get(importPolicyName);
            if (importPolicy == null) {
               throw new VendorConversionException(
                     "missing bgp import policy: \"" + importPolicyName + "\"");
            }
            neighbor.addInboundPolicyMap(importPolicy);
         }
         // export policies
         for (String exportPolicyName : ig.getExportPolicies()) {
            PolicyMap exportPolicy = _c.getPolicyMaps().get(exportPolicyName);
            if (exportPolicy == null) {
               throw new VendorConversionException(
                     "missing bgp export policy: \"" + exportPolicyName + "\"");
            }
            neighbor.addOutboundPolicyMap(exportPolicy);
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
         if (localAddress == null && _defaultAddressSelection) {
            Interface lo0 = _defaultRoutingInstance.getInterfaces().get(
                  FIRST_LOOPBACK_INTERFACE_NAME);
            if (lo0 != null) {
               Interface lo0_0 = lo0.getUnits().get(
                     FIRST_LOOPBACK_INTERFACE_NAME + ".0");
               if (lo0_0 != null) {
                  Prefix lo0_0Prefix = lo0_0.getPrimaryPrefix();
                  if (lo0_0Prefix != null) {
                     localAddress = lo0_0Prefix.getAddress();
                  }
               }
            }
         }
         if (localAddress == null) {
            // assign the ip of the interface that is likely connected to this
            // peer
            for (Interface iface : _defaultRoutingInstance.getInterfaces()
                  .values()) {
               for (Interface unit : iface.getUnits().values()) {
                  Prefix unitPrefix = unit.getPrimaryPrefix();
                  if (unitPrefix != null && unitPrefix.contains(ip)) {
                     localAddress = unitPrefix.getAddress();
                     break;
                  }
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
            _w.redFlag("undefined reference to is-is export policy: \""
                  + policyName + "\"");
         }
         else {
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
         newProc.getOutboundPolicyMaps().add(exportPolicy);
         // TODO: support type E1
         newProc.getPolicyMetricTypes().put(exportPolicy, OspfMetricType.E2);
      }
      // areas
      Map<Long, org.batfish.representation.OspfArea> newAreas = newProc
            .getAreas();
      for (Entry<Ip, OspfArea> e : _defaultRoutingInstance.getOspfAreas()
            .entrySet()) {
         Ip areaIp = e.getKey();
         long areaLong = areaIp.asLong();
         // OspfArea area = e.getValue();
         org.batfish.representation.OspfArea newArea = new org.batfish.representation.OspfArea(
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

   private void placeInterfaceIntoArea(
         Map<Long, org.batfish.representation.OspfArea> newAreas, String name,
         Interface iface) {
      org.batfish.representation.Interface newIface = _c.getInterfaces().get(
            name);
      Ip ospfArea = iface.getOspfActiveArea();
      if (ospfArea != null) {
         long ospfAreaLong = ospfArea.asLong();
         org.batfish.representation.OspfArea newArea = newAreas
               .get(ospfAreaLong);
         newArea.getInterfaces().add(newIface);
      }
   }

   public void setDefaultAddressSelection(boolean defaultAddressSelection) {
      _defaultAddressSelection = defaultAddressSelection;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _vendor = format;
   }

   private org.batfish.representation.GeneratedRoute toAggregateRoute(
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
      rfList.addLine(new org.batfish.representation.RouteFilterLine(
            LineAction.ACCEPT, prefix, new SubRange(prefixLength + 1, 32)));
      PolicyMapMatchLine matchLine = new PolicyMapMatchRouteFilterListLine(
            Collections.singleton(rfList));
      clause.getMatchLines().add(matchLine);
      Set<PolicyMap> policies = Collections.singleton(policy);
      org.batfish.representation.GeneratedRoute newRoute = new org.batfish.representation.GeneratedRoute(
            prefix, administrativeCost, policies);
      newRoute.setDiscard(true);
      _c.getPolicyMaps().put(policyName, policy);
      _c.getRouteFilterLists().put(rflName, rfList);
      return newRoute;
   }

   private org.batfish.representation.CommunityList toCommunityList(
         CommunityList cl) {
      String name = cl.getName();
      List<org.batfish.representation.CommunityListLine> newLines = new ArrayList<org.batfish.representation.CommunityListLine>();
      for (CommunityListLine line : cl.getLines()) {
         String regex = line.getRegex();
         String javaRegex = communityRegexToJavaRegex(regex);
         org.batfish.representation.CommunityListLine newLine = new org.batfish.representation.CommunityListLine(
               LineAction.ACCEPT, javaRegex);
         newLines.add(newLine);
      }
      org.batfish.representation.CommunityList newCl = new org.batfish.representation.CommunityList(
            name, newLines);
      return newCl;
   }

   private org.batfish.representation.GeneratedRoute toGeneratedRoute(
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
            throw new VendorConversionException(
                  "missing generated route policy: \"" + policyName + "\"");
         }
         policies.add(policy);
      }
      org.batfish.representation.GeneratedRoute newRoute = new org.batfish.representation.GeneratedRoute(
            prefix, administrativeCost, policies);
      newRoute.setMetric(metric);
      return newRoute;
   }

   private org.batfish.representation.IkeGateway toIkeGateway(
         IkeGateway oldIkeGateway) {
      String name = oldIkeGateway.getName();
      org.batfish.representation.IkeGateway newIkeGateway = new org.batfish.representation.IkeGateway(
            name);

      // address
      newIkeGateway.setAddress(oldIkeGateway.getAddress());

      // external interface
      Interface oldExternalInterface = oldIkeGateway.getExternalInterface();
      if (oldExternalInterface != null) {
         String externalInterfaceName = oldExternalInterface.getName();
         org.batfish.representation.Interface newExternalInterface = _c
               .getInterfaces().get(externalInterfaceName);
         if (newExternalInterface == null) {
            _w.redFlag("Reference to undefined interface: \""
                  + externalInterfaceName + "\" in ike gateway: \"" + name
                  + "\"");
         }
         else {
            newIkeGateway.setExternalInterface(newExternalInterface);
         }
      }
      else {
         _w.redFlag("No external interface set for ike gateway: \"" + name
               + "\"");
      }

      // ike policy
      String ikePolicyName = oldIkeGateway.getIkePolicy();
      org.batfish.representation.IkePolicy newIkePolicy = _c.getIkePolicies()
            .get(ikePolicyName);
      if (newIkePolicy == null) {
         _w.redFlag("Reference to undefined ike policy: \"" + ikePolicyName
               + "\" in ike gateway: \"" + name + "\"");
      }
      else {
         newIkeGateway.setIkePolicy(newIkePolicy);
      }

      return newIkeGateway;
   }

   private org.batfish.representation.IkePolicy toIkePolicy(
         IkePolicy oldIkePolicy) {
      String name = oldIkePolicy.getName();
      org.batfish.representation.IkePolicy newIkePolicy = new org.batfish.representation.IkePolicy(
            name);

      // pre-shared-key
      newIkePolicy.setPreSharedKey(oldIkePolicy.getPreSharedKey());

      // ike proposals
      for (String ikeProposalName : oldIkePolicy.getProposals()) {
         IkeProposal ikeProposal = _c.getIkeProposals().get(ikeProposalName);
         if (ikeProposal == null) {
            _w.redFlag("Reference to undefined ike proposal: \""
                  + ikeProposalName + "\" in ike policy: \"" + name + "\"");
         }
         else {
            newIkePolicy.getProposals().put(ikeProposalName, ikeProposal);
         }
      }

      return newIkePolicy;
   }

   private org.batfish.representation.Interface toInterface(Interface iface) {
      String name = iface.getName();
      org.batfish.representation.Interface newIface = new org.batfish.representation.Interface(
            name);
      String inAclName = iface.getIncomingFilter();
      if (inAclName != null) {
         IpAccessList inAcl = _c.getIpAccessLists().get(inAclName);
         if (inAcl == null) {
            throw new VendorConversionException("missing incoming acl: \""
                  + inAclName + "\"");
         }
         newIface.setIncomingFilter(inAcl);
      }
      String outAclName = iface.getOutgoingFilter();
      if (outAclName != null) {
         IpAccessList outAcl = _c.getIpAccessLists().get(outAclName);
         if (outAcl == null) {
            throw new VendorConversionException("missing outgoing acl: \""
                  + outAclName + "\"");
         }
         newIface.setOutgoingFilter(outAcl);
      }
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
            _w.redFlag("missing action in firewall filter: \"" + name
                  + "\", term: \"" + term.getName() + "\"");
            action = LineAction.REJECT;
         }
         IpAccessListLine line = new IpAccessListLine();
         line.setAction(action);
         for (FwFrom from : term.getFroms()) {
            from.applyTo(line);
         }
         lines.add(line);
      }
      IpAccessList list = new IpAccessList(name, lines);
      return list;
   }

   private org.batfish.representation.IpsecPolicy toIpsecPolicy(
         IpsecPolicy oldIpsecPolicy) {
      String name = oldIpsecPolicy.getName();
      org.batfish.representation.IpsecPolicy newIpsecPolicy = new org.batfish.representation.IpsecPolicy(
            name);

      // ipsec proposals
      for (String ipsecProposalName : oldIpsecPolicy.getProposals()) {
         IpsecProposal ipsecProposal = _c.getIpsecProposals().get(
               ipsecProposalName);
         if (ipsecProposal == null) {
            _w.redFlag("Reference to undefined ipsec proposal: \""
                  + ipsecProposalName + "\" in ipsec policy: \"" + name + "\"");
         }
         else {
            newIpsecPolicy.getProposals().put(ipsecProposalName, ipsecProposal);
         }
      }

      // perfect-forward-secrecy diffie-hellman key group
      newIpsecPolicy.setPfsKeyGroup(oldIpsecPolicy.getPfsKeyGroup());

      return newIpsecPolicy;
   }

   private org.batfish.representation.IpsecVpn toIpsecVpn(IpsecVpn oldIpsecVpn) {
      String name = oldIpsecVpn.getName();
      org.batfish.representation.IpsecVpn newIpsecVpn = new org.batfish.representation.IpsecVpn(
            name, _c);

      // bind interface
      Interface oldBindInterface = oldIpsecVpn.getBindInterface();
      if (oldBindInterface != null) {
         String bindInterfaceName = oldBindInterface.getName();
         org.batfish.representation.Interface newBindInterface = _c
               .getInterfaces().get(bindInterfaceName);
         if (newBindInterface == null) {
            _w.redFlag("Reference to undefined interface: \""
                  + bindInterfaceName + "\" in ipsec vpn: \"" + name + "\"");
         }
         else {
            newIpsecVpn.setBindInterface(newBindInterface);
         }
      }
      else {
         _w.redFlag("No bind interface set for ipsec vpn: \"" + name + "\"");
      }

      // ike gateway
      String ikeGatewayName = oldIpsecVpn.getGateway();
      org.batfish.representation.IkeGateway ikeGateway = _c.getIkeGateways()
            .get(ikeGatewayName);
      if (ikeGateway == null) {
         _w.redFlag("Reference to undefined ike gateway: \"" + ikeGatewayName
               + "\" in ipsec vpn: \"" + name + "\"");
      }
      else {
         newIpsecVpn.setGateway(ikeGateway);
      }

      // ipsec policy
      String ipsecPolicyName = oldIpsecVpn.getIpsecPolicy();
      org.batfish.representation.IpsecPolicy ipsecPolicy = _c
            .getIpsecPolicies().get(ipsecPolicyName);
      if (ipsecPolicy == null) {
         _w.redFlag("Reference to undefined ipsec policy: \"" + ipsecPolicyName
               + "\" in ipsec vpn: \"" + name + "\"");
      }
      else {
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
                        then.applyTo(lineSpecificClause, _c, null);
                     }
                     map.getClauses().add(lineSpecificClause);
                  }
               }
            }
            from.applyTo(clause, _c, _w);
         }
         for (PsThen then : term.getThens()) {
            then.applyTo(clause, _c, _w);
         }
         map.getClauses().add(clause);
      }
      return map;
   }

   private org.batfish.representation.StaticRoute toStaticRoute(
         StaticRoute route) {
      Prefix prefix = route.getPrefix();
      Ip nextHopIp = route.getNextHopIp();
      String nextHopInterface = route.getDrop() ? Util.NULL_INTERFACE_NAME
            : route.getNextHopInterface();
      int administrativeCost = route.getMetric();
      Integer oldTag = route.getTag();
      int tag;
      tag = oldTag != null ? oldTag : -1;
      org.batfish.representation.StaticRoute newStaticRoute = new org.batfish.representation.StaticRoute(
            prefix, nextHopIp, nextHopInterface, administrativeCost, tag);
      return newStaticRoute;
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      _w = warnings;
      String hostname = getHostname();
      _c = new Configuration(hostname);
      _c.setVendor(_vendor);
      _c.setRoles(_roles);

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

      // convert prefix lists to route filter lists
      for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
         String name = e.getKey();
         PrefixList pl = e.getValue();
         RouteFilterList rfl = new RouteFilterList(name);
         for (Prefix prefix : pl.getPrefixes()) {
            int prefixLength = prefix.getPrefixLength();
            org.batfish.representation.RouteFilterLine line = new org.batfish.representation.RouteFilterLine(
                  LineAction.ACCEPT, prefix, new SubRange(prefixLength,
                        prefixLength));
            rfl.addLine(line);
         }
         _c.getRouteFilterLists().put(name, rfl);
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
         org.batfish.representation.CommunityList newCl = toCommunityList(cl);
         _c.getCommunityLists().put(name, newCl);
      }

      // convert policy-statements to policymaps
      for (Entry<String, PolicyStatement> e : _policyStatements.entrySet()) {
         String name = e.getKey();
         PolicyStatement ps = e.getValue();
         PolicyMap map = toPolicyMap(ps);
         _c.getPolicyMaps().put(name, map);
      }

      // convert interfaces
      for (Entry<String, Interface> e : _defaultRoutingInstance.getInterfaces()
            .entrySet()) {
         String name = e.getKey();
         Interface iface = e.getValue();
         org.batfish.representation.Interface newIface = toInterface(iface);
         _c.getInterfaces().put(name, newIface);
         for (Entry<String, Interface> eUnit : iface.getUnits().entrySet()) {
            String unitName = eUnit.getKey();
            Interface unitIface = eUnit.getValue();
            org.batfish.representation.Interface newUnitIface = toInterface(unitIface);
            _c.getInterfaces().put(unitName, newUnitIface);
         }
      }

      // copy ike proposals
      _c.getIkeProposals().putAll(_ikeProposals);

      // convert ike policies
      for (Entry<String, IkePolicy> e : _ikePolicies.entrySet()) {
         String name = e.getKey();
         IkePolicy oldIkePolicy = e.getValue();
         org.batfish.representation.IkePolicy newPolicy = toIkePolicy(oldIkePolicy);
         _c.getIkePolicies().put(name, newPolicy);
      }

      // convert ike gateways
      for (Entry<String, IkeGateway> e : _ikeGateways.entrySet()) {
         String name = e.getKey();
         IkeGateway oldIkeGateway = e.getValue();
         org.batfish.representation.IkeGateway newIkeGateway = toIkeGateway(oldIkeGateway);
         _c.getIkeGateways().put(name, newIkeGateway);
      }

      // copy ipsec proposals
      _c.getIpsecProposals().putAll(_ipsecProposals);

      // convert ipsec policies
      for (Entry<String, IpsecPolicy> e : _ipsecPolicies.entrySet()) {
         String name = e.getKey();
         IpsecPolicy oldIpsecPolicy = e.getValue();
         org.batfish.representation.IpsecPolicy newPolicy = toIpsecPolicy(oldIpsecPolicy);
         _c.getIpsecPolicies().put(name, newPolicy);
      }

      // convert ipsec vpns
      for (Entry<String, IpsecVpn> e : _ipsecVpns.entrySet()) {
         String name = e.getKey();
         IpsecVpn oldIpsecVpn = e.getValue();
         org.batfish.representation.IpsecVpn newIpsecVpn = toIpsecVpn(oldIpsecVpn);
         _c.getIpsecVpns().put(name, newIpsecVpn);
      }

      // static routes
      for (StaticRoute route : _defaultRoutingInstance.getRibs()
            .get(RoutingInformationBase.RIB_IPV4_UNICAST).getStaticRoutes()
            .values()) {
         org.batfish.representation.StaticRoute newStaticRoute = toStaticRoute(route);
         _c.getStaticRoutes().add(newStaticRoute);
      }

      // aggregate routes
      for (AggregateRoute route : _defaultRoutingInstance.getRibs()
            .get(RoutingInformationBase.RIB_IPV4_UNICAST).getAggregateRoutes()
            .values()) {
         org.batfish.representation.GeneratedRoute newAggregateRoute = toAggregateRoute(route);
         _c.getGeneratedRoutes().add(newAggregateRoute);
      }

      // generated routes
      for (GeneratedRoute route : _defaultRoutingInstance.getRibs()
            .get(RoutingInformationBase.RIB_IPV4_UNICAST).getGeneratedRoutes()
            .values()) {
         org.batfish.representation.GeneratedRoute newGeneratedRoute = toGeneratedRoute(route);
         _c.getGeneratedRoutes().add(newGeneratedRoute);
      }

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

      return _c;
   }

}
