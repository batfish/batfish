package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.Evaluator;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

class AclLineMatchExprMatchersImpl {
  static final class Matches extends TypeSafeDiagnosingMatcher<AclLineMatchExpr> {
    private final Flow _flow;
    private final String _srcInterface;
    private final Map<String, IpAccessList> _availableAcls;

    Matches(
        @Nonnull Flow flow,
        @Nonnull String srcInterface,
        @Nonnull Map<String, IpAccessList> availableAcls) {
      _flow = flow;
      _srcInterface = srcInterface;
      _availableAcls = availableAcls;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format(
              "An AclLineMatchExpr matching flow: %s, srcInterface: %s", _flow, _srcInterface));
    }

    @Override
    protected boolean matchesSafely(AclLineMatchExpr item, Description mismatchDescription) {
      boolean matches = Evaluator.matches(item, _flow, _srcInterface, _availableAcls);
      if (!matches) {
        mismatchDescription.appendText(String.format("did not match and was %s", item));
      }
      return matches;
    }
  }
}
