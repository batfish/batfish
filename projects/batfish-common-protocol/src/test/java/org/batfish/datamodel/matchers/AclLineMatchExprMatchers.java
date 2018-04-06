package org.batfish.datamodel.matchers;

import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.matchers.AclLineMatchExprMatchersImpl.Matches;

public class AclLineMatchExprMatchers {

  /**
   * Provides a matcher that matches when the {@link AclLineMatchExpr} matches the provided inputs.
   */
  public static Matches matches(
      Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return new Matches(flow, srcInterface, availableAcls);
  }
}
