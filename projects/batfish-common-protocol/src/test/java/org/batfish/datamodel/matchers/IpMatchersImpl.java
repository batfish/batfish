package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IpMatchersImpl {

  private IpMatchersImpl() {}

  static final class ContainedBy extends TypeSafeDiagnosingMatcher<Ip> {
    private final IpSpace _ipSpace;
    private final Map<String, IpSpace> _namedIpSpaces;

    ContainedBy(@Nonnull IpSpace ipSpace, @Nonnull Map<String, IpSpace> namedIpSpaces) {
      _ipSpace = ipSpace;
      _namedIpSpaces = namedIpSpaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An Ip contained by IpSpace: %s", _ipSpace));
    }

    @Override
    protected boolean matchesSafely(Ip item, Description mismatchDescription) {
      boolean matches = _ipSpace.containsIp(item, _namedIpSpaces);
      if (!matches) {
        mismatchDescription.appendText(String.format("was %s", item));
      }
      return matches;
    }
  }
}
