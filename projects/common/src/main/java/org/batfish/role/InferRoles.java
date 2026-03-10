package org.batfish.role;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.RoleEdge;
import org.batfish.datamodel.Topology;

public final class InferRoles {
  private static class PreTokenizedString {
    private final @Nonnull String _string;
    private final @Nonnull PreToken _token;

    public PreTokenizedString(@Nonnull String string, @Nonnull PreToken token) {
      _string = string;
      _token = token;
    }

    public @Nonnull String getString() {
      return _string;
    }

    public @Nonnull PreToken getToken() {
      return _token;
    }
  }

  private static class TokenizedString {
    private final @Nonnull String _string;
    private final @Nonnull Token _token;

    public TokenizedString(@Nonnull String string, @Nonnull Token token) {
      _string = string;
      _token = token;
    }

    public @Nonnull String getString() {
      return _string;
    }

    public @Nonnull Token getToken() {
      return _token;
    }
  }

  private static class RegexScore {
    private final List<Integer> _groups;
    private final double _score;

    public RegexScore(List<Integer> groups, double score) {
      _groups = groups;
      _score = score;
    }

    public List<Integer> getGroups() {
      return _groups;
    }

    public double getScore() {
      return _score;
    }
  }

  private final Collection<String> _nodes;
  private final Topology _topology;

  // a tokenized version of _chosenNode
  private List<TokenizedString> _tokens;
  // the regex produced by generalizing from _tokens
  private List<String> _regex;
  // the list of nodes that match _regex
  private List<String> _matchingNodes;

  // the percentage of nodes that must match a regex for it to be used as
  // the base for determining roles
  private static final double REGEX_THRESHOLD = 0.5;

  private static final String ALPHABETIC_REGEX = "[a-zA-Z]";
  private static final String ALPHANUMERIC_REGEX = "[a-zA-Z0-9]";
  private static final String DIGIT_REGEX = "[0-9]";

  public InferRoles(Collection<String> nodes, Topology topology) {
    _nodes = ImmutableSortedSet.copyOf(nodes);
    _topology = topology;
  }

  // A node's name is first parsed into a sequence of simple "pretokens",
  // and then these pretokens are combined to form tokens.
  public enum PreToken {
    ALPHA_PLUS, // sequence of alphabetic characters
    DELIMITER, // sequence of non-alphanumeric characters
    DIGIT_PLUS; // sequence of digits

    public static PreToken charToPreToken(char c) {
      if (Character.isAlphabetic(c)) {
        return ALPHA_PLUS;
      } else if (Character.isDigit(c)) {
        return DIGIT_PLUS;
      } else {
        return DELIMITER;
      }
    }
  }

  public enum Token {
    ALPHA_PLUS,
    ALPHA_PLUS_DIGIT_PLUS,
    ALNUM_PLUS,
    DELIMITER,
    DIGIT_PLUS;

    public String tokenToRegex(String s) {
      return switch (this) {
        case ALPHA_PLUS -> plus(ALPHABETIC_REGEX);
        case ALPHA_PLUS_DIGIT_PLUS -> plus(ALPHABETIC_REGEX) + plus(DIGIT_REGEX);
        case ALNUM_PLUS -> plus(ALPHANUMERIC_REGEX);
        case DELIMITER -> Pattern.quote(s);
        case DIGIT_PLUS -> plus(DIGIT_REGEX);
      };
    }
  }

  // some useful operations on regexes
  private static String plus(String s) {
    return s + "+";
  }

  private static String star(String s) {
    return s + "*";
  }

  private static String group(String s) {
    return "(" + s + ")";
  }

  public Optional<RoleMapping> inferRoles() {

    if (_nodes.isEmpty()) {
      return Optional.empty();
    }

    boolean commonRegexFound = inferCommonRegex(_nodes);

    if (!commonRegexFound) {
      return Optional.empty();
    }

    // find the possible candidates that have a single role group
    int groups = possibleRoleGroups();

    if (groups == 0) {
      return Optional.empty();
    }

    // if there is at least one role group, let's find the best role dimension according
    // to our metric, and also keep all the others.

    Map<String, List<Integer>> roleDimensionGroups = new HashMap<>();

    List<List<Integer>> allSingleGroupDimensions =
        IntStream.rangeClosed(1, groups).mapToObj(ImmutableList::of).collect(Collectors.toList());

    RegexScore bestRegexAndScore = findBestRegex(allSingleGroupDimensions);

    // give names to each of the role dimensions
    // heuristically treat the dimension with the highest score as the primary dimension
    roleDimensionGroups.put(
        NodeRoleDimension.AUTO_DIMENSION_PRIMARY, bestRegexAndScore.getGroups());
    allSingleGroupDimensions.remove(bestRegexAndScore.getGroups());
    roleDimensionGroups.putAll(createRoleDimensionGroups(allSingleGroupDimensions));
    return Optional.of(toRoleMapping(roleDimensionGroups));
  }

