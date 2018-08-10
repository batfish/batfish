package org.batfish.specifier;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

import java.util.Collection;
import java.util.Set;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.batfish.specifier.IpSpaceAssignmentMatchersImpl.HasIpSpace;
import org.batfish.specifier.IpSpaceAssignmentMatchersImpl.HasLocations;
import org.hamcrest.Matcher;

public final class IpSpaceAssignmentMatchers {
  public static Matcher<IpSpaceAssignment> hasEntries(
      Matcher<? super Collection<Entry>> entriesMatcher) {
    return new IpSpaceAssignmentMatchersImpl.HasEntries(entriesMatcher);
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
}
