package org.batfish.role;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

/**
 * Objects of this class represent a way to map node names to role names for a particular role
 * dimension, via a regular expression.
 *
 * <p>Deprecated in favor of {@link RoleMapping}.
 */
@ParametersAreNonnullByDefault
public class RoleDimensionMapping implements Comparable<RoleDimensionMapping> {

  private static final String PROP_REGEX = "regex";
  private static final String PROP_GROUPS = "groups";
  private static final String PROP_CANONICAL_ROLE_NAMES = "canonicalRoleNames";
  private static final String PROP_CASE_SENSITIVE = "caseSensitive";

  // the regular expression that induces this role mapping on node names
  @Nonnull private String _regex;
  @Nonnull private List<Integer> _groups;
  /* this map is used to convert the default role name that was obtained from the node name, via
  the regex and groups above, to a canonical role name.  the keys are allowed to be arbitrary Java
  regexes, and any default role name matching the regex is mapped to the corresponding string value
  as its canonical role name.  we allow regexes in order to also make it easy to convert the old
  node roles format, based on the NodeRole class, to this new format.
   */
  @Nonnull private Map<String, String> _canonicalRoleNames;

  @Nonnull private boolean _caseSensitive;

  @Nonnull private Pattern _pattern;

  private int _patternFlags;

  @JsonCreator
  public RoleDimensionMapping(
      @JsonProperty(PROP_REGEX) String regex,
      @JsonProperty(PROP_GROUPS) @Nullable List<Integer> groups,
      @JsonProperty(PROP_CANONICAL_ROLE_NAMES) @Nullable Map<String, String> canonicalRoleNames,
      @JsonProperty(PROP_CASE_SENSITIVE) boolean caseSensitive) {
    checkArgument(regex != null, "The regex cannot be null");
    _regex = regex;
    _patternFlags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
    try {
      _pattern = Pattern.compile(regex, _patternFlags);
    } catch (PatternSyntaxException e) {
      throw new BatfishException("Supplied regex is not a valid Java regex: \"" + regex + "\"", e);
    }
    _groups =
        firstNonNull(
            groups,
            IntStream.rangeClosed(1, _pattern.matcher("").groupCount())
                .boxed()
                .collect(Collectors.toList()));
    _canonicalRoleNames = firstNonNull(canonicalRoleNames, ImmutableMap.of());
    _caseSensitive = caseSensitive;
  }

  public RoleDimensionMapping(String regex) {
    this(regex, null, null, false);
  }

  /**
   * To ease backward compatibility with an old format for roles, convert a NodeRole into a role
   * dimension mapping.
   *
   * @param role the node role
   */
  public RoleDimensionMapping(NodeRole role) {
    this(
        "(" + role.getRegex() + ")",
        ImmutableList.of(1),
        ImmutableMap.of(role.getRegex(), role.getName()),
        role.getCaseSensitive());
  }

  @Override
  public int compareTo(RoleDimensionMapping o) {
    return Comparator.comparing(RoleDimensionMapping::getRegex)
        .thenComparing(
            RoleDimensionMapping::getGroups,
            Comparator.nullsFirst(Comparators.lexicographical(Integer::compareTo)))
        .thenComparing(
            mapping -> mapping.getCanonicalRoleNames().entrySet(),
            Comparators.lexicographical(
                (e1, e2) -> {
                  int cKey = e1.getKey().compareTo(e2.getKey());
                  return cKey != 0 ? cKey : e1.getValue().compareTo(e2.getValue());
                }))
        .thenComparing(RoleDimensionMapping::getCaseSensitive)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleDimensionMapping)) {
      return false;
    }
    return Objects.equals(_regex, ((RoleDimensionMapping) o)._regex)
        && Objects.equals(_groups, ((RoleDimensionMapping) o)._groups)
        && Objects.equals(_canonicalRoleNames, ((RoleDimensionMapping) o)._canonicalRoleNames)
        && _caseSensitive == ((RoleDimensionMapping) o)._caseSensitive;
  }

  @JsonProperty(PROP_CANONICAL_ROLE_NAMES)
  @Nonnull
  public Map<String, String> getCanonicalRoleNames() {
    return _canonicalRoleNames;
  }

  @JsonProperty(PROP_CASE_SENSITIVE)
  public boolean getCaseSensitive() {
    return _caseSensitive;
  }

  @JsonProperty(PROP_GROUPS)
  @Nonnull
  public List<Integer> getGroups() {
    return _groups;
  }

  @JsonProperty(PROP_REGEX)
  @Nonnull
  public String getRegex() {
    return _regex;
  }

  public SortedMap<String, String> createNodeRolesMap(Set<String> nodeNames) {
    SortedMap<String, String> m = new TreeMap<>();
    for (String node : nodeNames) {
      Optional<String> optRoleName = roleNameForNode(node);
      optRoleName.ifPresent(roleName -> m.put(node, roleName));
    }
    return m;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_regex, _groups, _canonicalRoleNames, _caseSensitive);
  }

  private Optional<String> roleNameForNode(String nodeName) {
    Matcher matcher = _pattern.matcher(nodeName);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    List<String> roleNameParts = _groups.stream().map(matcher::group).collect(Collectors.toList());
    String roleName = String.join("-", roleNameParts);
    if (!_caseSensitive) {
      roleName = roleName.toLowerCase();
    }
    // convert to a canonical role name if there is one provided
    for (Map.Entry<String, String> entry : _canonicalRoleNames.entrySet()) {
      String roleRegex = entry.getKey();
      String canonicalRoleName = entry.getValue();
      Pattern p;
      try {
        p = Pattern.compile(roleRegex, _patternFlags);
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            "Supplied regex is not a valid Java regex: \"" + roleRegex + "\"", e);
      }
      if (p.matcher(roleName).matches()) {
        roleName = canonicalRoleName;
        break;
      }
    }
    return Optional.of(roleName);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_REGEX, _regex)
        .add(PROP_GROUPS, _groups)
        .add(PROP_CANONICAL_ROLE_NAMES, _canonicalRoleNames)
        .add(PROP_CASE_SENSITIVE, _caseSensitive)
        .toString();
  }
}
