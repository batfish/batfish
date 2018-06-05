package org.batfish.datamodel.answers;

import java.util.List;
import java.util.SortedMap;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.CanonicalAcl;

@ParametersAreNonnullByDefault
public interface AclLinesAnswerElementInterface {

  void addReachableLine(String hostname, IpAccessList ipAccessList, int lineNumber, String line);

  void addUnreachableLine(
      String hostname,
      IpAccessList acl,
      int lineNumber,
      String line,
      boolean unmatchable,
      SortedMap<Integer, String> blockingLines,
      boolean diffAction,
      boolean undefinedReference,
      boolean cycle);

  void addCycle(String hostname, List<String> aclsInCycle);

  void setCanonicalAcls(List<CanonicalAcl> acls);
}
