package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IpSpaceMatchers {

  private IpSpaceMatchers() {}

  /** Provides a matcher that matches if the {@link IpSpace} contains the specified {@link Ip}. */
  public static ContainsIp containsIp(@Nonnull Ip ip) {
    return new ContainsIp(ip, ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches if the {@link IpSpace} contains the specified {@link Ip} given
   * the specified named IpSpace definitions.
   */
  public static ContainsIp containsIp(@Nonnull Ip ip, @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return new ContainsIp(ip, namedIpSpaces);
  }

  public static Intersects intersects(@Nonnull IpWildcard ipWildcard) {
    return new Intersects(ipWildcard);
  }

  public static SubsetOf subsetOf(@Nonnull IpWildcard ipWildcard) {
    return new SubsetOf(ipWildcard);
  }

  public static SupersetOf supersetOf(@Nonnull IpWildcard ipWildcard) {
    return new SupersetOf(ipWildcard);
  }

  private static final class ContainsIp extends TypeSafeDiagnosingMatcher<IpSpace> {
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

  private static final class Intersects extends TypeSafeDiagnosingMatcher<IpWildcard> {
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

  private static final class SubsetOf extends TypeSafeDiagnosingMatcher<IpWildcard> {
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

  private static final class SupersetOf extends TypeSafeDiagnosingMatcher<IpWildcard> {
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
