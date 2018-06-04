package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class AclReachabilityQuestionPlugin extends QuestionPlugin {

  public static class AclReachabilityAnswerer extends Answerer {

    public AclReachabilityAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      AclReachabilityQuestion question = (AclReachabilityQuestion) _question;
      AclLinesAnswerElement answer = new AclLinesAnswerElement();

      /*
       - For each ACL, build a CanonicalAcl structure with that ACL and its referenced ACLs
       - Deal with any references to ACLs we don't know about
       - Deal with any cycles in ACL references
      */
      Set<String> specifiedNodes = question.getNodeRegex().getMatchingNodes(_batfish);
      Pattern aclRegex;
      try {
        aclRegex = Pattern.compile(question.getAclNameRegex());
      } catch (PatternSyntaxException e) {
        throw new BatfishException(
            "Supplied regex for nodes is not a valid Java regex: \""
                + question.getAclNameRegex()
                + "\"",
            e);
      }
      SortedMap<String, Configuration> configurations = _batfish.loadConfigurations();
      List<CanonicalAcl> canonicalAcls = new ArrayList<>();

      for (String hostname : configurations.keySet()) {
        if (specifiedNodes.contains(hostname)) {
          SortedMap<String, IpAccessList> acls = configurations.get(hostname).getIpAccessLists();
          Map<String, Map<String, IpAccessList>> aclDependenciesMap = new TreeMap<>();

          // Break cycles in ACLs specified by aclRegex and their dependencies.
          // Adds cycles to answer and changes cycle lines to be unmatchable.
          for (String aclName : acls.keySet()) {
            if (!aclDependenciesMap.containsKey(aclName) && aclRegex.matcher(aclName).matches()) {
              breakAndReportCycles(aclName, new ArrayList<>(), acls);
            }
          }

          // Create map of (aclName) -> (ACLs upon which it depends)
          // Include in the map all ACLs specified by aclRegex and all their dependencies
          for (String aclName : acls.keySet()) {
            if (aclRegex.matcher(aclName).matches() && !aclDependenciesMap.containsKey(aclName)) {
              collectDependencies(aclName, acls, aclDependenciesMap);
            }
          }

          // For each ACL specified by aclRegex, create a CanonicalAcl with its dependencies
          for (Entry<String, Map<String, IpAccessList>> e : aclDependenciesMap.entrySet()) {
            String aclName = e.getKey();
            if (aclRegex.matcher(aclName).matches()) {
              CanonicalAcl currentAcl =
                  new CanonicalAcl(aclName, acls.get(aclName), e.getValue(), hostname);

              // If an identical ACL doesn't exist, add it to the set; otherwise, find the existing
              // version and add current hostname
              boolean added = false;
              for (CanonicalAcl existingAcl : canonicalAcls) {
                if (existingAcl.equals(currentAcl)) {
                  existingAcl.addSource(hostname, aclName);
                  added = true;
                  break;
                }
              }
              if (!added) {
                canonicalAcls.add(currentAcl);
              }
            }
          }
        }
      }

      _batfish.answerAclReachability(canonicalAcls, answer);
      return answer;
    }

    /*
     Return value is a list of ACLs we've already recursed through that start a cycle we've found.
     For example, if we have ACLs A, B, C, D, E with dependencies:
      A -> B
      B -> C
      C -> D
      D -> B, E
      E -> D
     and we traverse in alphabetical order, then returning from E the return value will be [B, D].
    */
    private List<String> breakAndReportCycles(
        String aclName, List<String> aclsSoFar, SortedMap<String, IpAccessList> acls) {

      aclsSoFar.add(aclName);
      List<String> firstNodesInCycles = new ArrayList<>();

      // Keep track of dependencies that caused cycles (unlikely to have repeat dependencies in the
      // same ACL, but don't want to list a cycle twice in answer)
      Set<String> dependenciesWithCycles = new TreeSet<>();

      // Find lines with dependencies
      for (IpAccessListLine line : acls.get(aclName).getLines()) {
        AclLineMatchExpr matchCondition = line.getMatchCondition();
        if (matchCondition instanceof PermittedByAcl) {
          String referencedAcl = ((PermittedByAcl) matchCondition).getAclName();

          if (dependenciesWithCycles.contains(referencedAcl)) {
            // Dependency was already found to cause a cycle. Modify the line but don't record
            // again.
            line.makeUnmatchableDueToCycle();
          } else if (!acls.containsKey(referencedAcl)) {
            // Referenced ACL doesn't exist. Mark line as unmatchable; will be reported in answer.
            line.makeUnmatchableDueToUndefinedReference();
          } else {
            int referencedAclIndex = aclsSoFar.indexOf(referencedAcl);
            if (referencedAclIndex != -1) {
              // Dependency causes a cycle. Modify line to be unmatchable.
              line.makeUnmatchableDueToCycle();
              dependenciesWithCycles.add(referencedAcl);
              firstNodesInCycles.add(referencedAcl);
            } else {
              // Dependency doesn't cause a cycle so far; recurse to check deeper.
              firstNodesInCycles.addAll(breakAndReportCycles(referencedAcl, aclsSoFar, acls));
              if (!firstNodesInCycles.isEmpty()) {
                line.makeUnmatchableDueToCycle();
              }
            }
          }
        }
      }
      aclsSoFar.remove(aclsSoFar.size() - 1);
      firstNodesInCycles.remove(aclName);
      return firstNodesInCycles;
    }

    private void collectDependencies(
        String aclName,
        SortedMap<String, IpAccessList> acls,
        Map<String, Map<String, IpAccessList>> aclDependencies) {
      Map<String, IpAccessList> dependencies = new TreeMap<>();

      // Go through lines of ACL to find dependencies
      for (IpAccessListLine line : acls.get(aclName).getLines()) {
        AclLineMatchExpr matchCondition = line.getMatchCondition();
        if (matchCondition instanceof PermittedByAcl) {

          // Found a dependency.
          String referencedAclName = ((PermittedByAcl) matchCondition).getAclName();

          // Collect dependencies of referencedAcl if it hasn't already been done
          if (!aclDependencies.containsKey(referencedAclName)) {
            collectDependencies(referencedAclName, acls, aclDependencies);
          }
          // Add referencedAcl and its dependencies to current ACL's dependencies
          dependencies.put(referencedAclName, acls.get(referencedAclName));
          dependencies.putAll(aclDependencies.get(referencedAclName));
        }
      }
      aclDependencies.put(aclName, dependencies);
    }
  }

  // <question_page_comment>

  /**
   * Identifies unreachable lines in ACLs.
   *
   * <p>Report ACLs with unreachable lines, as well as reachability of each line within the ACL.
   * Unreachable lines can indicate erroneous configuration.
   *
   * @type AclReachability onefile
   * @param aclNameRegex Regular expression for names of the ACLs to analyze. Default value is '.*'
   *     (i.e., all ACLs).
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("AclReachability", aclNameRegex='OUTSIDE_TO_INSIDE.*') Analyzes only ACLs
   *     whose names start with 'OUTSIDE_TO_INSIDE'.
   */
  public static class AclReachabilityQuestion extends Question {

    private static final String PROP_ACL_NAME_REGEX = "aclNameRegex";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private String _aclNameRegex;

    private NodesSpecifier _nodeRegex;

    public AclReachabilityQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
      _aclNameRegex = ".*";
    }

    public AclReachabilityQuestion(
        @Nullable @JsonProperty(PROP_ACL_NAME_REGEX) String aclNameRegex,
        @Nullable @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex) {
      _aclNameRegex = firstNonNull(aclNameRegex, ".*");
      _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    }

    @JsonProperty(PROP_ACL_NAME_REGEX)
    public String getAclNameRegex() {
      return _aclNameRegex;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "aclreachability";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "%s %s%s=\"%s\" %s=\"%s\"",
              getName(),
              prettyPrintBase(),
              PROP_ACL_NAME_REGEX,
              _aclNameRegex,
              PROP_NODE_REGEX,
              _nodeRegex);
      return retString;
    }

    @JsonProperty(PROP_ACL_NAME_REGEX)
    public void setAclNameRegex(String regex) {
      _aclNameRegex = regex;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier regex) {
      _nodeRegex = regex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AclReachabilityAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AclReachabilityQuestion();
  }
}
