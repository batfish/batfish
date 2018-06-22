package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import org.batfish.datamodel.matchers.LineMatchersImpl.HasAuthenticationLoginList;
import org.batfish.datamodel.matchers.LineMatchersImpl.RequiresAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.batfish.datamodel.vendor_family.cisco.Line;
import org.hamcrest.Matcher;

public final class LineMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link Line}'s
   * {@code AaaAuthenticationLoginList}
   */
  public static HasAuthenticationLoginList hasAuthenticationLoginList(
      Matcher<? super AaaAuthenticationLoginList> subMatcher) {
    return new HasAuthenticationLoginList(subMatcher);
  }

  /** Provides a matcher that matches if the {@link Line} requires authentication */
  public static RequiresAuthentication requiresAuthentication() {
    return new RequiresAuthentication(equalTo(true));
  }

  private LineMatchers() {}
}
