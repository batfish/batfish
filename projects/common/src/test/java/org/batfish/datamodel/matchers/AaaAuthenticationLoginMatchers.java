package org.batfish.datamodel.matchers;

import java.util.SortedMap;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.matchers.AaaAuthenticationLoginMatchersImpl.HasListForKey;
import org.batfish.datamodel.matchers.AaaAuthenticationLoginMatchersImpl.HasLists;
import org.hamcrest.Matcher;

public class AaaAuthenticationLoginMatchers {

  public static HasLists hasLists(
      Matcher<? super SortedMap<String, AaaAuthenticationLoginList>> subMatcher) {
    return new HasLists(subMatcher);
  }

  public static HasListForKey hasListForKey(
      Matcher<? super AaaAuthenticationLoginList> subMatcher, String key) {
    return new HasListForKey(subMatcher, key);
  }
}
