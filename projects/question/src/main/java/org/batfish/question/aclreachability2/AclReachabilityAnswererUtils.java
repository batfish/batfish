package org.batfish.question.aclreachability2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface.AclSpecs;
import org.batfish.question.AclReachabilityQuestionPlugin.AclReachabilityAnswerer;

/**
 * Class to hold methods used by both {@link AclReachabilityAnswerer} and {@link
 * AclReachability2Answerer}.
 */
public final class AclReachabilityAnswererUtils {

  /**
   * Generates the list of {@link CanonicalAcl} objects to analyze in preparation for some ACL
   * reachability question. Besides returning the {@link CanonicalAcl}s, this method can add cycle
   * results to the answer and modify {@link IpAccessListLine}s to fix cycles and undefined
   * references.
   *
   * @param configurations Mapping of all hostnames to their {@link Configuration}s
   * @param specifiedNodes Nodes from which to collect ACLs
   * @param aclRegex Regex specifying which ACLs to canonicalize from the ACLs in the given node set
   * @param answer Answer for ACL reachability question for which these {@link CanonicalAcl}s are
   *     being generated
   * @return List of {@link CanonicalAcl} objects to analyze for an ACL reachability question with
   *     the given specified nodes and ACL regex.
   */
  public static List<AclSpecs> getAclSpecs(
      SortedMap<String, Configuration> configurations,
      Set<String> specifiedNodes,
      Pattern aclRegex,
      AclLinesAnswerElementInterface answer) {
    List<AclSpecs.Builder> aclSpecs = new ArrayList<>();

    /*
     - For each ACL, build a CanonicalAcl structure with that ACL and its referenced ACLs
     - Deal with any references to ACLs we don't know about
     - Deal with any cycles in ACL references
    */
    for (String hostname : configurations.keySet()) {
      if (specifiedNodes.contains(hostname)) {
        Map<String, IpAccessList> originalAcls = configurations.get(hostname).getIpAccessLists();

        // Create a copy of the configuration's ACL map
        SortedMap<String, IpAccessList> acls = new TreeMap<>();
        acls.putAll(originalAcls);
        Map<String, Map<String, IpAccessList>> aclDependenciesMap = new TreeMap<>();

        // Break cycles in ACLs specified by aclRegex and their dependencies.
        // Adds cycles to answer and changes cycle lines to be unmatchable.
        for (String aclName : acls.keySet()) {
          if (!aclDependenciesMap.containsKey(aclName) && aclRegex.matcher(aclName).matches()) {
            breakAndReportCycles(hostname, aclName, new ArrayList<>(), acls, answer);
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
                new CanonicalAcl(acls.get(aclName), originalAcls.get(aclName), e.getValue());

            // If an identical ACL doesn't exist, add it to the set; otherwise, find the existing
            // version and add current hostname
            boolean added = false;
            for (AclSpecs.Builder aclSpec : aclSpecs) {
              if (aclSpec.getAcl().equals(currentAcl)) {
                aclSpec.addSource(hostname, aclName);
                added = true;
                break;
              }
            }
            if (!added) {
              aclSpecs.add(AclSpecs.builder().setAcl(currentAcl).addSource(hostname, aclName));
            }
          }
        }
      }
    }
    List<AclSpecs> finalAclSpecs =
        aclSpecs.stream().map(aclSpec -> aclSpec.build()).collect(Collectors.toList());
    return finalAclSpecs;
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
  private static List<String> breakAndReportCycles(
      String hostname,
      String aclName,
      List<String> aclsSoFar,
      SortedMap<String, IpAccessList> acls,
      AclLinesAnswerElementInterface answer) {

    aclsSoFar.add(aclName);
    List<String> firstNodesInCycles = new ArrayList<>();

    // Keep track of dependencies that caused cycles (unlikely to have repeat dependencies in the
    // same ACL, but don't want to list a cycle twice in answer)
    Set<String> dependenciesWithCycles = new TreeSet<>();

    // Find lines with dependencies
    int index = 0;
    for (IpAccessListLine line : acls.get(aclName).getLines()) {
      AclLineMatchExpr matchCondition = line.getMatchCondition();
      if (matchCondition instanceof PermittedByAcl) {
        String referencedAcl = ((PermittedByAcl) matchCondition).getAclName();

        if (dependenciesWithCycles.contains(referencedAcl)) {
          // Dependency was already found to cause a cycle. Modify the line but don't record again.
          acls.put(aclName, acls.get(aclName).createVersionWithUnmatchableLine(index, true, false));
        } else if (!acls.containsKey(referencedAcl)) {
          // Referenced ACL doesn't exist. Mark line as unmatchable; will be reported in answer.
          acls.put(aclName, acls.get(aclName).createVersionWithUnmatchableLine(index, false, true));
        } else {
          int referencedAclIndex = aclsSoFar.indexOf(referencedAcl);
          if (referencedAclIndex != -1) {
            // Dependency causes a cycle. Record cycle and modify
            // line to be unmatchable.
            answer.addCycle(hostname, aclsSoFar.subList(referencedAclIndex, aclsSoFar.size()));
            acls.put(
                aclName, acls.get(aclName).createVersionWithUnmatchableLine(index, true, false));

            dependenciesWithCycles.add(referencedAcl);
            firstNodesInCycles.add(referencedAcl);
          } else {
            // Dependency doesn't cause a cycle so far; recurse to check deeper.
            firstNodesInCycles.addAll(
                breakAndReportCycles(hostname, referencedAcl, aclsSoFar, acls, answer));
            if (!firstNodesInCycles.isEmpty()) {
              acls.put(
                  aclName, acls.get(aclName).createVersionWithUnmatchableLine(index, true, false));
            }
          }
        }
      }
      index++;
    }
    aclsSoFar.remove(aclsSoFar.size() - 1);
    firstNodesInCycles.remove(aclName);
    return firstNodesInCycles;
  }

  private static void collectDependencies(
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
