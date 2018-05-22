package org.batfish.datamodel.answers;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.IpAccessList;

public interface AclLinesAnswerElementInterface {

  class AclSpecs {
    public List<String> lines;
    public SortedSet<String> nodes;

    public AclSpecs(List<String> lines, SortedSet<String> nodes) {
      this.lines = lines;
      this.nodes = nodes;
    }
  }

  void addEquivalenceClass(
      String aclName, String hostname, SortedSet<String> eqClassNodes, List<String> aclLines);

  void addReachableLine(String hostname, IpAccessList ipAccessList, int lineNumber, String line);

  void addUnreachableLine(
      String hostname,
      IpAccessList acl,
      int lineNumber,
      String line,
      boolean unmatchable,
      SortedMap<Integer, String> blockingLines,
      boolean diffAction);
}
