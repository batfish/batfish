package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.Pair;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;

/**
 * Represents answers to aclReachability. Implements {@link AclLinesAnswerElementInterface} so that
 * aclReachability and aclReachability2 can use some of the same methods to answer both.
 */
public class AclLinesAnswerElement extends AnswerElement implements AclLinesAnswerElementInterface {

  public static class AclReachabilityEntry implements Comparable<AclReachabilityEntry> {

    private static final String PROP_INDEX = "index";

    private static final String PROP_NAME = "name";

    private boolean _differentAction = false;

    private Integer _earliestMoreGeneralLineIndex;

    private String _earliestMoreGeneralLineName;

    private final int _index;

    private final String _name;

    @JsonCreator
    public AclReachabilityEntry(
        @JsonProperty(PROP_INDEX) int index, @JsonProperty(PROP_NAME) String name) {
      _index = index;
      _name = name;
    }

    @Override
    public int compareTo(AclReachabilityEntry rhs) {
      return Integer.compare(_index, rhs._index);
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      } else if (!(o instanceof AclReachabilityEntry)) {
        return false;
      }
      return _index == ((AclReachabilityEntry) o)._index;
    }

    public boolean getDifferentAction() {
      return _differentAction;
    }

    /**
     * Returns the line number of the earliest more general reachable line, which may be -1 if:
     *
     * <ul>
     *   <li>The line is independently unmatchable (no blocking line)
     *   <li>The line is blocked by multiple partially covering earlier lines
     * </ul>
     *
     * <p>This is a temporary solution for communicating these cases and will be changed soon.
     */
    public Integer getEarliestMoreGeneralLineIndex() {
      return _earliestMoreGeneralLineIndex;
    }

    /**
     * Returns the text of the earliest more general reachable line, except in these cases:
     *
     * <ul>
     *   <li>If line is independently unmatchable, returns "This line will never match any packet,
     *       independent of preceding lines."
     *   <li>If line has multiple partially blocking lines, returns "Multiple earlier lines
     *       partially block this line, making it unreachable."
     * </ul>
     *
     * <p>This is a temporary solution for communicating the latter two cases and will be changed
     * soon.
     */
    public String getEarliestMoreGeneralLineName() {
      return _earliestMoreGeneralLineName;
    }

    public int getIndex() {
      return _index;
    }

    public String getName() {
      return _name;
    }

    @Override
    public int hashCode() {
      return _name.hashCode();
    }

