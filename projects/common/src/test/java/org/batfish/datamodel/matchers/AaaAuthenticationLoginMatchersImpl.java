package org.batfish.datamodel.matchers;

import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLogin;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AaaAuthenticationLoginMatchersImpl {

  static final class HasLists
      extends FeatureMatcher<
          AaaAuthenticationLogin, SortedMap<String, AaaAuthenticationLoginList>> {
    HasLists(@Nonnull Matcher<? super SortedMap<String, AaaAuthenticationLoginList>> subMatcher) {
      super(subMatcher, "a AaaAuthenticationLogin with method lists", "lists");
    }

    @Override
    protected SortedMap<String, AaaAuthenticationLoginList> featureValueOf(
        AaaAuthenticationLogin actual) {
      return actual.getLists();
    }
  }

  static final class HasListForKey
      extends FeatureMatcher<AaaAuthenticationLogin, AaaAuthenticationLoginList> {
    private String _key;

    HasListForKey(@Nonnull Matcher<? super AaaAuthenticationLoginList> subMatcher, String key) {
      super(
          subMatcher,
          String.format("a AaaAuthenticationLogin with list for key '%s'", key),
          String.format("list for key '%s'", key));
      _key = key;
    }

    @Override
    protected AaaAuthenticationLoginList featureValueOf(AaaAuthenticationLogin actual) {
      return actual.getLists().get(_key);
    }
  }
}
