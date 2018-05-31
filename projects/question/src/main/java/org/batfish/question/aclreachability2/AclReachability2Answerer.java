package org.batfish.question.aclreachability2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.Pair;
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

    /* Unfortunately there is more to do here.
    - Look through the ACLs
    - For each ACL, build a CanonicalAcl structure with that ACL and its referenced ACLs. During this:
      - Deal with any references to ACLs we don't know about
      - Deal with any cycles in ACL references
     */
    /*
    Make a map hostname -> acl name -> set of acl and its referenced acls
     */
    Set<String> specifiedNodes =
        ((AclReachability2Question) _question).getNodeRegex().getMatchingNodes(_batfish);
    Pattern aclRegex = Pattern.compile(((AclReachability2Question) _question).getAclNameRegex());
    SortedMap<String, Configuration> configurations = _batfish.loadConfigurations();
    Map<CanonicalAcl, SortedSet<Pair<String, String>>> canonicalAcls = new TreeMap<>();

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
            canonicalAcls
                .computeIfAbsent(
                    new CanonicalAcl(aclName, acls.get(aclName), e.getValue()),
                    a -> Sets.newTreeSet())
                .add(new Pair(hostname, aclName));
          }
        }
      }
    }

    //    // get comparesamename results for acls
    //    CompareSameNameQuestion csnQuestion =
    //        new CompareSameNameQuestion(
    //            true,
    //            null,
    //            null,
    //            ImmutableSortedSet.of(IpAccessList.class.getSimpleName()),
    //            question.getNodeRegex(),
    //            true);
    //    CompareSameNameAnswerer csnAnswerer = new CompareSameNameAnswerer(csnQuestion, _batfish);
    //    CompareSameNameAnswerElement csnAnswer = csnAnswerer.answer();
    //    NamedStructureEquivalenceSets<?> aclEqSets =
    //        csnAnswer.getEquivalenceSets().get(IpAccessList.class.getSimpleName());
    //
    //    _batfish.answerAclReachability(question.getAclNameRegex(), aclEqSets, answerRows);
    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    answer.postProcessAnswer(question, answerRows.getRows());
    return answer;
  }

  private void breakAndReportCycles(
      String hostname,
      String aclName,
      List<String> aclsSoFar,
      SortedMap<String, IpAccessList> acls,
      AclLines2Rows answerRows) {

    aclsSoFar.add(aclName);

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
        } else {
          int referencedAclIndex = aclsSoFar.indexOf(referencedAcl);
          if (referencedAclIndex != -1) {
            // Dependency causes a cycle. Record cycle and modify line to be unmatchable.
            answerRows.addCycle(hostname, aclsSoFar.subList(referencedAclIndex, aclsSoFar.size()));
            line.makeUnmatchableDueToCycle();
            dependenciesWithCycles.add(referencedAcl);
          } else {
            // Dependency doesn't cause a cycle so far; recurse to check deeper.
            breakAndReportCycles(hostname, referencedAcl, aclsSoFar, acls, answerRows);
          }
        }
      }
    }
    aclsSoFar.remove(aclsSoFar.size() - 1);
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
        IpAccessList referencedAcl = acls.get(referencedAclName);
        if (referencedAcl == null) {
          // Referenced ACL doesn't exist. Mark line as unmatchable; will be reported in answer.
          line.makeUnmatchableDueToUndefinedReference();
        } else {
          // Collect dependencies of referencedAcl if it hasn't already been done
          if (!aclDependencies.containsKey(referencedAclName)) {
            collectDependencies(referencedAclName, acls, aclDependencies);
          }
          // Add referencedAcl and its dependencies to current ACL's dependencies
          dependencies.put(referencedAclName, referencedAcl);
          dependencies.putAll(aclDependencies.get(referencedAclName));
        }
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
                    AclLines2Rows.COL_NODES, Schema.list(Schema.NODE), "Nodes", true, false))
            .add(new ColumnMetadata(AclLines2Rows.COL_ACL, Schema.STRING, "ACL name", true, false))
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
