package org.batfish.specifier;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.role.NodeRole;

public interface SpecifierContext {
  @Nonnull
  Map<String, Configuration> getConfigs();

  @Nonnull
  Set<NodeRole> getNodeRolesByDimension(String dimension);
}