  // try to identify a regex that most node names match
  private boolean inferCommonRegex(Collection<String> nodes) {
    for (int attempts = 0; attempts < 10; attempts++) {
      // pick a random node name, in order to find one with a common pattern
      // the node name that is used to infer a regex
      String chosenNode = Iterables.get(nodes, new Random().nextInt(nodes.size()));
      _tokens = tokenizeName(chosenNode);
      _regex =
          _tokens.stream()
              .map((p) -> p.getToken().tokenToRegex(p.getString()))
              .collect(Collectors.toList());
      Pattern p = Pattern.compile(String.join("", _regex));
      _matchingNodes =
          nodes.stream().filter((node) -> p.matcher(node).matches()).collect(Collectors.toList());
      // keep this regex if it matches a sufficient fraction of node names; otherwise try again
      if ((double) _matchingNodes.size() / nodes.size() >= REGEX_THRESHOLD) {
        return true;
      }
    }
    return false;
  }

  // If delimiters (non-alphanumeric characters) are being used in the node names, we use them
  // to separate the different tokens.
  private static List<TokenizedString> preTokensToDelimitedTokens(
      List<PreTokenizedString> pretokens) {
    List<TokenizedString> tokens = new ArrayList<>();
    int size = pretokens.size();
    int i = 0;
    while (i < size) {
      StringBuilder chars = new StringBuilder(pretokens.get(i).getString());
      PreToken pt = pretokens.get(i).getToken();
      switch (pt) {
        case ALPHA_PLUS:
          // combine everything up to the next delimiter into a single alphanumeric token
          int next = i + 1;
          while (next < size && pretokens.get(next).getToken() != PreToken.DELIMITER) {
            chars.append(pretokens.get(next).getString());
            next++;
          }
          i = next - 1;
          tokens.add(new TokenizedString(chars.toString(), Token.ALNUM_PLUS));
          break;
        case DELIMITER:
          tokens.add(new TokenizedString(chars.toString(), Token.DELIMITER));
          break;
        case DIGIT_PLUS:
          tokens.add(new TokenizedString(chars.toString(), Token.DIGIT_PLUS));
          break;
      }
      i++;
    }
    return tokens;
  }

  // If delimiters (non-alphanumeric characters) are not being used in the node names, we treat
  // each consecutive string matching alpha+digit+ as a distinct token.
  private static List<TokenizedString> preTokensToUndelimitedTokens(
      List<PreTokenizedString> pretokens) {
    List<TokenizedString> tokens = new ArrayList<>();
    int size = pretokens.size();
    int i = 0;
    while (i < size) {
      String chars = pretokens.get(i).getString();
      PreToken pt = pretokens.get(i).getToken();
      switch (pt) {
        case ALPHA_PLUS:
          int next = i + 1;
          if (next >= size) {
            tokens.add(new TokenizedString(chars, Token.ALPHA_PLUS));
          } else {
            // the next token must be DIGIT_PLUS since we know there are no delimiters
            String bothChars = chars + pretokens.get(next).getString();
            tokens.add(new TokenizedString(bothChars, Token.ALPHA_PLUS_DIGIT_PLUS));
            i++;
          }
          break;
        case DIGIT_PLUS:
          tokens.add(new TokenizedString(chars, Token.DIGIT_PLUS));
          break;
        default:
          throw new BatfishException("Unexpected pretoken " + pt);
      }
      i++;
    }
    return tokens;
  }

  private static List<TokenizedString> tokenizeName(String name) {
    List<PreTokenizedString> pretokens = pretokenizeName(name);
    if (pretokens.stream().anyMatch((p) -> p.getToken() == PreToken.DELIMITER)) {
      return preTokensToDelimitedTokens(pretokens);
    } else {
      return preTokensToUndelimitedTokens(pretokens);
    }
  }

  // tokenizes a name into a sequence of pretokens defined by the PreToken enum above
  private static List<PreTokenizedString> pretokenizeName(String name) {
    List<PreTokenizedString> pattern = new ArrayList<>();
    char c = name.charAt(0);
    PreToken currPT = PreToken.charToPreToken(c);
    StringBuffer curr = new StringBuffer();
    curr.append(c);
    for (int i = 1; i < name.length(); i++) {
      c = name.charAt(i);
      PreToken newPT = PreToken.charToPreToken(c);
      if (newPT != currPT) {
        pattern.add(new PreTokenizedString(new String(curr), currPT));
        curr = new StringBuffer();
        currPT = newPT;
      }
      curr.append(c);
    }
    pattern.add(new PreTokenizedString(new String(curr), currPT));
    return pattern;
  }

  private static String regexTokensToRegex(List<String> tokens) {
    return String.join("", tokens);
  }

  /*
   Create a regex group for each token that starts with an alphabetic character, so we will
   consider the group as indicative of a useful node role dimension.
   This method updates the _regex that we maintain and returns the number of groups.
  */
  private int possibleRoleGroups() {
    int groups = 0;
    for (int i = 0; i < _tokens.size(); i++) {
      switch (_tokens.get(i).getToken()) {
        case ALNUM_PLUS:
          _regex.set(i, group(plus(ALPHABETIC_REGEX)) + star(ALPHANUMERIC_REGEX));
          groups++;
          break;
        case ALPHA_PLUS_DIGIT_PLUS:
          _regex.set(i, group(plus(ALPHABETIC_REGEX)) + plus(DIGIT_REGEX));
          groups++;
          break;
        default:
          break;
      }
    }
    return groups;
  }

