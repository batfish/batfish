package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.OspfArea;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class OspfAreaMatchersImpl {

  static final class HasInterfaces extends FeatureMatcher<OspfArea, Map<String, Interface>> {
    HasInterfaces(@Nonnull Matcher<? super Map<String, Interface>> subMatcher) {
      super(subMatcher, "an OspfArea with interfaces:", "interfaces");
    }

    @Override
    protected Map<String, Interface> featureValueOf(OspfArea actual) {
      return actual.getInterfaces();
    }
  }
}
