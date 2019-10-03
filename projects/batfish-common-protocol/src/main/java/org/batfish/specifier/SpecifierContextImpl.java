package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

/** Implementation of {@link SpecifierContext}. */
public class SpecifierContextImpl implements SpecifierContext {
  private final @Nonnull IBatfish _batfish;

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Supplier<ForwardingAnalysis> _forwardingAnalysis;

  private final @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps;

  public SpecifierContextImpl(@Nonnull IBatfish batfish, @Nonnull NetworkSnapshot networkSnapshot) {
    _batfish = batfish;
    _configs = _batfish.loadConfigurations(networkSnapshot);
    _interfaceOwnedIps =
        _batfish.getTopologyProvider().getIpOwners(networkSnapshot).getInterfaceOwnedIpSpaces();
    _forwardingAnalysis = Suppliers.memoize(() -> _batfish.loadDataPlane().getForwardingAnalysis());
  }

  @Nonnull
  @Override
  public Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _batfish.getReferenceLibraryData().getReferenceBook(bookName);
  }

  @Nonnull
  @Override
  public Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    return _batfish.getNodeRoleDimension(dimension);
  }

  @Override
  public IpSpace getInterfaceOwnedIps(String hostname, String iface) {
    return _interfaceOwnedIps
        .getOrDefault(hostname, ImmutableMap.of())
        .getOrDefault(iface, EmptyIpSpace.INSTANCE);
  }

  @Override
  public IpSpace getInterfaceLinkOwnedIps(String hostname, String ifaceName) {
    ForwardingAnalysis forwardingAnalysis = _forwardingAnalysis.get();
    Interface iface = _configs.get(hostname).getAllInterfaces().get(ifaceName);

    if (iface == null) {
      return EmptyIpSpace.INSTANCE;
    }

    String vrfName = iface.getVrfName();

    @Nullable IpSpace deliveredToSubnet =
        forwardingAnalysis
            .getDeliveredToSubnet()
            .getOrDefault(hostname, ImmutableMap.of())
            .getOrDefault(vrfName, ImmutableMap.of())
            .get(ifaceName);

    @Nullable IpSpace exitsNetwork =
        forwardingAnalysis
            .getExitsNetwork()
            .getOrDefault(hostname, ImmutableMap.of())
            .getOrDefault(vrfName, ImmutableMap.of())
            .get(ifaceName);

    return firstNonNull(AclIpSpace.union(deliveredToSubnet, exitsNetwork), EmptyIpSpace.INSTANCE);
  }
}
