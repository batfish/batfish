package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;

/**
 * An {@link FilterSpecifier} that yields {@link IpAccessList}s based on a {@code NodeSpecifier}.
 * The {@link NodeSpecifier} helps select the nodes and filters include all filters on those nodes.
 */
@ParametersAreNonnullByDefault
public class NodeSpecifierFilterSpecifier implements FilterSpecifier {
  private NodeSpecifier _nodeSpecifier;

  public NodeSpecifierFilterSpecifier(NodeSpecifier nodeSpecifier) {
    _nodeSpecifier = nodeSpecifier;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeSpecifierFilterSpecifier)) {
      return false;
    }
    return Objects.equals(_nodeSpecifier, ((NodeSpecifierFilterSpecifier) o)._nodeSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeSpecifier);
  }

  @Override
  public Set<IpAccessList> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return Sets.intersection(_nodeSpecifier.resolve(ctxt), nodes).stream()
        .flatMap(n -> ctxt.getConfigs().get(n).getIpAccessLists().values().stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
