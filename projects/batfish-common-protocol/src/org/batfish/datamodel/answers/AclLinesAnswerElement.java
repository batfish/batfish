package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.IpAccessList;

public class AclLinesAnswerElement implements AnswerElement {

   private final SortedMap<String, SortedMap<String, IpAccessList>> _acls;

   private final SortedMap<String, SortedMap<String, SortedSet<Integer>>> _reachableLines;

   private final SortedMap<String, SortedMap<String, SortedSet<Integer>>> _unreachableLines;

   public AclLinesAnswerElement() {
      _acls = new TreeMap<String, SortedMap<String, IpAccessList>>();
      _reachableLines = new TreeMap<String, SortedMap<String, SortedSet<Integer>>>();
      _unreachableLines = new TreeMap<String, SortedMap<String, SortedSet<Integer>>>();
   }

   private void addLine(
         SortedMap<String, SortedMap<String, SortedSet<Integer>>> lines,
         String hostname, IpAccessList ipAccessList, int index) {
      String aclName = ipAccessList.getName();
      SortedMap<String, IpAccessList> aclsByHostname = _acls.get(hostname);
      if (aclsByHostname == null) {
         aclsByHostname = new TreeMap<String, IpAccessList>();
         _acls.put(hostname, aclsByHostname);
      }
      if (!aclsByHostname.containsKey(aclName)) {
         aclsByHostname.put(aclName, ipAccessList);
      }
      SortedMap<String, SortedSet<Integer>> linesByHostname = lines
            .get(hostname);
      if (linesByHostname == null) {
         linesByHostname = new TreeMap<String, SortedSet<Integer>>();
         lines.put(hostname, linesByHostname);
      }
      SortedSet<Integer> linesByAcl = linesByHostname.get(aclName);
      if (linesByAcl == null) {
         linesByAcl = new TreeSet<Integer>();
         linesByHostname.put(aclName, linesByAcl);
      }
      linesByAcl.add(index);
   }

   public void addReachableLine(String hostname, IpAccessList ipAccessList,
         int index) {
      addLine(_reachableLines, hostname, ipAccessList, index);
   }

   public void addUnreachableLine(String hostname, IpAccessList ipAccessList,
         int index) {
      addLine(_unreachableLines, hostname, ipAccessList, index);
   }

   public SortedMap<String, SortedMap<String, IpAccessList>> getAcls() {
      return _acls;
   }

   public SortedMap<String, SortedMap<String, SortedSet<Integer>>> getReachableLines() {
      return _reachableLines;
   }

   public SortedMap<String, SortedMap<String, SortedSet<Integer>>> getUnreachableLines() {
      return _unreachableLines;
   }

}
