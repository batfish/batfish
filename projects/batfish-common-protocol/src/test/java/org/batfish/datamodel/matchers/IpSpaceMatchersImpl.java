package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IpSpaceMatchersImpl {

  private IpSpaceMatchersImpl() {}

  static final class ContainsIp extends TypeSafeDiagnosingMatcher<IpSpace> {
    private final Ip _ip;

    ContainsIp(@Nonnull Ip ip) {
      _ip = ip;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpSpace containing IP: %s", _ip));
    }

    @Override
    protected boolean matchesSafely(IpSpace item, Description mismatchDescription) {
      boolean matches = item.contains(_ip);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", item));
      }
      return matches;
    }
  }
}
