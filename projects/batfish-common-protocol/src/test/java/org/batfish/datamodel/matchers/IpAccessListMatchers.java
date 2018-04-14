package org.batfish.datamodel.matchers;

import java.util.List;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.matchers.IpAccessListMatchersImpl.HasLines;
import org.hamcrest.Matcher;

public class IpAccessListMatchers {

  private IpAccessListMatchers() {}

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IpAccessList's
   * lines.
   */
  public static HasLines hasLines(Matcher<? super List<IpAccessListLine>> subMatcher) {
    return new HasLines(subMatcher);
  }
}
