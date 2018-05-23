package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpSpace;
import org.batfish.role.NodeRole;

public class MockSpecifierContext implements SpecifierContext {

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull
  public Map<String, Map<String, IpSpace>> get_interfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  @Nonnull
  public Map<String, Map<String, IpSpace>> get_vrfOwnedIps() {
    return _vrfOwnedIps;
  }

  public static final class Builder {
    private @Nonnull Map<String, Configuration> _configs = ImmutableMap.of();

    private @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps = ImmutableMap.of();

    private @Nonnull Map<String, Set<NodeRole>> _nodeRolesByDimension = ImmutableMap.of();

    private @Nonnull Map<String, Map<String, IpSpace>> _vrfOwnedIps = ImmutableMap.of();

    private Builder() {}

    public Builder setConfigs(Map<String, Configuration> configs) {
      _configs = ImmutableMap.copyOf(configs);
      return this;
    }

    public Builder setInterfaceOwnedIps(Map<String, Map<String, IpSpace>> interfaceOwnedIps) {
      _interfaceOwnedIps = interfaceOwnedIps;
      return this;
    }

    public Builder setNodeRolesByDimension(Map<String, Set<NodeRole>> nodeRolesByDimension) {
      _nodeRolesByDimension = ImmutableMap.copyOf(nodeRolesByDimension);
      return this;
    }

    public Builder setVrfOwnedIps(Map<String, Map<String, IpSpace>> vrfOwnedIps) {
      _vrfOwnedIps = vrfOwnedIps;
      return this;
    }

    public MockSpecifierContext build() {
      return new MockSpecifierContext(this);
    }
  }

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps;

  private final @Nonnull Map<String, Set<NodeRole>> _nodeRolesByDimension;

  private final @Nonnull Map<String, Map<String, IpSpace>> _vrfOwnedIps;

  private MockSpecifierContext(Builder builder) {
    _configs = builder._configs;
    _interfaceOwnedIps = builder._interfaceOwnedIps;
    _nodeRolesByDimension = builder._nodeRolesByDimension;
    _vrfOwnedIps = builder._vrfOwnedIps;
  }

  @Override
  @Nonnull
  public Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Override
  @Nonnull
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  @Override
  @Nonnull
  public Set<NodeRole> getNodeRolesByDimension(String dimension) {
    return _nodeRolesByDimension.get(dimension);
  }

  @Override
  @Nonnull
  public Map<String, Map<String, IpSpace>> getVrfOwnedIps() {
    return _vrfOwnedIps;
  }
}
