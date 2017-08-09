package org.batfish.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.RoleEdge;
import org.batfish.datamodel.Topology;

public class InferRoles implements Callable<NodeRoleSpecifier> {

  private IBatfish _batfish;
  private Map<String,Configuration> _configurations;
  private List<String> _nodes;

  // the node name that is used to infer a regex
  private String _chosenNode;
  // a tokenized version of _chosenNode
  private List<Pair<String,InferRolesCharClass>> _tokens;
  // the regex produced by generalizing from _tokens
  private List<String> _regex;
  // the list of nodes that match _regex
  private List<String> _matchingNodes;

  public InferRoles(List<String> nodes, Map<String, Configuration> configurations,
      IBatfish batfish) {
    _nodes = nodes;
    _configurations = configurations;
    _batfish = batfish;
  }

  // Node names are parsed into a sequence of alphanumeric strings and
  // delimiters (strings of non-alphanumeric characters).
  public enum InferRolesCharClass {
    ALPHANUMERIC,
    DELIMITER;

    public static InferRolesCharClass charToCharClass(char c) {
      if (Character.isAlphabetic(c) || Character.isDigit(c)) {
        return InferRolesCharClass.ALPHANUMERIC;
      } else {
        return InferRolesCharClass.DELIMITER;
      }
    }

    public String tokenToRegex(String s) {
      switch (this) {
      case ALPHANUMERIC:
        return "\\p{Alnum}+";
      case DELIMITER:
        return Pattern.quote(s);
      default:
        throw new BatfishException("this case should be unreachable");
      }
    }
  }

  @Override
  public NodeRoleSpecifier call() {

    NodeRoleSpecifier result = new NodeRoleSpecifier();

    int allNodesCount = _nodes.size();

    if (allNodesCount == 0) {
      return result;
    }

    boolean commonRegexFound = inferCommonRegex(_nodes);

    if (!commonRegexFound) {
      return result;
    }


    List<String> candidateRegexes = possibleRoleGroups();

    int numCands = candidateRegexes.size();
    if (numCands == 0) {
      return result;
    } else {
      // choose the role group with the maximal "role score"
      String roleRegex = candidateRegexes.get(0);
      double maxRoleScore = computeRoleScore(roleRegex);
      for (int i = 1; i < candidateRegexes.size(); i++) {
        String cand = candidateRegexes.get(i);
        double roleScore = computeRoleScore(cand);
        if (roleScore > maxRoleScore) {
          maxRoleScore = roleScore;
          roleRegex = cand;
        }
      }
      NodeRoleSpecifier roleSpecifier = regexToRoleSpecifier(roleRegex);
      return roleSpecifier;
    }
  }

  // try to identify a regex that most node names match
  private boolean inferCommonRegex(List<String> nodes) {
    for (int attempts = 0 ; attempts < 10; attempts++) {
      // pick a random node name, in order to find one with a common pattern
      _chosenNode = nodes.get(new Random().nextInt(nodes.size()));
      _tokens = tokenizeName(_chosenNode);
      _regex = _tokens
          .stream()
          .map((p) -> p.getSecond().tokenToRegex(p.getFirst()))
          .collect(Collectors.toList());
      Pattern p = Pattern.compile(String.join("", _regex));
      _matchingNodes = nodes
          .stream()
          .filter((node) -> p.matcher(node).matches())
          .collect(Collectors.toList());
      // keep this regex if it matches more than half of the node names; otherwise try again
      if ((double) _matchingNodes.size() / nodes.size() >= 0.5) {
        return true;
      }
    }
    return false;
  }

  // a very simple lexer that tokenizes a name into a sequence of
  // alphanumeric and non-alphanumeric strings
  private List<Pair<String,InferRolesCharClass>> tokenizeName(String name) {
    List<Pair<String,InferRolesCharClass>> pattern = new ArrayList<>();
    char c = name.charAt(0);
    InferRolesCharClass cc = InferRolesCharClass.charToCharClass(c);
    StringBuffer curr = new StringBuffer();
    curr.append(c);
    for (int i = 1; i < name.length(); i++) {
      c = name.charAt(i);
      InferRolesCharClass currClass = InferRolesCharClass.charToCharClass(c);
      if (currClass == cc) {
        curr.append(c);
      } else {
        pattern.add(new Pair<>(new String(curr), cc));
        curr = new StringBuffer();
        cc = currClass;
        curr.append(c);
      }
    }
    pattern.add(new Pair<>(new String(curr), cc));
    return pattern;
  }


  // If for every node name matching the identified regex,
  // a particular alphanumeric token starts with one or more alphabetic characters,
  // the string of initial alphabetic characters is considered a candidate for the role name.
  // This method returns all such candidates, each represented as a regex
  // with a single group indicating the role name.
  private List<String> possibleRoleGroups() {
    List<String> candidateRegexes = new ArrayList<>();
    for (int i = 0; i < _tokens.size(); i++) {
      if (_tokens.get(i).getSecond() == InferRolesCharClass.DELIMITER) {
        continue;
      }
      List<String> regexCopy = new ArrayList<>(_regex);
      regexCopy.set(i, "(\\p{Alpha}+)\\p{Alnum}*");
      Pattern newp = Pattern.compile(String.join("", regexCopy));
      boolean matchesAll = true;
      for (String node : _matchingNodes) {
        Matcher newm = newp.matcher(node);
        if (!newm.matches()) {
          matchesAll = false;
          break;
        }
      }
      if (matchesAll) {
        candidateRegexes.add(String.join("", regexCopy));
      }
    }
    return candidateRegexes;
  }


  // A regex's role score is computed by obtaining role-level edges under the assumption
  // that the regex's group indeed represents each node's role, and then taking the negation
  // of the average degree of each node in this role-level graph.  Intuitively, the "real"
  // roles will have a smaller average degree, since the real roles will impose some
  // topological structure on the graph.
  private double computeRoleScore(String regex) {

    Topology topology = _batfish.computeTopology(_configurations);

    // produce a role-level topology
    SortedSet<RoleEdge> roleEdges = new TreeSet<>();
    Pattern p = Pattern.compile(regex);
    for (Edge e : topology.getEdges()) {
      String n1 = e.getNode1();
      String n2 = e.getNode2();
      Matcher m1 = p.matcher(n1);
      Matcher m2 = p.matcher(n2);
      if (m1.matches() && m2.matches()) {
        try {
          String role1 = m1.group(1);
          String role2 = m2.group(1);
          roleEdges.add(new RoleEdge(role1, role2));
        } catch (IndexOutOfBoundsException exn) {
          throw new BatfishException(
              "Inferred role regex does not contain a group: \"" + p.pattern() + "\"", exn);
        }
      }
    }

    // compute the average degree of each role in the role-level topology,
    // and use its negation as the role score of this candidate regex
    Map<String, Integer> roleDegrees = new HashMap<>();
    for (RoleEdge edge : roleEdges) {
      String role = edge.getRole1();
      roleDegrees.merge(role, 1, (currCount, one) -> currCount + one);
    }
    int numRoles = roleDegrees.size();
    OptionalDouble averageDegree = roleDegrees.values()
        .stream()
        .mapToDouble((i) -> (double) i / numRoles)
        .average();
    if (averageDegree.isPresent()) {
      return - averageDegree.getAsDouble();
    } else {
      return Double.NEGATIVE_INFINITY;
    }
  }

  NodeRoleSpecifier regexToRoleSpecifier(String regex) {
    List<String> regexes = new ArrayList<>();
    regexes.add(regex);
    NodeRoleSpecifier result = new NodeRoleSpecifier();
    result.setRoleRegexes(regexes);
    return result;
  }

}
