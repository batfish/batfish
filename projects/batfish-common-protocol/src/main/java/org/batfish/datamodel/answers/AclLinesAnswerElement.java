package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;

/**
 * Represents answers to aclReachability. Implements {@link AclLinesAnswerElementInterface} so that
 * aclReachability and aclReachability2 can use some of the same methods to answer both.
 */
public class AclLinesAnswerElement extends AnswerElement implements AclLinesAnswerElementInterface {

  private static final String PROP_UNREACHABLE_LINES = "unreachableLines";

  public static class AclReachabilityEntry implements Comparable<AclReachabilityEntry> {

    private static final String PROP_DIFFERENT_ACTION = "differentAction";

    private static final String PROP_EARLIEST_MORE_GENERAL_LINE_INDEX =
        "earliestMoreGeneralLineIndex";

    private static final String PROP_EARLIEST_MORE_GENERAL_LINE_NAME =
        "earliestMoreGeneralLineName";

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

    @JsonProperty(PROP_DIFFERENT_ACTION)
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
    @JsonProperty(PROP_EARLIEST_MORE_GENERAL_LINE_INDEX)
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
    @JsonProperty(PROP_EARLIEST_MORE_GENERAL_LINE_NAME)
    public String getEarliestMoreGeneralLineName() {
      return _earliestMoreGeneralLineName;
    }

    @JsonProperty(PROP_INDEX)
    public int getIndex() {
      return _index;
    }

    @JsonProperty(PROP_NAME)
    public String getName() {
      return _name;
    }

    @Override
    public int hashCode() {
      return _name.hashCode();
    }

    public String prettyPrint(String indent) {
      String sb =
          String.format("%s[index %d] %s\n", indent, _index, _name)
              + String.format(
                  "%s  Earliest covering line: [index %d] %s\n",
                  indent, _earliestMoreGeneralLineIndex, _earliestMoreGeneralLineName)
              + String.format("%s  Is different action: %s\n", indent, _differentAction);
      return sb;
    }

    @JsonProperty(PROP_DIFFERENT_ACTION)
    public void setDifferentAction(boolean differentAction) {
      _differentAction = differentAction;
    }

    @JsonProperty(PROP_EARLIEST_MORE_GENERAL_LINE_INDEX)
    public void setEarliestMoreGeneralLineIndex(Integer earliestMoreGeneralLineIndex) {
      _earliestMoreGeneralLineIndex = earliestMoreGeneralLineIndex;
    }

    @JsonProperty(PROP_EARLIEST_MORE_GENERAL_LINE_NAME)
    public void setEarliestMoreGeneralLineName(String earliestMoreGeneralLineName) {
      _earliestMoreGeneralLineName = earliestMoreGeneralLineName;
    }
  }

  private SortedMap<String, SortedMap<String, IpAccessList>> _acls;

  private SortedMap<String, SortedMap<String, SortedSet<String>>> _equivalenceClasses;

  private SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> _reachableLines;

  private SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> _unreachableLines;

  public AclLinesAnswerElement() {
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

  @Override
  public void addUnreachableLine(
      AclSpecs aclSpecs, int lineNumber, boolean unmatchable, SortedSet<Integer> blockingLines) {
    IpAccessListLine line = aclSpecs.acl.getOriginalAcl().getLines().get(lineNumber);
    AclReachabilityEntry entry =
        new AclReachabilityEntry(lineNumber, firstNonNull(line.getName(), line.toString()));

    if (aclSpecs.acl.hasUndefinedRef(lineNumber)) {
      entry.setEarliestMoreGeneralLineIndex(-1);
      entry.setEarliestMoreGeneralLineName(
          "This line will never match any packet because it references an undefined structure.");
    } else if (aclSpecs.acl.inCycle(lineNumber)) {
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
      IpAccessListLine blockingLine = aclSpecs.acl.getOriginalAcl().getLines().get(blockingLineNum);
      entry.setEarliestMoreGeneralLineIndex(blockingLineNum);
      entry.setEarliestMoreGeneralLineName(
          firstNonNull(blockingLine.getName(), blockingLine.toString()));
      entry.setDifferentAction(!line.getAction().equals(blockingLine.getAction()));
    }

    _unreachableLines
        .computeIfAbsent(aclSpecs.reprHostname, k -> new TreeMap<>())
        .computeIfAbsent(aclSpecs.acl.getAclName(), k -> new TreeSet<>())
        .add(entry);
  }

  @Deprecated
  public SortedMap<String, SortedMap<String, IpAccessList>> getAcls() {
    return _acls;
  }

  @Deprecated
  public SortedMap<String, SortedMap<String, SortedSet<String>>> getEquivalenceClasses() {
    return _equivalenceClasses;
  }

  @Deprecated
  public SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> getReachableLines() {
    return _reachableLines;
  }

  @JsonProperty(PROP_UNREACHABLE_LINES)
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

  @Deprecated
  public void setAcls(SortedMap<String, SortedMap<String, IpAccessList>> acls) {}

  @Deprecated
  public void setEquivalenceClasses(
      SortedMap<String, SortedMap<String, SortedSet<String>>> equivalenceClasses) {}

  @Deprecated
  public void setReachableLines(
      SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> reachableLines) {}

  @JsonProperty(PROP_UNREACHABLE_LINES)
  public void setUnreachableLines(
      SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> unreachableLines) {
    _unreachableLines = unreachableLines;
  }
}
