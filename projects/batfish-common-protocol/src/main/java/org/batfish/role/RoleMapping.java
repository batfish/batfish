package org.batfish.role;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

// Objects of this class represent a way to map node names
// to role dimensions and associated role names, via a regular expression.
@ParametersAreNonnullByDefault
public class RoleMapping {

  private static final String PROP_REGEX = "regex";
  private static final String PROP_ROLE_DIMENSIONS_GROUPS = "roleDimensionsGroups";
  private static final String PROP_CANONICAL_ROLE_NAMES = "canonicalRoleNames";

  // the regular expression that induces this role mapping on node names
  @Nonnull private String _regex;
  // a map from each role dimension name to the list of regex groups
  // that signify the role name for that dimension
  @Nonnull private Map<String, List<Integer>> _roleDimensionsGroups;
  // for each role dimension, a map from the default role name that was
  // obtained from the node name to a canonical role name
  @Nonnull private Map<String, Map<String, String>> _canonicalRoleNames;

  @JsonCreator
  public RoleMapping(
      @JsonProperty(PROP_REGEX) String regex,
      @JsonProperty(PROP_ROLE_DIMENSIONS_GROUPS) Map<String, List<Integer>> roleDimensionsGroups,
      @JsonProperty(PROP_CANONICAL_ROLE_NAMES)
          Map<String, Map<String, String>> canonicalRoleNames) {
    checkArgument(regex != null, "The regex cannot be null");
    _regex = regex;
    _roleDimensionsGroups = firstNonNull(roleDimensionsGroups, ImmutableMap.of());
    _canonicalRoleNames = firstNonNull(canonicalRoleNames, ImmutableMap.of());
    /* TODO: Take a flag for case sensitivity */
    try {
      Pattern.compile(regex);
    } catch (PatternSyntaxException e) {
      throw new BatfishException("Supplied regex is not a valid Java regex: \"" + regex + "\"", e);
    }
  }

  @JsonProperty(PROP_CANONICAL_ROLE_NAMES)
  @Nonnull
  public Map<String, Map<String, String>> getCanonicalRoleNames() {
    return _canonicalRoleNames;
  }

  @JsonProperty(PROP_ROLE_DIMENSIONS_GROUPS)
  @Nonnull
  public Map<String, List<Integer>> getRoleDimensionsGroups() {
    return _roleDimensionsGroups;
  }

  @JsonProperty(PROP_REGEX)
  @Nonnull
  public String getRegex() {
    return _regex;
  }
}
