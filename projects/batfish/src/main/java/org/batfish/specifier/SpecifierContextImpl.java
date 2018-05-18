package org.batfish.specifier;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.main.Batfish;
import org.batfish.role.NodeRole;

public class SpecifierContextImpl implements SpecifierContext {
  private final @Nonnull Batfish _batfish;

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps;

  private final @Nonnull Map<String, Map<String, IpSpace>> _vrfOwnedIps;

  public SpecifierContextImpl(
      @Nonnull Batfish batfish, @Nonnull Map<String, Configuration> configs) {
    _batfish = batfish;
    _configs = configs;
    Map<Ip, Map<String, Set<String>>> ipInterfaceOwners =
        CommonUtil.computeIpInterfaceOwners(CommonUtil.computeNodeInterfaces(configs), true);
    _interfaceOwnedIps = CommonUtil.computeInterfaceOwnedIpSpaces(ipInterfaceOwners);
    _vrfOwnedIps =
        CommonUtil.computeVrfOwnedIpSpaces(
            CommonUtil.computeIpVrfOwners(ipInterfaceOwners, configs));
  }

  @Nonnull
  @Override
  public Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Nonnull
  @Override
  public Set<NodeRole> getNodeRolesByDimension(String dimension) {
    return _batfish.getNodeRoleDimension(dimension).getRoles();
  }

  @Override
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  @Override
  public Map<String, Map<String, IpSpace>> getVrfOwnedIps() {
    return _vrfOwnedIps;
  }
}
