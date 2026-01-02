package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.matchers.AclLineMatchExprMatchersImpl.Matches;

public class AclLineMatchExprMatchers {

  /**
   * Provides a matcher that matches when the {@link AclLineMatchExpr} matches the provided inputs
   * and does not refer to named structures.
   */
  public static Matches matches(Flow flow, String srcInterface) {
    return new Matches(flow, srcInterface, ImmutableMap.of(), ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches when the {@link AclLineMatchExpr} matches the provided inputs
   * and does not refer to named {@link IpSpace}s.
   */
  public static Matches matches(
      Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return new Matches(flow, srcInterface, availableAcls, ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches when the {@link AclLineMatchExpr} matches the provided inputs.
   */
  public static Matches matches(
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    return new Matches(flow, srcInterface, availableAcls, namedIpSpaces);
  }

  private AclLineMatchExprMatchers() {}
}
