package org.batfish.dataplane.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.dataplane.ibdp.PrefixTracer;
import org.batfish.dataplane.ibdp.PrefixTracer.Neighbor;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** A set of matchers for testing {@link PrefixTracer} */
public final class PrefixTracerMatchers {

  /** Matches if a matched by the submatcher prefix was sent */
  public static Matcher<PrefixTracer> wasSent(
      Matcher<? super Map<Prefix, Set<Neighbor>>> submatcher) {
    return new WasSent(submatcher);
  }

  /** Matches if a prefix was received from neighbors specified by the submatcher and installed */
  public static Matcher<PrefixTracer> wasInstalled(
      Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new WasInstalled(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a prefix was considered for export */
  public static Matcher<PrefixTracer> wasOriginated(Prefix prefix) {
    return new WasOriginated(hasItem(prefix));
  }

  /** Matches if a prefix was sent to any neighbors specified by the submatcher */
  public static Matcher<PrefixTracer> wasSent(
      Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new WasSent(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a prefix was filtered on inbound at a neighbor specified by the submatcher */
  public static Matcher<PrefixTracer> wasFilteredIn(
      Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new FilteredIn(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a prefix was filtered on outbound at a neighbor specified by the submatcher */
  public static Matcher<PrefixTracer> wasFilteredOut(
      Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new FilteredOut(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a neighbor has a specified hostname */
  public static Matcher<Neighbor> hasHostname(String hostname) {
    return new HasHostname(equalTo(hostname));
  }

  /** Matches if any of the neighbors have a specified hostname */
  public static Matcher<? super Iterable<Neighbor>> toHostname(String hostname) {
    return hasItem(new HasHostname(equalTo(hostname)));
  }

  /** Matches if any of the neighbors have a specified ip */
  public static Matcher<? super Iterable<Neighbor>> toIp(Ip ip) {
    return hasItem(new HasIp(equalTo(ip)));
  }

  /** Matches if any of the neighbors have a specified ip */
  public static Matcher<? super Iterable<Neighbor>> fromIp(Ip ip) {
    return hasItem(new HasIp(equalTo(ip)));
  }

  /** Matches if any of the neighbors have a specified hostname */
  public static Matcher<? super Iterable<Neighbor>> fromHostname(String hostname) {
    return hasItem(new HasHostname(equalTo(hostname)));
  }

  private static final class WasOriginated extends FeatureMatcher<PrefixTracer, SortedSet<Prefix>> {

    WasOriginated(Matcher<? super SortedSet<Prefix>> subMatcher) {
      super(subMatcher, "Originated:", "originated");
    }

    @Override
    protected SortedSet<Prefix> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getOriginated();
    }
  }

  private static final class FilteredOut
      extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {

    FilteredOut(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Filtered outbound:", "filtered routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getFiltered(Direction.OUT);
    }
  }

  private static final class FilteredIn
      extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {

    FilteredIn(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Filtered inbound:", "filtered routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getFiltered(Direction.IN);
    }
  }

  private static final class WasSent
      extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {
    WasSent(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Sent routes:", "sent routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getSent();
    }
  }

  private static final class WasInstalled
      extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {

    WasInstalled(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Installed routes:", "installed routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getInstalled();
    }
  }

  private static final class HasHostname extends FeatureMatcher<Neighbor, String> {

    HasHostname(Matcher<? super String> subMatcher) {
      super(subMatcher, "Neighbor with hostname:", "hostname");
    }

    @Override
    protected String featureValueOf(Neighbor neighbor) {
      return neighbor.getHostname();
    }
  }

  private static final class HasIp extends FeatureMatcher<Neighbor, Ip> {
    HasIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "Neighbor with Ip:", "ip");
    }

    @Override
    protected Ip featureValueOf(Neighbor neighbor) {
      return neighbor.getIp();
    }
  }
}
