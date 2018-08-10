package org.batfish.dataplane.matchers;

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

/** Implementations of {@link PrefixTracerMatchers} */
final class PrefixTracerMatchersImpl {

  static final class WasOriginated extends FeatureMatcher<PrefixTracer, SortedSet<Prefix>> {

    WasOriginated(Matcher<? super SortedSet<Prefix>> subMatcher) {
      super(subMatcher, "Originated:", "originated");
    }

    @Override
    protected SortedSet<Prefix> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getOriginated();
    }
  }

  static final class FilteredOut extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {

    FilteredOut(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Filtered outbound:", "filtered routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getFiltered(Direction.OUT);
    }
  }

  static final class FilteredIn extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {

    FilteredIn(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Filtered inbound:", "filtered routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getFiltered(Direction.IN);
    }
  }

  static final class WasSent extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {
    WasSent(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Sent routes:", "sent routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getSent();
    }
  }

  static final class WasInstalled extends FeatureMatcher<PrefixTracer, Map<Prefix, Set<Neighbor>>> {

    WasInstalled(Matcher<? super Map<Prefix, Set<Neighbor>>> subMatcher) {
      super(subMatcher, "Installed routes:", "installed routes");
    }

    @Override
    protected Map<Prefix, Set<Neighbor>> featureValueOf(PrefixTracer prefixTracer) {
      return prefixTracer.getInstalled();
    }
  }

  static final class HasHostname extends FeatureMatcher<Neighbor, String> {

    HasHostname(Matcher<? super String> subMatcher) {
      super(subMatcher, "Neighbor with hostname:", "hostname");
    }

    @Override
    protected String featureValueOf(Neighbor neighbor) {
      return neighbor.getHostname();
    }
  }

  static final class HasIp extends FeatureMatcher<Neighbor, Ip> {
    HasIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "Neighbor with Ip:", "ip");
    }

    @Override
    protected Ip featureValueOf(Neighbor neighbor) {
      return neighbor.getIp();
    }
  }
}
