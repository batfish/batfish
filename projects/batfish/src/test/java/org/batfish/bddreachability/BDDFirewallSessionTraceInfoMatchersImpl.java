package org.batfish.bddreachability;

import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class BDDFirewallSessionTraceInfoMatchersImpl {
  static final class HasHostname extends FeatureMatcher<BDDFirewallSessionTraceInfo, String> {

    public HasHostname(Matcher<? super String> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with hostname:", "hostname");
    }

    @Override
    protected String featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getHostname();
    }
  }

  static final class HasIncomingInterfaces
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, Set<String>> {
    public HasIncomingInterfaces(Matcher<? super Set<String>> subMatcher) {
      super(
          subMatcher,
          "A BDDFirewallSessionTraceInfo with incomingInterfaces:",
          "incomingInterfaces");
    }

    @Override
    protected Set<String> featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getIncomingInterfaces();
    }
  }

  static final class HasNextHop
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, NodeInterfacePair> {
    public HasNextHop(Matcher<? super NodeInterfacePair> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with nextHop:", "nextHop");
    }

    @Override
    protected NodeInterfacePair featureValueOf(
        BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getNextHop();
    }
  }

  static final class HasOutgoingInterface
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, String> {
    public HasOutgoingInterface(Matcher<? super String> subMatcher) {
      super(
          subMatcher, "A BDDFirewallSessionTraceInfo with outgoingInterface:", "outgoingInterface");
    }

    @Override
    protected String featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getOutgoingInterface();
    }
  }

  static final class HasSessionFlows extends FeatureMatcher<BDDFirewallSessionTraceInfo, BDD> {
    public HasSessionFlows(Matcher<? super BDD> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with sessionFlows:", "sessionFlows");
    }

    @Override
    protected BDD featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getSessionFlows();
    }
  }

  static final class HasTransformation
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, Transition> {
    public HasTransformation(Matcher<? super Transition> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with transformation:", "transformation");
    }

    @Override
    protected Transition featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getTransformation();
    }
  }
}
