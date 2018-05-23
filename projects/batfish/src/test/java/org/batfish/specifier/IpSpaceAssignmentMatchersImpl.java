package org.batfish.specifier;

import java.util.Collection;
import java.util.Set;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IpSpaceAssignmentMatchersImpl {
  public static class HasEntries extends FeatureMatcher<IpSpaceAssignment, Collection<Entry>> {
    public HasEntries(Matcher<? super Collection<Entry>> subMatcher) {
      super(subMatcher, "entries", "entries");
    }

    @Override
    protected Collection<Entry> featureValueOf(IpSpaceAssignment ipSpaceAssignment) {
      return ipSpaceAssignment.getEntries();
    }
  }

  public static class HasIpSpace extends FeatureMatcher<Entry, IpSpace> {
    public HasIpSpace(Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "ipSpace", "ipSpace");
    }

    @Override
    protected IpSpace featureValueOf(Entry entry) {
      return entry.getIpSpace();
    }
  }

  public static class HasLocations extends FeatureMatcher<Entry, Set<Location>> {
    public HasLocations(Matcher<? super Set<Location>> subMatcher) {
      super(subMatcher, "locations", "locations");
    }

    @Override
    protected Set<Location> featureValueOf(Entry entry) {
      return entry.getLocations();
    }
  }
}
