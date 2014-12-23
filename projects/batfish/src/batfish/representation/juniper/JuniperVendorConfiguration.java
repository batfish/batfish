package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import batfish.collections.RoleSet;
import batfish.representation.Configuration;
import batfish.representation.IpAccessList;
import batfish.representation.IpAccessListLine;
import batfish.representation.LineAction;
import batfish.representation.VendorConfiguration;
import batfish.representation.VendorConversionException;

public final class JuniperVendorConfiguration extends JuniperConfiguration
      implements VendorConfiguration {

   private static final long serialVersionUID = 1L;

   private static final String VENDOR_NAME = "juniper";

   private final List<String> _conversionWarnings;

   private final RoleSet _roles;

   public JuniperVendorConfiguration() {
      _conversionWarnings = new ArrayList<String>();
      _roles = new RoleSet();
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
            //TODO: throw error if any transformation is being done
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

   @Override
   public Configuration toVendorIndependentConfiguration()
         throws VendorConversionException {
      String hostname = getHostname();
      Configuration c = new Configuration(hostname);
      c.setVendor(VENDOR_NAME);
      c.setRoles(_roles);

      // convert firewall filters to ipaccesslists
      for (Entry<String, FirewallFilter> e : _filters.entrySet()) {
         String name = e.getKey();
         FirewallFilter filter = e.getValue();
         // TODO: support other filter families
         if (filter.getFamily() != Family.INET) {
            continue;
         }
         IpAccessList list = toIpAccessList(filter);
         c.getIpAccessLists().put(name, list);
      }

      // convert policy-statements to policymaps

      // if (_defaultRoutingInstance.getOspfAreas().size() > 0) {
      // OspfProcess oproc = new OspfProcess();
      // for (String export_defaultRoutingInstance.getOspfExportPolicies()
      // }

      return c;
   }

}
