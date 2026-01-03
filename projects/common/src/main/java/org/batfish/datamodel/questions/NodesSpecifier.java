package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.SpecifierContext;

/**
 * Enables specification of groups of nodes in various questions.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>lhr-.* -&gt; all nodes with matching names (old style)
 *   <li>name:lhr-.* -&gt; same as above; name: is optional
 *   <li>role:auto0:a1.* -&gt; all nodes with roles that match a1.* in role dimension auto0
 * </ul>
 *
 * <p>In the future, we might need other tags (e.g., loc:) and boolean expressions (e.g.,
 * role:auto1.a1 AND lhr-* for all servers with matching names)
 */
public class NodesSpecifier {

  public enum Type {
    NAME,
    ROLE
  }

  public static final NodesSpecifier ALL = new NodesSpecifier(".*");

  public static final NodesSpecifier NONE = new NodesSpecifier("");

  private final @Nonnull String _expression;

  private final @Nonnull Pattern _regex;

  private final @Nullable String _roleDimension;

  private final @Nonnull Type _type;

  private final boolean _caseSensitive;

  public NodesSpecifier(String expression, boolean caseSensitive) {
    _expression = expression;
    _caseSensitive = caseSensitive;

    String[] parts = expression.split(":");

    int patternFlags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;

    if (parts.length == 1) {
      _type = Type.NAME;
      _regex = Pattern.compile(_expression, patternFlags);
      _roleDimension = null;
    } else {
      try {
        _type = Type.valueOf(parts[0].toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format(
                "Illegal NodesSpecifier filter %s. Should be one of %s",
                parts[0],
                Arrays.stream(Type.values())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "))));
      }
      if (parts.length == 2 && _type == Type.NAME) {
        _regex = Pattern.compile(parts[1], patternFlags);
        _roleDimension = null;
      } else if (parts.length == 3 && _type == Type.ROLE) {
        _roleDimension = parts[1];
        _regex = Pattern.compile(parts[2], patternFlags);
      } else {
        throw new IllegalArgumentException("Cannot parse NodeSpecifier " + expression);
      }
    }
  }

  @JsonCreator
  public NodesSpecifier(String expression) {
    this(expression, false);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NodesSpecifier)) {
      return false;
    }
    NodesSpecifier rhs = (NodesSpecifier) obj;
    return Objects.equals(_expression, rhs._expression)
        && Objects.equals(_regex.pattern(), rhs._regex.pattern())
        && Objects.equals(_type, rhs._type)
        && _caseSensitive == rhs._caseSensitive;
  }

