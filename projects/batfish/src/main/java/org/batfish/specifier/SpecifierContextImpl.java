package org.batfish.specifier;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.role.NodeRole;

public class SpecifierContextImpl implements SpecifierContext {
  private final @Nonnull Batfish _batfish;

  private final @Nonnull Map<String, Configuration> _configs;

  public SpecifierContextImpl(
      @Nonnull Batfish batfish, @Nonnull Map<String, Configuration> configs) {
    _batfish = batfish;
    _configs = configs;
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
}
