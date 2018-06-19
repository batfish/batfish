package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import org.batfish.datamodel.matchers.LineMatchersImpl.HasAuthenticationLoginList;
import org.batfish.datamodel.matchers.LineMatchersImpl.RequiresAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.hamcrest.Matcher;

public class LineMatchers {

  public static HasAuthenticationLoginList hasAuthenticationLoginList(Matcher<? super AaaAuthenticationLoginList> subMatcher) {
    return new HasAuthenticationLoginList(subMatcher);
  }

  public static RequiresAuthentication requiresAuthentication() {
    return new RequiresAuthentication(equalTo(true));
  }
}
