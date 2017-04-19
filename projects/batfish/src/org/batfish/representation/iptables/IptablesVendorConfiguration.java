package org.batfish.representation.iptables;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.common.Warnings;

public class IptablesVendorConfiguration extends IptablesConfiguration {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Configuration _c;

   private String _hostname;

   private transient Set<String> _unimplementedFeatures;

   private ConfigurationFormat _vendor;

   public void addAsIpAccessLists(Configuration config, Warnings warnings) {
      for (Entry<String, IptablesTable> e : _tables.entrySet()) {
         String tableName = e.getKey();
         IptablesTable table = e.getValue();
         for (Entry<String, IptablesChain> ec : table.getChains().entrySet()) {
            String chainName = ec.getKey();
            IptablesChain chain = ec.getValue();

            String aclName = toIpAccessListName(tableName, chainName);
            IpAccessList list = toIpAccessList(aclName, chain);

            config.getIpAccessLists().put(aclName, list);
         }
      }
   }

   @Override
   public String getHostname() {
      return _hostname;
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
   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _vendor = format;
   }

   private IpAccessList toIpAccessList(String aclName, IptablesChain chain) {
      IpAccessList acl = new IpAccessList(aclName,
            new LinkedList<IpAccessListLine>());

      for (IptablesRule rule : chain.getRules()) {
         IpAccessListLine aclLine = new IpAccessListLine();

         for (IptablesMatch match : rule.getMatchList()) {

            switch (match.getMatchType()) {

            case DESTINATION:
               IpWildcard dstWildCard = match.toIpWildcard();
               aclLine.getDstIps().add(dstWildCard);
               break;
            case DESTINATION_PORT:
               List<SubRange> dstPortRanges = match.toPortRanges();
               aclLine.getDstPorts().addAll(dstPortRanges);
               break;
            // case IN_INTERFACE:
            // case OUT_INTERFACE:
            // _warnings.unimplemented("Matching on incoming and outgoing
            // interface not supported");
            // break;
            case PROTOCOL:
               aclLine.getIpProtocols().add(match.toIpProtocol());
               break;
            case SOURCE:
               IpWildcard srcWildCard = match.toIpWildcard();
               aclLine.getSrcIps().add(srcWildCard);
               break;
            case SOURCE_PORT:
               List<SubRange> srcPortRanges = match.toPortRanges();
               aclLine.getSrcPorts().addAll(srcPortRanges);
               break;
            case IN_INTERFACE:
            case OUT_INTERFACE:
            default:
               throw new BatfishException(
                     "Unknown match type: " + match.getMatchType().toString());
            }
         }

         aclLine.setAction(rule.getIpAccessListLineAction());
         acl.getLines().add(aclLine);
      }

      // add a final line corresponding to default chain policy
      LineAction chainAction = chain.getIpAccessListLineAction();
      IpAccessListLine defaultLine = new IpAccessListLine();
      defaultLine.setAction(chainAction);
      acl.getLines().add(defaultLine);

      return acl;
   }

   private String toIpAccessListName(String tableName, String chainName) {
      return tableName + "::" + chainName;
   }

   @Override
   public Configuration toVendorIndependentConfiguration()
         throws VendorConversionException {
      String hostname = getHostname();
      _c = new Configuration(hostname);
      _c.setConfigurationFormat(_vendor);
      _c.setRoles(_roles);

      addAsIpAccessLists(_c, _w);

      return _c;
   }

}
