package org.batfish.specifier;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

import java.util.Collection;
import java.util.Set;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IpSpaceAssignmentMatchers {
  public static Matcher<IpSpaceAssignment> hasEntries(
      Matcher<? super Collection<Entry>> entriesMatcher) {
    return new HasEntries(entriesMatcher);
  }

  public static Matcher<IpSpaceAssignment> hasEntry(Matcher<? super Entry> entryMatcher) {
    return hasEntries(hasItem(entryMatcher));
  }

  public static Matcher<Entry> hasIpSpace(Matcher<? super IpSpace> ipSpaceMatcher) {
    return new HasIpSpace(ipSpaceMatcher);
  }

  public static Matcher<Entry> hasLocations(Matcher<? super Set<Location>> locationsMatcher) {
    return new HasLocations(locationsMatcher);
  }

  public static Matcher<IpSpaceAssignment> hasEntry(
      Matcher<? super IpSpace> ipSpaceMatcher, Matcher<? super Set<Location>> locationsMatcher) {
    return hasEntry(allOf(hasIpSpace(ipSpaceMatcher), hasLocations(locationsMatcher)));
  }

  private static final class HasEntries
      extends FeatureMatcher<IpSpaceAssignment, Collection<Entry>> {
    public HasEntries(Matcher<? super Collection<Entry>> subMatcher) {
      super(subMatcher, "entries", "entries");
    }

    @Override
    protected Collection<Entry> featureValueOf(IpSpaceAssignment ipSpaceAssignment) {
      return ipSpaceAssignment.getEntries();
    }
  }

  private static final class HasIpSpace extends FeatureMatcher<Entry, IpSpace> {
    public HasIpSpace(Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "ipSpace", "ipSpace");
    }

    @Override
    protected IpSpace featureValueOf(Entry entry) {
      return entry.getIpSpace();
    }
  }

  private static final class HasLocations extends FeatureMatcher<Entry, Set<Location>> {
    public HasLocations(Matcher<? super Set<Location>> subMatcher) {
      super(subMatcher, "locations", "locations");
    }

    @Override
    protected Set<Location> featureValueOf(Entry entry) {
      return entry.getLocations();
    }
  }
}