    public String prettyPrint(String indent) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%s[index %d] %s\n", indent, _index, _name));
      sb.append(
          String.format(
              "%s  Earliest covering line: [index %d] %s\n",
              indent, _earliestMoreGeneralLineIndex, _earliestMoreGeneralLineName));
      sb.append(String.format("%s  Is different action: %s\n", indent, _differentAction));
      return sb.toString();
    }

    public void setDifferentAction(boolean differentAction) {
      _differentAction = differentAction;
    }

    public void setEarliestMoreGeneralLineIndex(Integer earliestMoreGeneralLineIndex) {
      _earliestMoreGeneralLineIndex = earliestMoreGeneralLineIndex;
    }

    public void setEarliestMoreGeneralLineName(String earliestMoreGeneralLineName) {
      _earliestMoreGeneralLineName = earliestMoreGeneralLineName;
    }
  }

  private SortedMap<String, SortedMap<String, IpAccessList>> _acls;

  private SortedMap<String, SortedMap<String, SortedSet<String>>> _equivalenceClasses;

  private SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> _reachableLines;

  private SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> _unreachableLines;

  public AclLinesAnswerElement() {
    _acls = new TreeMap<>();
    _equivalenceClasses = new TreeMap<>();
    _reachableLines = new TreeMap<>();
    _unreachableLines = new TreeMap<>();
  }

  /**
   * Not used in this class because its {@link AclReachabilityEntry} class is better suited to
   * report each line in a cycle individually. Having this method in {@link
   * AclLinesAnswerElementInterface} allows code used for both aclReachability and aclReachability2
   * to report cycles for aclReachability2.
   */
  @Override
  public void addCycle(String hostname, List<String> aclsInCycle) {}

  private void addEquivalenceClass(
      String aclName, String hostname, SortedSet<String> eqClassNodes) {
    SortedMap<String, SortedSet<String>> byRep =
        _equivalenceClasses.computeIfAbsent(aclName, k -> new TreeMap<>());
    byRep.put(hostname, eqClassNodes);
  }

  private void addLine(
      SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> lines,
      String hostname,
      String aclName,
      IpAccessList ipAccessList,
      AclReachabilityEntry entry) {
    SortedMap<String, IpAccessList> aclsByHostname =
        _acls.computeIfAbsent(hostname, k -> new TreeMap<>());
    if (!aclsByHostname.containsKey(aclName)) {
      aclsByHostname.put(aclName, ipAccessList);
    }
    lines
        .computeIfAbsent(hostname, k -> new TreeMap<>())
        .computeIfAbsent(aclName, k -> new TreeSet<>())
        .add(entry);
  }

  @Override
  public void addReachableLine(AclSpecs aclSpecs, int lineNumber) {
    Pair<String, String> hostnameAndAcl = aclSpecs.getRepresentativeHostnameAclPair();
    IpAccessList acl = aclSpecs.acl.getOriginal();
    IpAccessListLine line = acl.getLines().get(lineNumber);
    addLine(
        _reachableLines,
        hostnameAndAcl.getFirst(),
        hostnameAndAcl.getSecond(),
        acl,
        new AclReachabilityEntry(lineNumber, firstNonNull(line.getName(), line.toString())));
  }

  @Override
  public void addUnreachableLine(
      AclSpecs aclSpecs, int lineNumber, boolean unmatchable, SortedSet<Integer> blockingLines) {

    IpAccessListLine blockedLine = aclSpecs.acl.getAcl().getLines().get(lineNumber);
    IpAccessListLine originalLine = aclSpecs.acl.getOriginal().getLines().get(lineNumber);
    AclReachabilityEntry entry =
        new AclReachabilityEntry(
            lineNumber, firstNonNull(originalLine.getName(), originalLine.toString()));

    if (blockedLine.undefinedReference()) {
      entry.setEarliestMoreGeneralLineIndex(-1);
      entry.setEarliestMoreGeneralLineName(
          "This line will never match any packet because it references an undefined structure.");
    } else if (blockedLine.inCycle()) {
      entry.setEarliestMoreGeneralLineIndex(-1);
      entry.setEarliestMoreGeneralLineName(
          "This line contains a reference that is part of a circular chain of references.");
    } else if (unmatchable) {
      entry.setEarliestMoreGeneralLineIndex(-1);
      entry.setEarliestMoreGeneralLineName(
          "This line will never match any packet, independent of preceding lines.");
    } else if (blockingLines.isEmpty()) {
      entry.setEarliestMoreGeneralLineIndex(-1);
      entry.setEarliestMoreGeneralLineName(
          "Multiple earlier lines partially block this line, making it unreachable.");
    } else {
      int blockingLineNum = blockingLines.first();
      IpAccessListLine blockingLine = aclSpecs.acl.getOriginal().getLines().get(blockingLineNum);
      entry.setEarliestMoreGeneralLineIndex(blockingLineNum);
      entry.setEarliestMoreGeneralLineName(
          firstNonNull(blockingLine.getName(), blockingLine.toString()));
      entry.setDifferentAction(!blockedLine.getAction().equals(blockingLine.getAction()));
    }

    Pair<String, String> hostnameAndAcl = aclSpecs.getRepresentativeHostnameAclPair();
    addLine(
        _unreachableLines,
        hostnameAndAcl.getFirst(),
        hostnameAndAcl.getSecond(),
        aclSpecs.acl.getOriginal(),
        entry);
  }

  public SortedMap<String, SortedMap<String, IpAccessList>> getAcls() {
    return _acls;
  }

  public SortedMap<String, SortedMap<String, SortedSet<String>>> getEquivalenceClasses() {
    return _equivalenceClasses;
  }

  public SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> getReachableLines() {
    return _reachableLines;
  }

  public SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>>
      getUnreachableLines() {
    return _unreachableLines;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder("Results for unreachable ACL lines\n");
    // private SortedMap<String, SortedMap<String,
    // SortedSet<AclReachabilityEntry>>> _unreachableLines;
    for (String hostname : _unreachableLines.keySet()) {
      for (String aclName : _unreachableLines.get(hostname).keySet()) {
        sb.append("\n  " + hostname + " :: " + aclName + "\n");
        for (AclReachabilityEntry arEntry : _unreachableLines.get(hostname).get(aclName)) {
          sb.append(arEntry.prettyPrint("    "));
        }
      }
    }
    return sb.toString();
  }
  
  public void setEquivalenceClasses(
      SortedMap<String, SortedMap<String, SortedSet<String>>> equivalenceClasses) {
    _equivalenceClasses = equivalenceClasses;
  }

  public void setReachableLines(
      SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> reachableLines) {
    _reachableLines = reachableLines;
  }

  public void setUnreachableLines(
      SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> unreachableLines) {
    _unreachableLines = unreachableLines;
  }

  @JsonIgnore
  public void setAcls(List<AclSpecs> acls) {
    for (AclSpecs specs : acls) {
      Pair<String, String> representativeSource = specs.getRepresentativeHostnameAclPair();
      addEquivalenceClass(
          representativeSource.getSecond(),
          representativeSource.getFirst(),
          ImmutableSortedSet.copyOf(specs.sources.keySet()));
    }
  }
}
