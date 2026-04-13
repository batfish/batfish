package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.Evaluator;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

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

  private static final class Matches extends TypeSafeDiagnosingMatcher<AclLineMatchExpr> {
    private final Flow _flow;
    private final String _srcInterface;
    private final Map<String, IpAccessList> _availableAcls;
    private final Map<String, IpSpace> _namedIpSpaces;

    Matches(
        @Nonnull Flow flow,
        @Nonnull String srcInterface,
        @Nonnull Map<String, IpAccessList> availableAcls,
        @Nonnull Map<String, IpSpace> namedIpSpaces) {
      _flow = flow;
      _srcInterface = srcInterface;
      _availableAcls = availableAcls;
      _namedIpSpaces = namedIpSpaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "An AclLineMatchExpr matching flow: %s, srcInterface: %s", _flow, _srcInterface));
    }

    @Override
    protected boolean matchesSafely(AclLineMatchExpr item, Description mismatchDescription) {
      boolean matches =
          Evaluator.matches(item, _flow, _srcInterface, _availableAcls, _namedIpSpaces);
      if (!matches) {
        mismatchDescription.appendText(String.format("did not match and was %s", item));
      }
      return matches;
    }
  }
}
