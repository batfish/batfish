package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;

public class NodeNameRegexInterfaceLocationSpecifier implements LocationSpecifier {
  private final Pattern _pattern;

  public NodeNameRegexInterfaceLocationSpecifier(Pattern pattern) {
    _pattern = pattern;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .entrySet()
        .stream()
        .filter(entry -> _pattern.matcher(entry.getKey()).matches())
        .flatMap(
            entry -> {
              String node = entry.getKey();
              return entry
                  .getValue()
                  .getInterfaces()
                  .keySet()
                  .stream()
                  .map(iface -> makeLocation(node, iface));
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  protected Location makeLocation(String node, String iface) {
    return new InterfaceLocation(node, iface);
  }
}