  /**
   * Produces a list of suggestions based on the query (prefix string).
   *
   * <p>What is produced for various queries:
   *
   * <ul>
   *   <li>na -&gt; all {@link Type}s whose names begin with srv and nodes whose name begins with
   *       srv
   *   <li>NAME:srv -&gt; all nodes whose names with srv
   *   <li>ROLE:d all role dimensions that begin with r
   *   <li>ROLE:dim:r all roles in dimension 'dim' that begin with r
   * </ul>
   *
   * @param query The prefix substring
   * @param nodes Set of node names
   * @param nodeRoleData {@link NodeRolesData} object
   * @return A list of {@link AutocompleteSuggestion} objects
   */
  public static List<AutocompleteSuggestion> autoComplete(
      String query, Set<String> nodes, NodeRolesData nodeRoleData) {
    final String finalQuery = query == null ? "" : query;

    // fill in types; will produce something only with finalQuery is empty or something like "NA"
    List<AutocompleteSuggestion> suggestions =
        Arrays.stream(Type.values())
            .filter(type -> type.toString().startsWith(finalQuery.toUpperCase()))
            .map(
                type ->
                    new AutocompleteSuggestion(
                        type.toString() + ":", true, "Select nodes by " + type.toString()))
            .collect(Collectors.toList());

    // for reading code below, remember splitting "PRE:" or "PRE" yields ["PRE"]
    String[] parts = finalQuery.split(":");

    if (!finalQuery.contains(":") || finalQuery.toUpperCase().startsWith("NAME:")) { // NAME query
      String namePrefix =
          finalQuery.equalsIgnoreCase("NAME:") ? "" : (parts.length == 1 ? finalQuery : parts[1]);
      List<AutocompleteSuggestion> nameSuggestions =
          nodes.stream()
              .filter(n -> n.startsWith(namePrefix))
              .map(n -> new AutocompleteSuggestion(n, false))
              .collect(Collectors.toList());
      if (!nameSuggestions.isEmpty()) {
        suggestions.add(
            new AutocompleteSuggestion(
                finalQuery + ".*", false, "All nodes whose names match " + namePrefix + ".*"));
        suggestions.addAll(nameSuggestions);
      }
    }

    if (finalQuery.toUpperCase().startsWith("ROLE:") && nodeRoleData != null) { // ROLE query
      if (parts.length == 3 || (parts.length == 2 && finalQuery.endsWith(":"))) {
        String roleDimension = parts[1];
        Optional<NodeRoleDimension> optDimension =
            Optional.ofNullable(nodeRoleData.toNodeRoleDimensions().get(roleDimension));
        if (optDimension.isPresent()) {
          NodeRoleDimension dimension = optDimension.get();
          String roleDimensionPrefix = "ROLE:" + dimension.getName() + ":";
          String rolePrefix = finalQuery.endsWith(":") ? "" : parts[2];
          List<AutocompleteSuggestion> roleSuggestions =
              dimension.roleNamesFor(nodes).stream()
                  .filter(role -> role.startsWith(rolePrefix))
                  .map(
                      role ->
                          new AutocompleteSuggestion(
                              "ROLE:" + dimension.getName() + ":" + role,
                              false,
                              "All nodes that belong to this role"))
                  .collect(Collectors.toList());
          if (!roleSuggestions.isEmpty()) {
            suggestions.add(
                new AutocompleteSuggestion(
                    roleDimensionPrefix + rolePrefix + ".*",
                    false,
                    "All nodes with matching roles"));
            suggestions.addAll(roleSuggestions);
          }
        }
      } else {
        String roleDimPrefix = finalQuery.equalsIgnoreCase("ROLE:") ? "" : parts[1];
        suggestions.addAll(
            nodeRoleData.toNodeRoleDimensions().values().stream()
                .filter(dim -> dim.getName().startsWith(roleDimPrefix))
                .map(
                    dim ->
                        new AutocompleteSuggestion(
                            "ROLE:" + dim.getName() + ":",
                            true,
                            "Select nodes using this dimension"))
                .collect(ImmutableSet.toImmutableSet()));
      }
    }

    int rank = 0;
    for (AutocompleteSuggestion suggestion : suggestions) {
      AutocompleteSuggestion.builder(suggestion).setRank(rank).build();
      rank++;
    }

    return suggestions;
  }

  /**
   * @return The input {@link String} expression.
   */
  public String getExpression() {
    return _expression;
  }

  /** Return the set of nodes that match this specifier */
  public Set<String> getMatchingNodes(IBatfish batfish, NetworkSnapshot snapshot) {
    return getMatchingNodes(batfish.specifierContext(snapshot));
  }

  /** Return the set of nodes that match this specifier. */
  @JsonIgnore
  public Set<String> getMatchingNodes(SpecifierContext ctxt) {
    return switch (_type) {
      case NAME -> getMatchingNodesByName(ctxt.getConfigs().keySet());
      case ROLE ->
          ctxt.getNodeRoleDimension(_roleDimension)
              .map(rd -> getMatchingNodesByRole(rd, ctxt.getConfigs().keySet()))
              .orElse(ImmutableSet.of());
    };
  }

  public Set<String> getMatchingNodesByName(Set<String> nodes) {
    return nodes.stream()
        .filter(n -> _regex.matcher(n).matches())
        .collect(ImmutableSet.toImmutableSet());
  }

  public Set<String> getMatchingNodesByRole(NodeRoleDimension roleDimension, Set<String> nodes) {
    return nodes.stream()
        .filter(n -> nodeNameInMatchingRole(n, roleDimension))
        .collect(ImmutableSet.toImmutableSet());
  }

  @JsonIgnore
  public Pattern getRegex() {
    return _regex;
  }

  @JsonIgnore
  public String getRoleDimension() {
    return _roleDimension;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @JsonIgnore
  public boolean getCaseSensitive() {
    return _caseSensitive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expression, _regex.pattern(), _type.ordinal());
  }

  /** Does this nodeName match any of roles that match our _regex? */
  private boolean nodeNameInMatchingRole(String nodeName, NodeRoleDimension roleDimension) {
    for (String role : roleDimension.roleNamesFor(ImmutableSortedSet.of(nodeName))) {
      if (_regex.matcher(role).matches()) {
        return true;
      }
    }
    return false;
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
