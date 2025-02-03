package org.batfish.representation.iptables;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.representation.iptables.IptablesRule.IptablesActionType;
import org.batfish.vendor.VendorConfiguration;

public class IptablesVendorConfiguration extends IptablesConfiguration {

  private String _hostname;

  private transient Map<AclLine, String> _lineInInterfaces;

  private transient Map<AclLine, String> _lineOutInterfaces;

  private ConfigurationFormat _vendor;

  public void addAsIpAccessLists(Configuration config, VendorConfiguration vc, Warnings warnings) {
    _lineInInterfaces = new IdentityHashMap<>();
    _lineOutInterfaces = new IdentityHashMap<>();
    for (Entry<String, IptablesTable> e : getTables().entrySet()) {
      String tableName = e.getKey();
      IptablesTable table = e.getValue();
      for (Entry<String, IptablesChain> ec : table.getChains().entrySet()) {
        String chainName = ec.getKey();
        IptablesChain chain = ec.getValue();

        String aclName = toIpAccessListName(tableName, chainName);
        IpAccessList list =
            toIpAccessList(aclName, chain, vc, warnings, _lineInInterfaces, _lineOutInterfaces);

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
              + configuration.getHostname()
              + ": only mangle tables are supported");
    }

    if (prerouting != null) {
      for (Interface i : configuration.getAllInterfaces().values()) {

        String dbgName = configuration.getHostname() + ":" + i.getName();

        List<AclLine> newRules =
            prerouting.getLines().stream()
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
        IpAccessList acl = IpAccessList.builder().setName(aclName).setLines(newRules).build();
        if (configuration.getIpAccessLists().putIfAbsent(aclName, acl) != null) {
          throw new BatfishException(dbgName + " acl " + aclName + " already exists");
        }

        i.setIncomingFilter(acl);
      }
    }

    if (postrouting != null) {
      for (Interface i : configuration.getAllInterfaces().values()) {

        String dbgName = configuration.getHostname() + ":" + i.getName();

        List<AclLine> newRules =
            postrouting.getLines().stream()
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
        IpAccessList acl = IpAccessList.builder().setName(aclName).setLines(newRules).build();
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

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @VisibleForTesting
  static IpAccessList toIpAccessList(
      String aclName,
      IptablesChain chain,
      VendorConfiguration vc,
      Warnings warnings,
      Map<AclLine, String> lineInInterfaces,
      Map<AclLine, String> lineOutInterfaces) {
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();

    for (IptablesRule rule : chain.getRules()) {
      if (rule.getActionType() != IptablesActionType.ACCEPT
          && rule.getActionType() != IptablesActionType.DROP) {
        warnings.redFlag(
            String.format(
                "IpTables action type '%s' is not supported. Skipped '%s'",
                rule.getActionType(), rule.getName()));
        continue;
      }
      HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
      boolean anyInterface = true;
      List<IptablesMatch> inInterfaceMatches = new ArrayList<>();
      List<IptablesMatch> outInterfaceMatches = new ArrayList<>();

      for (IptablesMatch match : rule.getMatchList()) {
        switch (match.getMatchType()) {
          case DESTINATION:
            headerSpaceBuilder.setDstIps(
                AclIpSpace.union(headerSpaceBuilder.getDstIps(), match.toIpWildcard().toIpSpace()));
            break;
          case DESTINATION_PORT:
            headerSpaceBuilder.setDstPorts(
                Iterables.concat(headerSpaceBuilder.getDstPorts(), match.toPortRanges()));
            break;
          case IN_INTERFACE:
            inInterfaceMatches.add(match);
            anyInterface = false;
            break;
          case OUT_INTERFACE:
            outInterfaceMatches.add(match);
            anyInterface = false;
            break;
          case PROTOCOL:
            headerSpaceBuilder.setIpProtocols(
                Iterables.concat(
                    headerSpaceBuilder.getIpProtocols(), ImmutableSet.of(match.toIpProtocol())));
            break;
          case SOURCE:
            headerSpaceBuilder.setSrcIps(
                AclIpSpace.union(headerSpaceBuilder.getSrcIps(), match.toIpWildcard().toIpSpace()));
            break;
          case SOURCE_PORT:
            headerSpaceBuilder.setSrcPorts(
                Iterables.concat(headerSpaceBuilder.getSrcPorts(), match.toPortRanges()));
            break;
        }
      }
      ExprAclLine aclLine =
          ExprAclLine.builder()
              .setAction(rule.getIpAccessListLineAction())
              .setMatchCondition(new MatchHeaderSpace(headerSpaceBuilder.build()))
              .setName(rule.getName())
              .build();

      inInterfaceMatches.forEach(
          match ->
              lineInInterfaces.put(aclLine, vc.canonicalizeInterfaceName(match.toInterfaceName())));
      outInterfaceMatches.forEach(
          match ->
              lineOutInterfaces.put(
                  aclLine, vc.canonicalizeInterfaceName(match.toInterfaceName())));

      if (anyInterface) {
        lineInInterfaces.put(aclLine, null);
        lineOutInterfaces.put(aclLine, null);
      }

      lines.add(aclLine);
    }

    // add a final line corresponding to default chain policy
    LineAction chainAction = chain.getIpAccessListLineAction();
    ExprAclLine defaultLine =
        ExprAclLine.builder()
            .setAction(chainAction)
            .setMatchCondition(TrueExpr.INSTANCE)
            .setName("default")
            .build();
    lines.add(defaultLine);

    return IpAccessList.builder().setName(aclName).setLines(lines.build()).build();
  }

  private String toIpAccessListName(String tableName, String chainName) {
    return tableName + "::" + chainName;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    throw new BatfishException("Not meant to be converted to vendor-independent format");
  }
}
