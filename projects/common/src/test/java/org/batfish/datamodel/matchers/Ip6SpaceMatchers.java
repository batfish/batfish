package org.batfish.datamodel.matchers;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6Space;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Ip6SpaceMatchers {
  private Ip6SpaceMatchers() {}

  /** Provides a matcher that matches if the {@link Ip6Space} contains the specified {@link Ip6}. */
  public static ContainsIp containsIp6(@Nonnull Ip6 ip6) {
    return new ContainsIp(ip6, ImmutableMap.of());
  }

  /**
   * Provides a matcher that matches if the {@link Ip6Space} contains the specified {@link Ip6}
   * given the specified named Ip6Space definitions.
   */
  public static ContainsIp containsIp6(
      @Nonnull Ip6 ip6, @Nonnull Map<String, Ip6Space> namedIp6Spaces) {
    return new ContainsIp(ip6, namedIp6Spaces);
  }

  private static final class ContainsIp extends TypeSafeDiagnosingMatcher<Ip6Space> {
    private final Ip6 _ip6;
    private final Map<String, Ip6Space> _namedIp6Spaces;

    ContainsIp(@Nonnull Ip6 ip6, @Nonnull Map<String, Ip6Space> namedIp6Spaces) {
      _ip6 = ip6;
      _namedIp6Spaces = namedIp6Spaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An Ip6Space containing IP6: %s", _ip6));
    }

    @Override
    protected boolean matchesSafely(Ip6Space item, Description mismatchDescription) {
      boolean matches = item.containsIp6(_ip6, _namedIp6Spaces);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", item));
      }
      return matches;
    }
  }
}
