package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;

/** A {@link LocationSpecifier} specifying VRFs with names matching the input regex. */
public class NameRegexVrfLocationSpecifier implements LocationSpecifier {
  public static final NameRegexVrfLocationSpecifier ALL_VRFS =
      new NameRegexVrfLocationSpecifier(Pattern.compile(".*"));

  private final Pattern _namePattern;

  public NameRegexVrfLocationSpecifier(Pattern namePattern) {
    _namePattern = namePattern;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .values()
        .stream()
        .flatMap(
            config ->
                config
                    .getVrfs()
                    .values()
                    .stream()
                    .filter(vrf -> _namePattern.matcher(vrf.getName()).matches())
                    .map(vrf -> new VrfLocation(config.getName(), vrf.getName())))
        .collect(ImmutableSet.toImmutableSet());
  }
}
