package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;

public class NodeNameRegexVrfLocationSpecifier implements LocationSpecifier {
  private final Pattern _nodeNamePattern;

  public NodeNameRegexVrfLocationSpecifier(Pattern pattern) {
    _nodeNamePattern = pattern;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .values()
        .stream()
        .filter(config -> _nodeNamePattern.matcher(config.getHostname()).matches())
        .flatMap(
            config ->
                config
                    .getVrfs()
                    .keySet()
                    .stream()
                    .map(vrfName -> new VrfLocation(config.getHostname(), vrfName)))
        .collect(ImmutableSet.toImmutableSet());
  }
}
