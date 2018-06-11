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
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface.AclSpecs;
import org.batfish.question.AclReachabilityQuestionPlugin.AclReachabilityAnswerer;

/**
 * Class to hold methods used by both {@link AclReachabilityAnswerer} and {@link
 * AclReachability2Answerer}.
 */
public final class AclReachabilityAnswererUtils {

  private static final class AclNode {

    private final class Dependency {
      public final AclNode dependency;
      public final Set<Integer> lineNums = new TreeSet<>();

      public Dependency(AclNode dependency, int lineNum) {
        this.dependency = dependency;
        lineNums.add(lineNum);
      }
    }

    private final IpAccessList _acl;
    private final Set<Integer> _linesWithUndefinedRefs = new TreeSet<>();
    private final Set<Integer> _linesInCycles = new TreeSet<>();
    private final List<ImmutableList<String>> _cycles = new ArrayList<>();
    private final List<Dependency> _dependencies = new ArrayList<>();
    private final List<AclNode> _referencingAcls = new ArrayList<>();
    private IpAccessList _sanitizedAcl;

    public AclNode(IpAccessList acl) {
      _acl = acl;
    }

    public void addCycle(ImmutableList<String> cycleAcls) {

      // Record cycle
      _cycles.add(cycleAcls);

      // Remove previous ACL from referencing ACLs
      int aclIndex = cycleAcls.indexOf(_acl.getName());
      int cycleSize = cycleAcls.size();
      String prevAclName = cycleAcls.get((aclIndex - 1 + cycleSize) % cycleSize);
      int referencingAclIndex = 0;
      while (!_referencingAcls.get(referencingAclIndex).getName().equals(prevAclName)) {
        referencingAclIndex++;
      }
      _referencingAcls.remove(referencingAclIndex);

      // Remove next ACL from dependencies, and record line numbers that reference dependency
      String nextAclName = cycleAcls.get((aclIndex + 1) % cycleSize);
      int dependencyIndex = 0;
      while (!_dependencies.get(dependencyIndex).dependency.getName().equals(nextAclName)) {
        dependencyIndex++;
      }
      _linesInCycles.addAll(_dependencies.get(dependencyIndex).lineNums);
      _dependencies.remove(dependencyIndex);
    }

    public List<AclNode> getDependencies() {
      return _dependencies.stream().map(d -> d.dependency).collect(Collectors.toList());
    }

    public Map<String, IpAccessList> getFlatDependencies() {
      Map<String, IpAccessList> ret = new TreeMap<>();
      for (Dependency d : _dependencies) {
        ret.put(d.dependency.getName(), d.dependency._sanitizedAcl);
        ret.putAll(d.dependency.getFlatDependencies());
      }
      return ret;
    }

    public void addDependency(AclNode dependency, int lineNum) {
      for (Dependency d : _dependencies) {
        if (d.dependency.getName().equals(dependency.getName())) {
          d.lineNums.add(lineNum);
          return;
        }
      }
      _dependencies.add(new Dependency(dependency, lineNum));
    }

    public void addReferencingAcl(AclNode referencing) {
      _referencingAcls.add(referencing);
    }

    public void addUndefinedRef(int lineNum) {
      _linesWithUndefinedRefs.add(lineNum);
    }

    public void buildSanitizedAcl() {
      if (_linesWithUndefinedRefs.isEmpty() && _linesInCycles.isEmpty()) {
        // No lines need to be sanitized; just use the original IpAccessList as sanitized version
        _sanitizedAcl = _acl;
      } else {
        // Some lines need to be sanitized. Build a new IpAccessList, modifying problematic lines.
        List<IpAccessListLine> originalLines = _acl.getLines();
        List<IpAccessListLine> sanitizedLines = new ArrayList<>();
        for (int i = 0; i < originalLines.size(); i++) {
          IpAccessListLine oldLine = originalLines.get(i);
          if (!_linesInCycles.contains(i) && !_linesWithUndefinedRefs.contains(i)) {
            sanitizedLines.add(oldLine);
          } else {
            sanitizedLines.add(
                IpAccessListLine.builder()
                    .setMatchCondition(FalseExpr.INSTANCE)
                    .setAction(oldLine.getAction())
                    .setName(oldLine.getName())
                    .build());
          }
        }
        _sanitizedAcl = IpAccessList.builder().setName(getName()).setLines(sanitizedLines).build();
      }
    }

    public IpAccessList getAcl() {
      return _acl;
    }

    public List<ImmutableList<String>> getCycles() {
      return _cycles;
    }

    public Set<Integer> getLinesInCycles() {
      return _linesInCycles;
    }

    public Set<Integer> getLinesWithUndefinedRefs() {
      return _linesWithUndefinedRefs;
    }

    public IpAccessList getSanitizedAcl() {
      return _sanitizedAcl;
    }

    public String getName() {
      return _acl.getName();
    }
  }

