package org.batfish.question.aclreachability2;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

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
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface.AclSpecs;
import org.batfish.datamodel.visitors.IpSpaceDereferencer;

/**
 * Class to hold methods used by both {@link AclReachability2Answerer} and the original ACL
 * reachability question plugin.
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
    private final List<Dependency> _dependencies = new ArrayList<>();
    private final Set<String> _interfaces = new TreeSet<>();
    private IpAccessList _sanitizedAcl;
    private List<IpAccessListLine> _sanitizedLines;

    public AclNode(IpAccessList acl) {
      _acl = acl;
    }

    private void sanitizeLine(int lineNum, AclLineMatchExpr newMatchExpr) {
      _sanitizedLines = firstNonNull(_sanitizedLines, new ArrayList<>(_acl.getLines()));
      IpAccessListLine originalLine = _sanitizedLines.remove(lineNum);
      _sanitizedLines.add(
          lineNum,
          IpAccessListLine.builder()
              .setMatchCondition(newMatchExpr)
              .setAction(originalLine.getAction())
              .setName(originalLine.getName())
              .build());
    }

    public void sanitizeCycle(ImmutableList<String> cycleAcls) {
      // Remove previous ACL from referencing ACLs
      int aclIndex = cycleAcls.indexOf(_acl.getName());
      int cycleSize = cycleAcls.size();

      // Remove next ACL from dependencies, and record line numbers that reference dependency
      String nextAclName = cycleAcls.get((aclIndex + 1) % cycleSize);
      int dependencyIndex = 0;
      while (!_dependencies.get(dependencyIndex).dependency.getName().equals(nextAclName)) {
        dependencyIndex++;
      }

      for (int lineNum : _dependencies.remove(dependencyIndex).lineNums) {
        _linesInCycles.add(lineNum);
        sanitizeLine(lineNum, FalseExpr.INSTANCE);
      }
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

    public Set<String> getInterfaceDependencies() {
      Set<String> interfaceDependencies = new TreeSet<>(_interfaces);
      for (Dependency d : _dependencies) {
        interfaceDependencies.addAll(d.dependency.getInterfaceDependencies());
      }
      return interfaceDependencies;
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

    public void addInterfaces(Set<String> newInterfaces) {
      _interfaces.addAll(newInterfaces);
    }

    public void sanitizeHeaderSpace(
        int lineNum, HeaderSpace headerSpace, Map<String, IpSpace> namedIpSpaces) {
      try {
        // Try dereferencing all IpSpace fields in header space. If that results in a header space
        // different from the original, sanitize the line.
        HeaderSpace dereferenced =
            IpSpaceDereferencer.dereferenceHeaderSpace(headerSpace, namedIpSpaces);
        if (!dereferenced.equals(headerSpace)) {
          sanitizeLine(lineNum, new MatchHeaderSpace(dereferenced));
        }
      } catch (BatfishException e) {
        // If dereferencing causes an error, one of the IpSpaces was a cycle or undefined ref.
        addUndefinedRef(lineNum);
      }
    }

    public void addUndefinedRef(int lineNum) {
      _linesWithUndefinedRefs.add(lineNum);
      sanitizeLine(lineNum, FalseExpr.INSTANCE);
    }

    public void buildSanitizedAcl() {
      // If _sanitizedLines was never initialized, just use original ACL for sanitized ACL
      _sanitizedAcl =
          _sanitizedLines == null
              ? _acl
              : IpAccessList.builder().setName(getName()).setLines(_sanitizedLines).build();
    }

    public IpAccessList getAcl() {
      return _acl;
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

  private static void createAclNode(
      IpAccessList acl,
      Map<String, AclNode> aclNodeMap,
      SortedMap<String, IpAccessList> acls,
      Map<String, IpSpace> namedIpSpaces,
      Set<String> nodeInterfaces) {

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
            createAclNode(referencedAcl, aclNodeMap, acls, namedIpSpaces, nodeInterfaces);
            referencedAclNode = aclNodeMap.get(referencedAclName);
          }
          // Referenced ACL has now been recorded; add dependency
          node.addDependency(referencedAclNode, index);
        }
      } else if (matchCondition instanceof MatchHeaderSpace) {
        // Sanitize IP space references, or mark the line unmatchable if it has invalid references
        HeaderSpace headerSpace = ((MatchHeaderSpace) matchCondition).getHeaderspace();
        node.sanitizeHeaderSpace(index, headerSpace, namedIpSpaces);
      } else if (matchCondition instanceof MatchSrcInterface) {
        Set<String> referencedInterfaces = ((MatchSrcInterface) matchCondition).getSrcInterfaces();
        if (!nodeInterfaces.containsAll(referencedInterfaces)) {
          // Line references an undefined source interface. Report undefined ref.
          node.addUndefinedRef(index);
        } else {
          // Line interfaces are valid. Record.
          node.addInterfaces(referencedInterfaces);
        }
      }
      index++;
    }
  }

  private static List<ImmutableList<String>> sanitizeNode(
      AclNode node, List<AclNode> visited, Set<String> sanitized, Map<String, AclNode> aclNodeMap) {

    // Mark starting node as visited
    visited.add(node);

    // Create set to hold cycles found
    List<ImmutableList<String>> cyclesFound = new ArrayList<>();

    // Go through dependencies (each ACL this one depends on will only appear as one dependency)
    for (AclNode dependency : node.getDependencies()) {
      if (sanitized.contains(dependency.getName())) {
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
                    .map(AclNode::getName)
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
        node.sanitizeCycle(cycleAcls);
      }
    }

    // Now that all cycles are recorded, never explore this node again, and sanitize its ACL.
    node.buildSanitizedAcl();
    sanitized.add(node.getName());
    return cyclesFound;
  }

  /**
   * Generates the list of {@link AclSpecs} objects to analyze in preparation for some ACL
   * reachability question. Besides returning the {@link AclSpecs}, this method adds cycle results
   * to the answer and provides sanitized {@link IpAccessList}s without cycles, undefined
   * references, or dependence on named IP spaces.
   *
   * @param configurations Mapping of all hostnames to their {@link Configuration}s
   * @param specifiedNodes Nodes from which to collect ACLs
   * @param aclRegex Regex specifying which ACLs to canonicalize from the ACLs in the given node set
   * @param answer Answer for ACL reachability question for which these {@link AclSpecs} are being
   *     generated
   * @return List of {@link AclSpecs} objects to analyze for an ACL reachability question with the
   *     given specified nodes and ACL regex.
   */
  public static List<AclSpecs> getAclSpecs(
      SortedMap<String, Configuration> configurations,
      Set<String> specifiedNodes,
      Pattern aclRegex,
      AclLinesAnswerElementInterface answer) {
    List<AclSpecs.Builder> aclSpecs = new ArrayList<>();

    /*
     - For each ACL, build a CanonicalAcl structure with that ACL and referenced ACLs & interfaces
     - Deal with any references to undefined ACLs, IpSpaces, or interfaces
     - Deal with any cycles in ACL references
    */
    for (String hostname : configurations.keySet()) {
      if (specifiedNodes.contains(hostname)) {
        Configuration c = configurations.get(hostname);
        SortedMap<String, IpAccessList> acls = c.getIpAccessLists();
        Map<String, IpSpace> namedIpSpaces = c.getIpSpaces();
        Map<String, Interface> nodeInterfaces = c.getInterfaces();

        // Build graph of AclNodes containing pointers to dependencies and referencing nodes
        Map<String, AclNode> aclNodeMap = new TreeMap<>();
        for (IpAccessList acl : acls.values()) {
          String aclName = acl.getName();
          if (!aclNodeMap.containsKey(aclName) && aclRegex.matcher(aclName).matches()) {
            createAclNode(acl, aclNodeMap, acls, namedIpSpaces, nodeInterfaces.keySet());
          }
        }

        // Sanitize nodes in graph (finds all cycles, creates sanitized versions of IpAccessLists)
        Set<String> sanitizedAcls = new TreeSet<>();
        for (AclNode node : aclNodeMap.values()) {
          if (!sanitizedAcls.contains(node.getName())) {
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

            // Finalize interfaces. If ACL references all interfaces on the device, keep interfaces
            // list as-is; otherwise, add one extra interface to represent the "unreferenced
            // interface not originating from router" possibility. Needs to have a name different
            // from any referenced interface.
            Set<String> referencedInterfaces = node.getInterfaceDependencies();
            if (referencedInterfaces.size() < nodeInterfaces.size()) {
              // At least one interface was not referenced by the ACL. Represent that option.
              String unreferencedIfaceName = "unreferencedInterface";
              int n = 0;
              while (referencedInterfaces.contains(unreferencedIfaceName)) {
                unreferencedIfaceName = "unreferencedInterface" + n;
                n++;
              }
              referencedInterfaces = new TreeSet<>(referencedInterfaces);
              referencedInterfaces.add(unreferencedIfaceName);
            }

            CanonicalAcl currentAcl =
                new CanonicalAcl(
                    node.getSanitizedAcl(),
                    node.getAcl(),
                    node.getFlatDependencies(),
                    referencedInterfaces,
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
    return aclSpecs.stream().map(AclSpecs.Builder::build).collect(Collectors.toList());
  }
}
