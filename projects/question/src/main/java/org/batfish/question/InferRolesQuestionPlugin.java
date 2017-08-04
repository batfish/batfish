package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.RoleEdge;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.NeighborsQuestionPlugin.EdgeStyle;
import org.batfish.question.NeighborsQuestionPlugin.NeighborsAnswerElement;
import org.batfish.question.NeighborsQuestionPlugin.NeighborsQuestion;

public class InferRolesQuestionPlugin extends QuestionPlugin {

  public static class InferRolesAnswerElement implements AnswerElement {

    private static final String ROLE_SPECIFIER_VAR = "roleSpecifier";

    private static final String ALL_NODES_VAR = "allNodes";

    private static final String ALL_NODES_COUNT_VAR = "allNodesCount";

    private static final String MATCHING_NODES_COUNT_VAR = "matchingNodesCount";

    private NodeRoleSpecifier _roleSpecifier;

    private List<String> _allNodes;

    private int _allNodesCount;

    private int _matchingNodesCount;

    public InferRolesAnswerElement() {
    }

    @JsonProperty(ROLE_SPECIFIER_VAR)
    public NodeRoleSpecifier getRoleSpecifier() {
      return _roleSpecifier;
    }

    @Override
    public String prettyPrint() {

      StringBuilder sb;
      sb = new StringBuilder(
          "Results for infer roles\n");

      if (_roleSpecifier == null) {
        return sb.toString();
      }

      for (String regex : _roleSpecifier.getRoleRegexes()) {
        sb.append("Role regex inferred:  " + regex + "\n");
        sb.append("Matches " + _matchingNodesCount + " out of "
            + _allNodesCount + " nodes\n");
      }

      SortedMap<String, SortedSet<String>> roleNodesMap =
          _roleSpecifier.createRoleNodesMap(new TreeSet<String>(_allNodes));

      sb.append("Roles inferred:\n");
      for (Map.Entry<String, SortedSet<String>> entry : roleNodesMap.entrySet()) {
        sb.append("  " + entry + "\n");
      }

      return sb.toString();
    }

    @JsonProperty(ROLE_SPECIFIER_VAR)
    public void setRoleSpecifier(NodeRoleSpecifier roleSpecifier) {
      _roleSpecifier = roleSpecifier;
    }

    @JsonProperty(ALL_NODES_VAR)
    public void setAllNodes(List<String> allNodes) {
      _allNodes = allNodes;
    }

    @JsonProperty(ALL_NODES_COUNT_VAR)
    public void setAllNodesCount(int allNodesCount) {
      _allNodesCount = allNodesCount;
    }


    @JsonProperty(MATCHING_NODES_COUNT_VAR)
    public void setMatchingNodesCount(int matchingNodesCount) {
      _matchingNodesCount = matchingNodesCount;
    }
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

  public static class InferRolesAnswerer extends Answerer {

    // the node name that is used to infer a regex
    String _chosenNode;
    // a tokenized version of _chosenNode
    List<Pair<String,InferRolesCharClass>> _tokens;
    // the regex produced by generalizing from _tokens
    List<String> _regex;
    // the list of nodes that match _regex
    List<String> _matchingNodes;

    public InferRolesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public InferRolesAnswerElement answer() {

      InferRolesQuestion question = (InferRolesQuestion) _question;
      InferRolesAnswerElement answerElement = new InferRolesAnswerElement();

      Map<String,Configuration> configurations = _batfish.loadConfigurations();
      // collect relevant nodes in a list.
      List<String> nodes = CommonUtil.getMatchingStrings(
          question.getNodeRegex(),
          configurations.keySet());

      int allNodesCount = nodes.size();

      answerElement.setAllNodes(nodes);
      answerElement.setAllNodesCount(allNodesCount);

      if (allNodesCount == 0) {
        return answerElement;
      }

      boolean commonRegexFound = inferCommonRegex(nodes);

      if (!commonRegexFound) {
        return answerElement;
      }


      List<String> candidateRegexes = possibleRoleGroups();

      int numCands = candidateRegexes.size();
      if (numCands == 0) {
        return answerElement;
      } else {
        // choose the role group with the maximal "role score"
        String roleRegex = null;
        double maxRoleScore = Double.NEGATIVE_INFINITY;
        for (String cand : candidateRegexes) {
          double roleScore = computeRoleScore(cand);
          if (roleScore > maxRoleScore) {
            maxRoleScore = roleScore;
            roleRegex = cand;
          }
        }
        NodeRoleSpecifier roleSpecifier = regexToRoleSpecifier(roleRegex);
        answerElement.setRoleSpecifier(roleSpecifier);
        answerElement.setMatchingNodesCount(_matchingNodes.size());
        return answerElement;
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
    double computeRoleScore(String regex) {
      InferRolesQuestion irq = (InferRolesQuestion) _question;
      NeighborsQuestion nq = new NeighborsQuestion();
      nq.setNode1Regex(irq.getNodeRegex());
      nq.setNode2Regex(irq.getNodeRegex());
      nq.setStyle(EdgeStyle.ROLE);
      nq.setRoleSpecifier(regexToRoleSpecifier(regex));

      NeighborsAnswerElement ae =
          new NeighborsQuestionPlugin.NeighborsAnswerer(nq, _batfish).answer();
      SortedSet<RoleEdge> edges = ae.getRoleLanNeighbors();
      Map<String, Integer> roleDegrees = new HashMap<>();
      for (RoleEdge edge : edges) {
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

  // <question_page_comment>
  /**
   * Infer a regex that identifies a role from a node name.
   *
   * <p>Uses heuristics to identify a part of a node's name that represents its role.
   *
   * @type InferRoles multifile
   *
   * @param nodeRegex
   *           Regular expression for names of nodes to include. Default value
   *           is '.*' (all nodes).
   */
  public static final class InferRolesQuestion extends Question {

    private static final String NODE_REGEX_VAR = "nodeRegex";

    private String _nodeRegex;

    public InferRolesQuestion() {
      _nodeRegex = ".*";
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "inferroles";
    }

    @JsonProperty(NODE_REGEX_VAR)
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(NODE_REGEX_VAR)
    public void setNodeRegex(String regex) {
      _nodeRegex = regex;
    }

  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new InferRolesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new InferRolesQuestion();
  }

}
