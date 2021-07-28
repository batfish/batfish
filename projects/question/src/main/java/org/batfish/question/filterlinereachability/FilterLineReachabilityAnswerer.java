package org.batfish.question.filterlinereachability;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.question.filterlinereachability.AclEraser.erase;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityRows.createMetadata;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityUtils.getReferencedAcls;
import static org.batfish.question.filterlinereachability.FilterLineReachabilityUtils.getReferencedInterfaces;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.ActionGetter;
import org.batfish.datamodel.acl.CanonicalAcl;
import org.batfish.datamodel.acl.CircularReferenceException;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.UndefinedReferenceException;
import org.batfish.datamodel.answers.AclSpecs;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Answers {@link FilterLineReachabilityQuestion}. */
@ParametersAreNonnullByDefault
public class FilterLineReachabilityAnswerer extends Answerer {

  public FilterLineReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    FilterLineReachabilityQuestion question = (FilterLineReachabilityQuestion) _question;
    FilterLineReachabilityRows answerRows = new FilterLineReachabilityRows();

    SpecifierContext ctxt = _batfish.specifierContext(snapshot);

    Map<String, Set<String>> specifiedAcls = getSpecifiedFilters(question, ctxt);

    SortedMap<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);
    List<AclSpecs> aclSpecs = getAclSpecs(configurations, specifiedAcls, answerRows);
    computeUnreachableFilterLines(aclSpecs).forEach(answerRows::addRowForLine);
    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    answer.postProcessAnswer(question, answerRows.getRows());
    return answer;
  }

  private static final class AclNode {

    private static final class Dependency {
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
    private List<AclLine> _sanitizedLines;

    public AclNode(IpAccessList acl) {
      _acl = acl;
    }

    /**
     * Replaces match expr of the line at the given {@code lineNum} with a {@link FalseExpr}. Used
     * to ignore lines with undefined or cyclic references.
     */
    public void markLineUnmatchable(int lineNum) {
      _sanitizedLines = firstNonNull(_sanitizedLines, new ArrayList<>(_acl.getLines()));
      AclLine originalLine = _sanitizedLines.remove(lineNum);

      // If the original line has a concrete action, preserve it; otherwise default to DENY
      LineAction unmatchableLineAction =
          firstNonNull(ActionGetter.getAction(originalLine), LineAction.DENY);
      _sanitizedLines.add(
          lineNum,
          ExprAclLine.builder()
              .setName(originalLine.getName())
              .setMatchCondition(FalseExpr.INSTANCE)
              .setAction(unmatchableLineAction)
              .build());
    }

    public void sanitizeLine(int lineNum, AclLine sanitizedLine) {
      _sanitizedLines = firstNonNull(_sanitizedLines, new ArrayList<>(_acl.getLines()));
      _sanitizedLines.remove(lineNum);
      _sanitizedLines.add(lineNum, sanitizedLine);
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
        markLineUnmatchable(lineNum);
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

    public void addUndefinedRef(int lineNum) {
      _linesWithUndefinedRefs.add(lineNum);
      markLineUnmatchable(lineNum);
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
      String aclName,
      Map<String, AclNode> aclNodeMap,
      Map<String, Supplier<IpAccessList>> eraseAcls,
      HeaderSpaceSanitizer headerSpaceSanitizer,
      Set<String> nodeInterfaces) {

    // Create ACL node for current ACL
    IpAccessList erasedAcl = eraseAcls.get(aclName).get();
    AclNode node = new AclNode(erasedAcl);
    aclNodeMap.put(aclName, node);

    // Go through lines and add dependencies
    int index = 0;
    for (AclLine line : erasedAcl.getLines()) {
      boolean lineMarkedUnmatchable = false;

      // Find all references to other ACLs and record them
      Set<String> referencedAcls = getReferencedAcls(line);
      if (!referencedAcls.isEmpty()) {
        if (!eraseAcls.keySet().containsAll(referencedAcls)) {
          // Not all referenced ACLs exist. Mark line as unmatchable.
          node.addUndefinedRef(index);
          lineMarkedUnmatchable = true;
        } else {
          for (String referencedAclName : referencedAcls) {
            AclNode referencedAclNode = aclNodeMap.get(referencedAclName);
            if (referencedAclNode == null) {
              // Referenced ACL not yet recorded; recurse on it
              createAclNode(
                  referencedAclName, aclNodeMap, eraseAcls, headerSpaceSanitizer, nodeInterfaces);
              referencedAclNode = aclNodeMap.get(referencedAclName);
            }
            // Referenced ACL has now been recorded; add dependency
            node.addDependency(referencedAclNode, index);
          }
        }
      }

      // Dereference all IpSpace references, or mark line unmatchable if it has invalid references
      if (!lineMarkedUnmatchable) {
        try {
          AclLine sanitizedForIpSpaces = headerSpaceSanitizer.visit(line);
          if (!line.equals(sanitizedForIpSpaces)) {
            node.sanitizeLine(index, sanitizedForIpSpaces);
          }
        } catch (CircularReferenceException | UndefinedReferenceException e) {
          // Line contains invalid IpSpaceReference: undefined or part of a circular reference chain
          node.addUndefinedRef(index);
          lineMarkedUnmatchable = true;
        }
      }

      // Find all references to interfaces and ensure they exist
      if (!lineMarkedUnmatchable) {
        Set<String> referencedInterfaces = getReferencedInterfaces(line);
        if (!nodeInterfaces.containsAll(referencedInterfaces)) {
          // Line references an undefined source interface. Report undefined ref.
          node.addUndefinedRef(index);
        } else {
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
                visited.subList(dependencyIndex, visited.size()).stream()
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
   * Collects the list of specified filters that we need to process, based on the nodes desired, the
   * filters desired, and whether generated filters are ignored
   */
  static Map<String, Set<String>> getSpecifiedFilters(
      FilterLineReachabilityQuestion question, SpecifierContext ctxt) {
    Set<String> specifiedNodes = question.nodeSpecifier().resolve(ctxt);
    FilterSpecifier filterSpecifier = question.getFilterSpecifier();

    return toImmutableMap(
        specifiedNodes,
        Function.identity(),
        node ->
            filterSpecifier.resolve(node, ctxt).stream()
                .filter(f -> !(question.getIgnoreComposites() && f.isComposite()))
                .map(IpAccessList::getName)
                .collect(ImmutableSet.toImmutableSet()));
  }

  /**
   * Generates the list of {@link AclSpecs} objects to analyze in preparation for some ACL
   * reachability question. Besides returning the {@link AclSpecs}, this method adds cycle results
   * to the answer and provides sanitized {@link IpAccessList}s without cycles, undefined
   * references, or dependence on named IP spaces.
   *
   * @param configurations Mapping of all hostnames to their {@link Configuration}s
   * @param specifiedAcls Specified ACLs to canonicalize, indexed by hostname.
   * @param answer Answer for ACL reachability question for which these {@link AclSpecs} are being
   *     generated
   * @return List of {@link AclSpecs} objects to analyze for an ACL reachability question with the
   *     given specified nodes and ACL regex.
   */
  @VisibleForTesting
  public static List<AclSpecs> getAclSpecs(
      SortedMap<String, Configuration> configurations,
      Map<String, Set<String>> specifiedAcls,
      FilterLineReachabilityRows answer) {
    List<AclSpecs.Builder> aclSpecs = new ArrayList<>();

    /*
     - For each ACL, build a CanonicalAcl structure with that ACL and referenced ACLs & interfaces
     - Deal with any references to undefined ACLs, IpSpaces, or interfaces
     - Deal with any cycles in ACL references
    */
    for (String hostname : configurations.keySet()) {
      if (specifiedAcls.containsKey(hostname)) {
        Configuration c = configurations.get(hostname);

        // Erase TraceElements and VendorStructureIds from ACLs, so ACLs from different devices can
        // be considered equivalent (VSIDs contain filenames).
        Map<String, Supplier<IpAccessList>> erasedAcls =
            toImmutableMap(
                c.getIpAccessLists().values(),
                IpAccessList::getName,
                acl -> Suppliers.memoize(() -> erase(acl)));

        Set<String> acls = specifiedAcls.get(hostname);
        HeaderSpaceSanitizer headerSpaceSanitizer = new HeaderSpaceSanitizer(c.getIpSpaces());
        Map<String, Interface> nodeInterfaces = c.getAllInterfaces();

        // Build graph of AclNodes containing pointers to dependencies and referencing nodes
        Map<String, AclNode> aclNodeMap = new TreeMap<>();
        for (String aclName : acls) {
          if (!aclNodeMap.containsKey(aclName)) {
            createAclNode(
                aclName, aclNodeMap, erasedAcls, headerSpaceSanitizer, nodeInterfaces.keySet());
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
        for (String aclName : acls) {
          AclNode node = aclNodeMap.get(aclName);

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
    return aclSpecs.stream().map(AclSpecs.Builder::build).collect(Collectors.toList());
  }

  private static Stream<UnreachableFilterLine> computeUnreachableFilterLines(
      List<AclSpecs> aclSpecs) {
    BDDPacket bddPacket = new BDDPacket();
    return aclSpecs.stream()
        .flatMap(
            aclSpec ->
                FilterLineReachabilityUtils.computeUnreachableFilterLines(aclSpec, bddPacket));
  }
}
