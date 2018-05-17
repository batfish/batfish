package org.batfish.specifier;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.role.NodeRole;

public class MockSpecifierContext implements SpecifierContext {
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nonnull Map<String, Configuration> _configs = ImmutableMap.of();
    private @Nonnull Map<String, Set<NodeRole>> _nodeRolesByDimension = ImmutableMap.of();

    private Builder() {}

    public Builder setConfigs(Map<String, Configuration> configs) {
      _configs = ImmutableMap.copyOf(configs);
      return this;
    }

    public Builder setNodeRolesByDimension(Map<String, Set<NodeRole>> nodeRolesByDimension) {
      _nodeRolesByDimension = ImmutableMap.copyOf(nodeRolesByDimension);
      return this;
    }

    public MockSpecifierContext build() {
      return new MockSpecifierContext(this);
    }
  }

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Map<String, Set<NodeRole>> _nodeRolesByDimension;

  private MockSpecifierContext(Builder builder) {
    _configs = builder._configs;
    _nodeRolesByDimension = builder._nodeRolesByDimension;
  }

  @Override
  @Nonnull
  public Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Override
  @Nonnull
  public Set<NodeRole> getNodeRolesByDimension(String dimension) {
    return _nodeRolesByDimension.get(dimension);
  }
}
