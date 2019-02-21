package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IpSpaceMatchersImpl {

  private IpSpaceMatchersImpl() {}

  static final class ContainsIp extends TypeSafeDiagnosingMatcher<IpSpace> {
    private final Ip _ip;
    private final Map<String, IpSpace> _namedIpSpaces;

    ContainsIp(@Nonnull Ip ip, @Nonnull Map<String, IpSpace> namedIpSpaces) {
      _ip = ip;
      _namedIpSpaces = namedIpSpaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpSpace containing IP: %s", _ip));
    }

    @Override
    protected boolean matchesSafely(IpSpace item, Description mismatchDescription) {
      boolean matches = item.containsIp(_ip, _namedIpSpaces);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", item));
      }
      return matches;
    }
  }

  static final class Intersects extends TypeSafeDiagnosingMatcher<IpWildcard> {
    private final IpWildcard _ipWildcard;

    Intersects(IpWildcard ipWildcard) {
      _ipWildcard = ipWildcard;
    }

    @Override
    protected boolean matchesSafely(IpWildcard ipWildcard, Description mismatchDescription) {
      boolean matches = ipWildcard.intersects(_ipWildcard);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", ipWildcard));
      }
      return matches;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpWildcard that intersects %s", _ipWildcard));
    }
  }

  static final class SubsetOf extends TypeSafeDiagnosingMatcher<IpWildcard> {
    private final IpWildcard _ipWildcard;

    SubsetOf(IpWildcard ipWildcard) {
      _ipWildcard = ipWildcard;
    }

    @Override
    protected boolean matchesSafely(IpWildcard ipWildcard, Description mismatchDescription) {
      boolean matches = ipWildcard.subsetOf(_ipWildcard);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", ipWildcard));
      }
      return matches;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpWildcard that is a subset of %s", _ipWildcard));
    }
  }

  static final class SupersetOf extends TypeSafeDiagnosingMatcher<IpWildcard> {
    private final IpWildcard _ipWildcard;

    SupersetOf(IpWildcard ipWildcard) {
      _ipWildcard = ipWildcard;
    }

    @Override
    protected boolean matchesSafely(IpWildcard ipWildcard, Description mismatchDescription) {
      boolean matches = ipWildcard.supersetOf(_ipWildcard);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", ipWildcard));
      }
      return matches;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpWildcard that is a superset of %s", _ipWildcard));
    }
  }
}
