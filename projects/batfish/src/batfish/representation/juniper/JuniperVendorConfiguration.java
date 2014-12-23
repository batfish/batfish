package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import batfish.collections.RoleSet;
import batfish.representation.Configuration;
import batfish.representation.Ip;
import batfish.representation.IpAccessList;
import batfish.representation.IpAccessListLine;
import batfish.representation.LineAction;
import batfish.representation.OspfProcess;
import batfish.representation.PolicyMap;
import batfish.representation.PolicyMapClause;
import batfish.representation.RouteFilterList;
import batfish.representation.VendorConfiguration;
import batfish.representation.VendorConversionException;

public final class JuniperVendorConfiguration extends JuniperConfiguration
      implements VendorConfiguration {

   private static final long serialVersionUID = 1L;

   private static final String VENDOR_NAME = "juniper";

   private Configuration _c;

   private final List<String> _conversionWarnings;

   private final RoleSet _roles;

   public JuniperVendorConfiguration() {
      _conversionWarnings = new ArrayList<String>();
      _roles = new RoleSet();
   }

   private OspfProcess createOspfProcess() {
      OspfProcess newProc = new OspfProcess();
      // export policies
      for (String exportPolicyName : _defaultRoutingInstance
            .getOspfExportPolicies()) {
         PolicyMap exportPolicy = _c.getPolicyMaps().get(exportPolicyName);
         newProc.getOutboundPolicyMaps().add(exportPolicy);
      }
      // areas
      Map<Long, batfish.representation.OspfArea> newAreas = newProc.getAreas();
      for (Entry<Ip, OspfArea> e : _defaultRoutingInstance.getOspfAreas()
            .entrySet()) {
         Ip areaIp = e.getKey();
         long areaLong = areaIp.asLong();
         // OspfArea area = e.getValue();
         batfish.representation.OspfArea newArea = new batfish.representation.OspfArea(
               areaLong);
         newAreas.put(areaLong, newArea);
      }
      // place interfaces into areas
      for (Entry<String, Interface> e : _defaultRoutingInstance.getInterfaces()
            .entrySet()) {
         String name = e.getKey();
         Interface iface = e.getValue();
         batfish.representation.Interface newIface = _c.getInterfaces().get(
               name);
         Ip ospfArea = iface.getOspfArea();
         if (ospfArea != null) {
            long ospfAreaLong = ospfArea.asLong();
            batfish.representation.OspfArea newArea = newAreas
                  .get(ospfAreaLong);
            newArea.getInterfaces().add(newIface);
         }
      }
      newProc.setRouterId(_defaultRoutingInstance.getRouterId());
      return newProc;
   }

   @Override
   public List<String> getConversionWarnings() {
      return _conversionWarnings;
   }

   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   private batfish.representation.CommunityList toCommunityList(CommunityList cl) {
      String name = cl.getName();
      List<batfish.representation.CommunityListLine> newLines = new ArrayList<batfish.representation.CommunityListLine>();
      for (CommunityListLine line : cl.getLines()) {
         batfish.representation.CommunityListLine newLine = new batfish.representation.CommunityListLine(
               LineAction.ACCEPT, line.getRegex());
         newLines.add(newLine);
      }
      batfish.representation.CommunityList newCl = new batfish.representation.CommunityList(
            name, newLines);
      return newCl;
   }

   private batfish.representation.Interface toInterface(Interface iface) {
      String name = iface.getName();
      batfish.representation.Interface newIface = new batfish.representation.Interface(
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
      if (iface.getPrefix() != null) {
         newIface.setIp(iface.getPrefix().getAddress());
         newIface.setSubnetMask(iface.getPrefix().getSubnetMask());
      }
      newIface.setActive(iface.getActive());
      newIface.setAccessVlan(iface.getAccessVlan());
      newIface.setNativeVlan(iface.getNativeVlan());
      newIface.setSwitchportMode(iface.getSwitchportMode());
      newIface.setSwitchportTrunkEncapsulation(iface
            .getSwitchportTrunkEncapsulation());
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
         else {
            throw new VendorConversionException(
                  "not sure what to do without corresponding access list action from firewall filter term");
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

   private PolicyMap toPolicyMap(PolicyStatement ps) {
      List<PolicyMapClause> clauses = new ArrayList<PolicyMapClause>();
      String name = ps.getName();
      PolicyMap map = new PolicyMap(name, clauses);
      boolean singleton = ps.getSingletonTerm().getFroms().size() > 0
            || ps.getSingletonTerm().getThens().size() > 0;
      Collection<PsTerm> terms = singleton ? Collections.singleton(ps
            .getSingletonTerm()) : ps.getTerms().values();
      for (PsTerm term : terms) {
         PolicyMapClause clause = new PolicyMapClause();
         clause.setName(term.getName());
         for (PsFrom from : term.getFroms()) {
            from.applyTo(clause, _c);
         }
         for (PsThen then : term.getThens()) {
            then.applyTo(clause, _c);
         }
      }
      return map;
   }

   @Override
   public Configuration toVendorIndependentConfiguration()
         throws VendorConversionException {
      String hostname = getHostname();
      _c = new Configuration(hostname);
      _c.setVendor(VENDOR_NAME);
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

      // convert route filters to route filter lists
      for (Entry<String, RouteFilter> e : _routeFilters.entrySet()) {
         String name = e.getKey();
         RouteFilter rf = e.getValue();
         RouteFilterList rfl = new RouteFilterList(name);
         for (RouteFilterLine line : rf.getLines()) {
            line.applyTo(rfl);
         }
         _c.getRouteFilterLists().put(name, rfl);
      }

      // convert community lists
      for (Entry<String, CommunityList> e : _communityLists.entrySet()) {
         String name = e.getKey();
         CommunityList cl = e.getValue();
         batfish.representation.CommunityList newCl = toCommunityList(cl);
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
         batfish.representation.Interface newIface = toInterface(iface);
         _c.getInterfaces().put(name, newIface);
      }

      // create ospf process
      if (_defaultRoutingInstance.getOspfAreas().size() > 0) {
         OspfProcess oproc = createOspfProcess();
         _c.setOspfProcess(oproc);
      }

      return _c;
   }
}
