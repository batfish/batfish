package org.batfish.question.aclreachability2;

import com.google.common.collect.ImmutableList;
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
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclLines2Rows;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

@ParametersAreNonnullByDefault
public class AclReachability2Answerer extends Answerer {

  public AclReachability2Answerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer() {
    AclReachability2Question question = (AclReachability2Question) _question;
    AclLines2Rows answerRows = new AclLines2Rows();

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
            breakAndReportCycles(hostname, aclName, new ArrayList<>(), acls, answerRows);
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

    answerRows.setCanonicalAcls(canonicalAcls);
    _batfish.answerAclReachability(canonicalAcls, answerRows);
    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    answer.postProcessAnswer(question, answerRows.getRows());
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
      String hostname,
      String aclName,
      List<String> aclsSoFar,
      SortedMap<String, IpAccessList> acls,
      AclLines2Rows answerRows) {

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
          // Dependency was already found to cause a cycle. Modify the line but don't record again.
          line.makeUnmatchableDueToCycle();
        } else if (!acls.containsKey(referencedAcl)) {
          // Referenced ACL doesn't exist. Mark line as unmatchable; will be reported in answer.
          line.makeUnmatchableDueToUndefinedReference();
        } else {
          int referencedAclIndex = aclsSoFar.indexOf(referencedAcl);
          if (referencedAclIndex != -1) {
            // Dependency causes a cycle. Record cycle and modify line to be unmatchable.
            answerRows.addCycle(hostname, aclsSoFar.subList(referencedAclIndex, aclsSoFar.size()));
            line.makeUnmatchableDueToCycle();
            dependenciesWithCycles.add(referencedAcl);
            firstNodesInCycles.add(referencedAcl);
          } else {
            // Dependency doesn't cause a cycle so far; recurse to check deeper.
            firstNodesInCycles.addAll(
                breakAndReportCycles(hostname, referencedAcl, aclsSoFar, acls, answerRows));
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

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  private static TableMetadata createMetadata(AclReachability2Question question) {
    List<ColumnMetadata> columnMetadata =
        new ImmutableList.Builder<ColumnMetadata>()
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_SOURCES,
                    Schema.list(Schema.STRING),
                    "ACL sources",
                    true,
                    false))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_LINES, Schema.list(Schema.STRING), "ACL lines", false, false))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_BLOCKED_LINE_NUM,
                    Schema.INTEGER,
                    "Blocked line number",
                    true,
                    false))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_BLOCKING_LINE_NUMS,
                    Schema.list(Schema.INTEGER),
                    "Blocking line numbers",
                    false,
                    true))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_DIFF_ACTION, Schema.BOOLEAN, "Different action", false, true))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_MESSAGE, Schema.STRING, "Message", false, false))
            .build();

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(String.format("${%s}", AclLines2Rows.COL_MESSAGE));
    }
    return new TableMetadata(columnMetadata, dhints);
  }
}
