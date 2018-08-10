package org.batfish.dataplane.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.ibdp.PrefixTracer;
import org.batfish.dataplane.ibdp.PrefixTracer.Neighbor;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.FilteredIn;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.FilteredOut;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.HasHostname;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.HasIp;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.WasInstalled;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.WasOriginated;
import org.batfish.dataplane.matchers.PrefixTracerMatchersImpl.WasSent;
import org.hamcrest.Matcher;

/** A set of matchers for testing {@link PrefixTracer} */
public final class PrefixTracerMatchers {

  /** Matches if a matched by the submatcher prefix was sent */
  public static WasSent wasSent(Matcher<? super Map<Prefix, Set<Neighbor>>> submatcher) {
    return new WasSent(submatcher);
  }

  /** Matches if a prefix was received from neighbors specified by the submatcher and installed */
  public static WasInstalled wasInstalled(
      Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new WasInstalled(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a prefix was considered for export */
  public static WasOriginated wasOriginated(Prefix prefix) {
    return new WasOriginated(hasItem(prefix));
  }

  /** Matches if a prefix was sent to any neighbors specified by the submatcher */
  public static WasSent wasSent(Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new WasSent(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a prefix was filtered on inbound at a neighbor specified by the submatcher */
  public static FilteredIn wasFilteredIn(Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new FilteredIn(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a prefix was filtered on outbound at a neighbor specified by the submatcher */
  public static FilteredOut wasFilteredOut(
      Prefix prefix, Matcher<? super Set<Neighbor>> submatcher) {
    return new FilteredOut(hasEntry(equalTo(prefix), submatcher));
  }

  /** Matches if a neighbor has a specified hostname */
  public static HasHostname hasHostname(String hostname) {
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
}
