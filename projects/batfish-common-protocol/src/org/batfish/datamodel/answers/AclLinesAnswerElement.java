package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.IpAccessList;

public class AclLinesAnswerElement implements AnswerElement {

   private final Map<String, Map<String, IpAccessList>> _acls;

   private final Map<String, Map<String, Set<Integer>>> _reachableLines;

   private final Map<String, Map<String, Set<Integer>>> _unreachableLines;

   public AclLinesAnswerElement() {
      _acls = new TreeMap<String, Map<String, IpAccessList>>();
      _reachableLines = new TreeMap<String, Map<String, Set<Integer>>>();
      _unreachableLines = new TreeMap<String, Map<String, Set<Integer>>>();
   }

   private void addLine(Map<String, Map<String, Set<Integer>>> lines,
         String hostname, IpAccessList ipAccessList, int index) {
      String aclName = ipAccessList.getName();
      Map<String, IpAccessList> aclsByHostname = _acls.get(hostname);
      if (aclsByHostname == null) {
         aclsByHostname = new TreeMap<String, IpAccessList>();
         _acls.put(hostname, aclsByHostname);
      }
      if (!aclsByHostname.containsKey(aclName)) {
         aclsByHostname.put(aclName, ipAccessList);
      }
      Map<String, Set<Integer>> linesByHostname = lines.get(hostname);
      if (linesByHostname == null) {
         linesByHostname = new TreeMap<String, Set<Integer>>();
         lines.put(hostname, linesByHostname);
      }
      Set<Integer> linesByAcl = linesByHostname.get(aclName);
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

   public Map<String, Map<String, IpAccessList>> getAcls() {
      return _acls;
   }

   public Map<String, Map<String, Set<Integer>>> getReachableLines() {
      return _reachableLines;
   }

   public Map<String, Map<String, Set<Integer>>> getUnreachableLines() {
      return _unreachableLines;
   }

}