  private Map<String, List<Integer>> createRoleDimensionGroups(List<List<Integer>> groups) {
    Map<String, List<Integer>> result = new HashMap<>();
    int i = 0;
    for (List<Integer> group : groups) {
      String dimName = NodeRoleDimension.AUTO_DIMENSION_PREFIX + (i + 1);
      result.put(dimName, group);
      i++;
    }
    return result;
  }

  private double computeRoleScore(String regex, List<Integer> groups) {

    SortedMap<String, SortedSet<String>> nodeRolesMap = regexToNodeRolesMap(regex, groups, _nodes);

    // produce a role-level topology and the list of nodes in each edge's source role
    // that have an edge to some node in the edge's target role
    SortedMap<RoleEdge, SortedSet<String>> roleEdges = new TreeMap<>();
    for (Edge e : _topology.getEdges()) {
      String n1 = e.getNode1();
      String n2 = e.getNode2();
      SortedSet<String> roles1 = nodeRolesMap.get(n1);
      SortedSet<String> roles2 = nodeRolesMap.get(n2);
      if (roles1 != null && roles2 != null && roles1.size() == 1 && roles2.size() == 1) {
        String role1 = roles1.first();
        String role2 = roles2.first();
        // ignore self-edges
        if (role1.equals(role2)) {
          continue;
        }
        RoleEdge redge = new RoleEdge(role1, role2);
        SortedSet<String> roleEdgeNodes = roleEdges.getOrDefault(redge, new TreeSet<>());
        roleEdgeNodes.add(n1);
        roleEdges.put(redge, roleEdgeNodes);
      }
    }

    int numEdges = roleEdges.size();
    if (numEdges == 0) {
      return 0.0;
    }

    // compute the "support" of each edge in the role-level topology:
    // the percentage of nodes playing the source role that have an edge
    // to a node in the target role.
    // the score of this regex is then the average support across all role edges
    SortedMap<String, SortedSet<String>> roleNodesMap = regexToRoleNodesMap(regex, groups, _nodes);

    double supportSum = 0.0;
    for (Map.Entry<RoleEdge, SortedSet<String>> roleEdgeCount : roleEdges.entrySet()) {
      RoleEdge redge = roleEdgeCount.getKey();
      int count = roleEdgeCount.getValue().size();
      supportSum += (double) count / roleNodesMap.get(redge.getRole1()).size();
    }
    return supportSum / numEdges;
  }

  private SortedMap<String, SortedSet<String>> regexToRoleNodesMap(
      String regex, List<Integer> groups, Collection<String> nodes) {
    SortedMap<String, SortedSet<String>> roleNodesMap = new TreeMap<>();
    Pattern pattern;
    try {
      pattern = Pattern.compile(regex);
    } catch (PatternSyntaxException e) {
      throw new BatfishException("Supplied regex is not a valid Java regex: \"" + regex + "\"", e);
    }
    for (String node : nodes) {
      Matcher matcher = pattern.matcher(node);
      if (matcher.matches()) {
        try {
          List<String> roleParts = groups.stream().map(matcher::group).collect(Collectors.toList());
          String role = String.join("-", roleParts);
          SortedSet<String> currNodes = roleNodesMap.computeIfAbsent(role, k -> new TreeSet<>());
          currNodes.add(node);
        } catch (IndexOutOfBoundsException e) {
          throw new BatfishException(
              "Supplied regex does not contain enough groups: \"" + pattern.pattern() + "\"", e);
        }
      }
    }
    return roleNodesMap;
  }

  // return a map from each node name to the set of roles that it plays
  private SortedMap<String, SortedSet<String>> regexToNodeRolesMap(
      String regex, List<Integer> groups, Collection<String> allNodes) {

    SortedMap<String, SortedSet<String>> roleNodesMap =
        regexToRoleNodesMap(regex, groups, allNodes);

    // invert the map from roles to nodes, to create a map from nodes to roles
    SortedMap<String, SortedSet<String>> nodeRolesMap = new TreeMap<>();

    roleNodesMap.forEach(
        (role, nodes) -> {
          for (String node : nodes) {
            SortedSet<String> nodeRoles = nodeRolesMap.computeIfAbsent(node, k -> new TreeSet<>());
            nodeRoles.add(role);
          }
        });

    return nodeRolesMap;
  }

  private RegexScore findBestRegex(List<List<Integer>> groups) {
    String regex = regexTokensToRegex(_regex);
    // choose the candidate role regex with the maximal "role score"
    return groups.stream()
        .map(g -> new RegexScore(g, computeRoleScore(regex, g)))
        .max(Comparator.comparingDouble(RegexScore::getScore))
        .orElseThrow(() -> new BatfishException("this exception should not be reachable"));
  }

  private RoleMapping toRoleMapping(Map<String, List<Integer>> dimensionGroups) {
    return new RoleMapping(
        null, regexTokensToRegex(_regex), ImmutableMap.copyOf(dimensionGroups), null);
  }
}
