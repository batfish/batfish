package org.batfish.datamodel.matchers;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class IpAccessListMatchersImpl {

  static class Accepts extends TypeSafeDiagnosingMatcher<IpAccessList> {

    private final Map<String, IpAccessList> _availableAcls;
    private final Flow _flow;
    private final String _srcInterface;
    private final Map<String, IpSpace> _namedIpSpaces;

    Accepts(
        @Nonnull Flow flow,
        @Nullable String srcInterface,
        @Nonnull Map<String, IpAccessList> availableAcls,
        @Nonnull Map<String, IpSpace> namedIpSpaces) {
      _flow = flow;
      _srcInterface = srcInterface;
      _availableAcls = availableAcls;
      _namedIpSpaces = namedIpSpaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpAccessList that accepts flow: '%s'", _flow));
      if (_srcInterface != null) {
        description.appendText(String.format("at srcInterface: %s", _srcInterface));
      }
    }

    @Override
    protected boolean matchesSafely(IpAccessList item, Description mismatchDescription) {
      if (item.filter(_flow, _srcInterface, _availableAcls, _namedIpSpaces).getAction()
          != LineAction.PERMIT) {
        mismatchDescription.appendText(String.format("does not accept flow '%s'", _flow));
        if (_srcInterface != null) {
          mismatchDescription.appendText(String.format("at source interface: %s", _srcInterface));
        }
        return false;
      }
      return true;
    }
  }

  static class HasLines extends FeatureMatcher<IpAccessList, List<AclLine>> {

    public HasLines(@Nonnull Matcher<? super List<AclLine>> subMatcher) {
      super(subMatcher, "An IpAccessList with lines:", "lines");
    }

    @Override
    protected List<AclLine> featureValueOf(IpAccessList actual) {
      return actual.getLines();
    }
  }

  static final class HasName extends FeatureMatcher<IpAccessList, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IpAccessList with Name:", "Name");
    }

    @Override
    protected String featureValueOf(IpAccessList actual) {
      return actual.getName();
    }
  }

  static class Rejects extends TypeSafeDiagnosingMatcher<IpAccessList> {

    private final Map<String, IpAccessList> _availableAcls;
    private final Flow _flow;
    private final String _srcInterface;
    private final Map<String, IpSpace> _namedIpSpaces;

    Rejects(
        @Nonnull Flow flow,
        @Nullable String srcInterface,
        @Nonnull Map<String, IpAccessList> availableAcls,
        @Nonnull Map<String, IpSpace> namedIpSpaces) {
      _flow = flow;
      _srcInterface = srcInterface;
      _availableAcls = availableAcls;
      _namedIpSpaces = namedIpSpaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("An IpAccessList that rejects flow: '%s'", _flow));
      if (_srcInterface != null) {
        description.appendText(String.format("at srcInterface: %s", _srcInterface));
      }
    }

    @Override
    protected boolean matchesSafely(IpAccessList item, Description mismatchDescription) {
      if (item.filter(_flow, _srcInterface, _availableAcls, _namedIpSpaces).getAction()
          != LineAction.DENY) {
        mismatchDescription.appendText(String.format("does not reject flow '%s'", _flow));
        if (_srcInterface != null) {
          mismatchDescription.appendText(String.format("at source interface: %s", _srcInterface));
        }
        return false;
      }
      return true;
    }
  }

  static class RejectsByDefault extends TypeSafeDiagnosingMatcher<IpAccessList> {

    private final Map<String, IpAccessList> _availableAcls;
    private final Flow _flow;
    private final String _srcInterface;
    private final Map<String, IpSpace> _namedIpSpaces;

    RejectsByDefault(
        @Nonnull Flow flow,
        @Nullable String srcInterface,
        @Nonnull Map<String, IpAccessList> availableAcls,
        @Nonnull Map<String, IpSpace> namedIpSpaces) {
      _flow = flow;
      _srcInterface = srcInterface;
      _availableAcls = availableAcls;
      _namedIpSpaces = namedIpSpaces;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format("An IpAccessList that rejects by default flow: '%s'", _flow));
      if (_srcInterface != null) {
        description.appendText(String.format("at srcInterface: %s", _srcInterface));
      }
    }

    @Override
    protected boolean matchesSafely(IpAccessList item, Description mismatchDescription) {
      FilterResult result = item.filter(_flow, _srcInterface, _availableAcls, _namedIpSpaces);
      if (result.getAction() != LineAction.DENY) {
        mismatchDescription.appendText(String.format("does not reject flow '%s'", _flow));
        if (_srcInterface != null) {
          mismatchDescription.appendText(String.format("at source interface: %s", _srcInterface));
        }
        return false;
      } else if (result.getMatchLine() != null) {
        mismatchDescription.appendText(
            String.format("does not reject by default flow '%s'", _flow));
        if (_srcInterface != null) {
          mismatchDescription.appendText(String.format("at source interface: %s", _srcInterface));
        }
        return false;
      }
      return true;
    }
  }

  private IpAccessListMatchersImpl() {}
}
