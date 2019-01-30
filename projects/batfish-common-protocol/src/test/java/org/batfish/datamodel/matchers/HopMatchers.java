package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.matchers.HopMatchersImpl.HasNodeName;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Hop}. */
public final class HopMatchers {
  public static HasNodeName hasNodeName(String nodeName) {
    return new HasNodeName(equalTo(nodeName));
  }
}
