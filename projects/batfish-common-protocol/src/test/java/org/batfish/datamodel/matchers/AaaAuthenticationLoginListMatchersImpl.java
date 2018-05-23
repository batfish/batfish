package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AaaAuthenticationLoginListMatchersImpl {

  static final class HasMethods extends FeatureMatcher<AaaAuthenticationLoginList, List<String>> {
    HasMethods(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "a login list has methods", "methods");
    }

    @Override
    protected List<String> featureValueOf(AaaAuthenticationLoginList loginList) {
      return loginList.getMethods();
    }
  }
}
