package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfProcess;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfProcessMatchersImpl {

  static final class HasAreas extends FeatureMatcher<OspfProcess, Map<Long, OspfArea>> {
    HasAreas(@Nonnull Matcher<? super Map<Long, OspfArea>> subMatcher) {
      super(subMatcher, "areas", "areas");
    }

    @Override
    protected Map<Long, OspfArea> featureValueOf(OspfProcess actual) {
      return actual.getAreas();
    }
  }
}
