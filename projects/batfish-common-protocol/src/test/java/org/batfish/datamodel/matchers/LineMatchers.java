package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import org.batfish.datamodel.matchers.LineMatchersImpl.RequiresAuthentication;

public class LineMatchers {

  public static RequiresAuthentication requiresAuthentication() {
    return new RequiresAuthentication(equalTo(true));
  }
}
