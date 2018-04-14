package org.batfish.datamodel.matchers;

import java.util.List;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

class IpAccessListMatchersImpl {

  static class HasLines extends FeatureMatcher<IpAccessList, List<IpAccessListLine>> {

    public HasLines(Matcher<? super List<IpAccessListLine>> subMatcher) {
      super(subMatcher, "An IpAcessList with lines:", "lines");
    }

    @Override
    protected List<IpAccessListLine> featureValueOf(IpAccessList actual) {
      return actual.getLines();
    }
  }

  private IpAccessListMatchersImpl() {}
}
