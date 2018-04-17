package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.IpAccessList;

public class AclLinesAnswerElement extends AnswerElement {

  public static class AclReachabilityEntry implements Comparable<AclReachabilityEntry> {

    private static final String PROP_INDEX = "index";

    private static final String PROP_NAME = "name";

    private boolean _differentAction;

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

    public Integer getEarliestMoreGeneralLineIndex() {
      return _earliestMoreGeneralLineIndex;
    }

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

  public void addEquivalenceClass(String aclName, String hostname, SortedSet<String> eqClassNodes) {
    SortedMap<String, SortedSet<String>> byRep =
        _equivalenceClasses.computeIfAbsent(aclName, k -> new TreeMap<>());
    byRep.put(hostname, eqClassNodes);
  }

  private void addLine(
      SortedMap<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> lines,
      String hostname,
      IpAccessList ipAccessList,
      AclReachabilityEntry entry) {
    String aclName = ipAccessList.getName();
    SortedMap<String, IpAccessList> aclsByHostname =
        _acls.computeIfAbsent(hostname, k -> new TreeMap<>());
    if (!aclsByHostname.containsKey(aclName)) {
      aclsByHostname.put(aclName, ipAccessList);
    }
    SortedMap<String, SortedSet<AclReachabilityEntry>> linesByHostname =
        lines.computeIfAbsent(hostname, k -> new TreeMap<>());
    SortedSet<AclReachabilityEntry> linesByAcl =
        linesByHostname.computeIfAbsent(aclName, k -> new TreeSet<>());
    linesByAcl.add(entry);
  }

  public void addReachableLine(
      String hostname, IpAccessList ipAccessList, AclReachabilityEntry entry) {
    addLine(_reachableLines, hostname, ipAccessList, entry);
  }

  public void addUnreachableLine(
      String hostname, IpAccessList ipAccessList, AclReachabilityEntry entry) {
    addLine(_unreachableLines, hostname, ipAccessList, entry);
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

  public void setAcls(SortedMap<String, SortedMap<String, IpAccessList>> acls) {
    _acls = acls;
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
}
