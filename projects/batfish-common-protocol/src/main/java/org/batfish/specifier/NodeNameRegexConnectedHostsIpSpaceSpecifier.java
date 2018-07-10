package org.batfish.specifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceSpecifier} that represents the {@link IpSpace} of host subnets (less than 30-bit
 * prefixes) connected to nodes with names matching an input {@link Pattern regex}.
 */
public final class NodeNameRegexConnectedHostsIpSpaceSpecifier implements IpSpaceSpecifier {
  private static final int MAX_PREFIX_LENGTH = 29;

  private final Pattern _pattern;

  NodeNameRegexConnectedHostsIpSpaceSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    Builder<IpSpace> includeBuilder = ImmutableList.builder();
    Builder<IpSpace> excludeBuilder = ImmutableList.builder();
    ctxt.getConfigs()
        .values()
        .stream()
        .filter(node -> _pattern.matcher(node.getName()).matches())
        .flatMap(node -> node.getInterfaces().values().stream())
        .flatMap(iface -> iface.getAllAddresses().stream())
        .filter(ifaceAddr -> ifaceAddr.getPrefix().getPrefixLength() <= MAX_PREFIX_LENGTH)
        .forEach(
            ifaceAddr -> {
              includeBuilder.add(ifaceAddr.getPrefix().toIpSpace());
              excludeBuilder.add(ifaceAddr.getIp().toIpSpace());
            });
    IpSpace include = AclIpSpace.union(includeBuilder.build());
    IpSpace exclude = AclIpSpace.union(excludeBuilder.build());
    IpSpace ipSpace = AclIpSpace.difference(include, exclude);
    return IpSpaceAssignment.builder().assign(locations, ipSpace).build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeNameRegexConnectedHostsIpSpaceSpecifier)) {
      return false;
    }
    NodeNameRegexConnectedHostsIpSpaceSpecifier that =
        (NodeNameRegexConnectedHostsIpSpaceSpecifier) o;
    return Objects.equals(_pattern.pattern(), that._pattern.pattern());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_pattern);
  }
}
