package org.batfish.representation.iptables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.vendor.VendorConfiguration;

public class IptablesVendorConfiguration extends IptablesConfiguration {

  /** */
  private static final long serialVersionUID = 1L;

  private String _hostname;

  private transient Map<IpAccessListLine, String> _lineInInterfaces;

  private transient Map<IpAccessListLine, String> _lineOutInterfaces;

  private transient Set<String> _unimplementedFeatures;

  private ConfigurationFormat _vendor;

  public void addAsIpAccessLists(Configuration config, VendorConfiguration vc, Warnings warnings) {
    _lineInInterfaces = new IdentityHashMap<>();
    _lineOutInterfaces = new IdentityHashMap<>();
    for (Entry<String, IptablesTable> e : _tables.entrySet()) {
      String tableName = e.getKey();
      IptablesTable table = e.getValue();
      for (Entry<String, IptablesChain> ec : table.getChains().entrySet()) {
        String chainName = ec.getKey();
        IptablesChain chain = ec.getValue();

        String aclName = toIpAccessListName(tableName, chainName);
        IpAccessList list = toIpAccessList(aclName, chain, vc);

        config.getIpAccessLists().put(aclName, list);
      }
    }
  }

  public void applyAsOverlay(Configuration configuration, Warnings warnings) {

    IpAccessList prerouting = configuration.getIpAccessLists().remove("mangle::PREROUTING");
    IpAccessList postrouting = configuration.getIpAccessLists().remove("mangle::POSTROUTING");

    if (!configuration.getIpAccessLists().isEmpty()) {
      throw new BatfishException(
          "Merging iptables rules for "
              + configuration.getName()
              + ": only mangle tables are supported");
    }

    if (prerouting != null) {
      for (Interface i : configuration.getInterfaces().values()) {

        String dbgName = configuration.getHostname() + ":" + i.getName();

        List<IpAccessListLine> newRules =
            prerouting
                .getLines()
                .stream()
                .filter(
                    l -> {
                      String iface = _lineInInterfaces.get(l);
                      return iface == null || i.getName().equals(iface);
                    })
                .collect(Collectors.toList());

        // TODO: ipv6

        if (i.getIncomingFilter() != null) {
          throw new BatfishException(
              dbgName + " already has a filter," + " cannot combine with iptables rules!");
        }

        String aclName = "iptables_" + i.getName() + "_ingress";
        IpAccessList acl = new IpAccessList(aclName, newRules);
        if (configuration.getIpAccessLists().putIfAbsent(aclName, acl) != null) {
          throw new BatfishException(dbgName + " acl " + aclName + " already exists");
        }

        i.setIncomingFilter(acl);
      }
    }

    if (postrouting != null) {
      for (Interface i : configuration.getInterfaces().values()) {

        String dbgName = configuration.getHostname() + ":" + i.getName();

        List<IpAccessListLine> newRules =
            postrouting
                .getLines()
                .stream()
                .filter(
                    l -> {
                      String iface = _lineOutInterfaces.get(l);
                      return iface == null || i.getName().equals(iface);
                    })
                .collect(Collectors.toList());

        // TODO: ipv6

        if (i.getOutgoingFilter() != null) {
          throw new BatfishException(
              dbgName + " already has a filter," + " cannot combine with iptables rules!");
        }

        String aclName = "iptables_" + i.getName() + "_egress";
        IpAccessList acl = new IpAccessList(aclName, newRules);
        if (configuration.getIpAccessLists().putIfAbsent(aclName, acl) != null) {
          throw new BatfishException(dbgName + " acl " + aclName + " already exists");
        }

        i.setOutgoingFilter(acl);
      }
    }
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public SortedSet<String> getRoles() {
    return _roles;
  }

  @Override
  public Set<String> getUnimplementedFeatures() {
    return _unimplementedFeatures;
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setRoles(SortedSet<String> roles) {
    _roles.addAll(roles);
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  private IpAccessList toIpAccessList(String aclName, IptablesChain chain, VendorConfiguration vc) {
    ImmutableList.Builder<IpAccessListLine> lines = ImmutableList.builder();

    for (IptablesRule rule : chain.getRules()) {
      IpAccessListLine aclLine = new IpAccessListLine();
      boolean anyInterface = false;

      for (IptablesMatch match : rule.getMatchList()) {

        switch (match.getMatchType()) {
          case DESTINATION:
            aclLine.setDstIps(
                Iterables.concat(aclLine.getDstIps(), Collections.singleton(match.toIpWildcard())));
            break;
          case DESTINATION_PORT:
            aclLine.setDstPorts(Iterables.concat(aclLine.getDstPorts(), match.toPortRanges()));
            break;
          case IN_INTERFACE:
            _lineInInterfaces.put(aclLine, vc.canonicalizeInterfaceName(match.toInterfaceName()));
            anyInterface = false;
            break;
          case OUT_INTERFACE:
            _lineOutInterfaces.put(aclLine, vc.canonicalizeInterfaceName(match.toInterfaceName()));
            anyInterface = false;
            break;
          case PROTOCOL:
            aclLine.setIpProtocols(
                Iterables.concat(
                    aclLine.getIpProtocols(), Collections.singleton(match.toIpProtocol())));
            break;
          case SOURCE:
            aclLine.setSrcIps(
                Iterables.concat(aclLine.getSrcIps(), Collections.singleton(match.toIpWildcard())));
            break;
          case SOURCE_PORT:
            aclLine.setSrcPorts(Iterables.concat(aclLine.getSrcPorts(), match.toPortRanges()));
            break;
          default:
            throw new BatfishException("Unknown match type: " + match.getMatchType());
        }
      }

      if (anyInterface) {
        _lineInInterfaces.put(aclLine, null);
        _lineOutInterfaces.put(aclLine, null);
      }

      aclLine.setName(rule.getName());
      aclLine.setAction(rule.getIpAccessListLineAction());
      lines.add(aclLine);
    }

    // add a final line corresponding to default chain policy
    LineAction chainAction = chain.getIpAccessListLineAction();
    IpAccessListLine defaultLine = new IpAccessListLine();
    defaultLine.setAction(chainAction);
    defaultLine.setName("default");
    lines.add(defaultLine);

    IpAccessList acl = new IpAccessList(aclName, lines.build());
    return acl;
  }

  private String toIpAccessListName(String tableName, String chainName) {
    return tableName + "::" + chainName;
  }

  @Override
  public Configuration toVendorIndependentConfiguration() throws VendorConversionException {
    throw new BatfishException("Not meant to be converted to vendor-independent format");
  }
}
