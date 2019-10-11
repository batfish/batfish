package org.batfish.role;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

/**
 * Objects of this class represent a way to map node names to role dimensions and associated role
 * names, via a regular expression.
 */
@ParametersAreNonnullByDefault
public class RoleMapping {

  private static final String PROP_NAME = "name";
  private static final String PROP_REGEX = "regex";
  private static final String PROP_ROLE_DIMENSION_GROUPS = "roleDimensionGroups";
  private static final String PROP_CANONICAL_ROLE_NAMES = "canonicalRoleNames";
  private static final String PROP_CASE_SENSITIVE = "caseSensitive";

  // a name for this mapping
  @Nullable private String _name;
  // the regular expression that induces this role mapping on node names
  @Nonnull private String _regex;
  /* a map from each role dimension name to the list of regex groups
  that signify the role name for that dimension */
  @Nonnull private Map<String, List<Integer>> _roleDimensionGroups;
  /* for each role dimension, a map from the default role name that was
  obtained from the node name to a canonical role name */
  @Nonnull private Map<String, Map<String, String>> _canonicalRoleNames;

  private boolean _caseSensitive;

  @JsonCreator
  public RoleMapping(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_REGEX) String regex,
      @JsonProperty(PROP_ROLE_DIMENSION_GROUPS) Map<String, List<Integer>> roleDimensionGroups,
      @JsonProperty(PROP_CANONICAL_ROLE_NAMES) Map<String, Map<String, String>> canonicalRoleNames,
      @JsonProperty(PROP_CASE_SENSITIVE) boolean caseSensitive) {
    _name = name;
    checkArgument(regex != null, "The regex cannot be null");
    _regex = regex;
    _roleDimensionGroups = firstNonNull(roleDimensionGroups, ImmutableMap.of());
    _canonicalRoleNames = firstNonNull(canonicalRoleNames, ImmutableMap.of());
    _caseSensitive = caseSensitive;
    try {
      Pattern.compile(regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException e) {
      throw new BatfishException("Supplied regex is not a valid Java regex: \"" + regex + "\"", e);
    }
  }

  @JsonProperty(PROP_CANONICAL_ROLE_NAMES)
  @Nonnull
  public Map<String, Map<String, String>> getCanonicalRoleNames() {
    return _canonicalRoleNames;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public Optional<String> getName() {
    return Optional.ofNullable(_name);
  }

  @JsonProperty(PROP_ROLE_DIMENSION_GROUPS)
  @Nonnull
  public Map<String, List<Integer>> getRoleDimensionsGroups() {
    return _roleDimensionGroups;
  }

  @JsonProperty(PROP_REGEX)
  @Nonnull
  public String getRegex() {
    return _regex;
  }

  @JsonProperty(PROP_CASE_SENSITIVE)
  public boolean getCaseSensitive() {
    return _caseSensitive;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleMapping)) {
      return false;
    }
    return Objects.equals(_name, ((RoleMapping) o)._name)
        && Objects.equals(_regex, ((RoleMapping) o)._regex)
        && Objects.equals(_roleDimensionGroups, ((RoleMapping) o)._roleDimensionGroups)
        && Objects.equals(_canonicalRoleNames, ((RoleMapping) o)._canonicalRoleNames)
        && Objects.equals(_caseSensitive, ((RoleMapping) o)._caseSensitive);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _regex, _roleDimensionGroups, _canonicalRoleNames, _caseSensitive);
  }
}