  private static void createAclNodeWithDependencies(
      IpAccessList acl, Map<String, AclNode> aclNodeMap, SortedMap<String, IpAccessList> acls) {

    // Create ACL node for current ACL
    AclNode node = new AclNode(acl);
    aclNodeMap.put(acl.getName(), node);

    // Go through lines and add dependencies
    int index = 0;
    for (IpAccessListLine line : acl.getLines()) {
      AclLineMatchExpr matchCondition = line.getMatchCondition();
      if (matchCondition instanceof PermittedByAcl) {
        String referencedAclName = ((PermittedByAcl) matchCondition).getAclName();
        IpAccessList referencedAcl = acls.get(referencedAclName);
        if (referencedAcl == null) {
          // Referenced ACL doesn't exist. Mark line as unmatchable.
          node.addUndefinedRef(index);
        } else {
          AclNode referencedAclNode = aclNodeMap.get(referencedAclName);
          if (referencedAclNode == null) {
            // Referenced ACL not yet recorded; recurse on it
            createAclNodeWithDependencies(referencedAcl, aclNodeMap, acls);
            referencedAclNode = aclNodeMap.get(referencedAclName);
          }
          // Referenced ACL has now been recorded; add dependency and reference
          node.addDependency(referencedAclNode, index);
          referencedAclNode.addReferencingAcl(node);
        }
      }
      index++;
    }
  }

  public static List<ImmutableList<String>> sanitizeNode(
      AclNode node,
      List<AclNode> visited,
      Map<String, AclNode> sanitized,
      Map<String, AclNode> aclNodeMap) {

    // Mark starting node as visited
    visited.add(node);

    // Create set to hold cycles found
    List<ImmutableList<String>> cyclesFound = new ArrayList<>();

    // Go through dependencies (each ACL this one depends on will only appear as one dependency)
    for (AclNode dependency : node.getDependencies()) {
      if (sanitized.containsKey(dependency.getName())) {
        // We've already checked out the dependency. It must not be in a cycle with current ACL.
        continue;
      }
      int dependencyIndex = visited.indexOf(dependency);
      if (dependencyIndex != -1) {
        // Found a new cycle.
        ImmutableList<String> cycleAcls =
            ImmutableList.copyOf(
                visited
                    .subList(dependencyIndex, visited.size())
                    .stream()
                    .map(n -> n.getName())
                    .collect(Collectors.toList()));
        cyclesFound.add(cycleAcls);
      } else {
        // No cycle found; recurse on dependency to see if there is a cycle farther down.
        cyclesFound.addAll(
            sanitizeNode(aclNodeMap.get(dependency.getName()), visited, sanitized, aclNodeMap));
      }
    }
    // Remove current node from visited list
    visited.remove(node);

    // Record found cycles in this node.
    for (ImmutableList<String> cycleAcls : cyclesFound) {
      int indexOfThisAcl = cycleAcls.indexOf(node.getName());
      if (indexOfThisAcl != -1) {
        node.addCycle(cycleAcls);
      }
    }

    // Now that all cycles are recorded, never explore this node again, and sanitize its ACL.
    node.buildSanitizedAcl();
    sanitized.put(node.getName(), node);
    return cyclesFound;
  }

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
        SortedMap<String, IpAccessList> acls = configurations.get(hostname).getIpAccessLists();

        // Build graph of AclNodes containing pointers to dependencies and referencing nodes
        Map<String, AclNode> aclNodeMap = new TreeMap<>();
        for (IpAccessList acl : acls.values()) {
          String aclName = acl.getName();
          if (!aclNodeMap.containsKey(aclName) && aclRegex.matcher(aclName).matches()) {
            createAclNodeWithDependencies(acl, aclNodeMap, acls);
          }
        }

        // Sanitize nodes in graph (finds all cycles, creates sanitized versions of IpAccessLists)
        Map<String, AclNode> sanitizedAcls = new TreeMap<>();
        for (AclNode node : aclNodeMap.values()) {
          if (!sanitizedAcls.containsKey(node.getName())) {
            List<ImmutableList<String>> cycles =
                sanitizeNode(node, new ArrayList<>(), sanitizedAcls, aclNodeMap);
            for (ImmutableList<String> cycleAcls : cycles) {
              answer.addCycle(hostname, cycleAcls);
            }
          }
        }

        // For each ACL specified by aclRegex, create a CanonicalAcl with its dependencies
        for (Entry<String, AclNode> e : aclNodeMap.entrySet()) {
          String aclName = e.getKey();
          if (aclRegex.matcher(aclName).matches()) {
            AclNode node = e.getValue();
            CanonicalAcl currentAcl =
                new CanonicalAcl(
                    node.getSanitizedAcl(),
                    node.getAcl(),
                    node.getFlatDependencies(),
                    node.getLinesWithUndefinedRefs(),
                    node.getLinesInCycles());

            // If an identical ACL exists, add current hostname/aclName pair; otherwise, add new ACL
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
    return aclSpecs.stream().map(aclSpec -> aclSpec.build()).collect(Collectors.toList());
  }
}
