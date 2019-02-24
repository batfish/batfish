package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.matchers.FibEntryMatchersImpl.HasInterface;

/** Matchers for {@link FibEntry} */
public final class FibEntryMatchers {
  public static HasInterface hasInterface(String interfaceName) {
    return new HasInterface(equalTo(interfaceName));
  }

  private FibEntryMatchers() {}
}
