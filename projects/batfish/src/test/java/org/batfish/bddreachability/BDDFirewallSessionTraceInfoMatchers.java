package org.batfish.bddreachability;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.SessionAction;
import org.batfish.datamodel.flow.SessionScope;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link BDDFirewallSessionTraceInfo}. */
@ParametersAreNonnullByDefault
public final class BDDFirewallSessionTraceInfoMatchers {
  private BDDFirewallSessionTraceInfoMatchers() {}

  public static @Nonnull Matcher<BDDFirewallSessionTraceInfo> hasAction(
      SessionAction expectedAction) {
    return hasAction(equalTo(expectedAction));
  }

  public static @Nonnull Matcher<BDDFirewallSessionTraceInfo> hasAction(
      Matcher<? super SessionAction> subMatcher) {
    return new HasAction(subMatcher);
  }

  public static Matcher<BDDFirewallSessionTraceInfo> hasHostname(String hostname) {
    return new HasHostname(equalTo(hostname));
  }

  /**
   * Matches {@link BDDFirewallSessionTraceInfo} with an {@link IncomingSessionScope} whose incoming
   * interfaces match the given matcher
   */
  public static Matcher<BDDFirewallSessionTraceInfo> hasIncomingInterfaces(
      Matcher<? super Set<String>> matcher) {
    return hasSessionScope(
        allOf(instanceOf(IncomingSessionScope.class), hasProperty("incomingInterfaces", matcher)));
  }

  public static Matcher<BDDFirewallSessionTraceInfo> hasSessionScope(SessionScope sessionScope) {
    return hasSessionScope(equalTo(sessionScope));
  }

  public static Matcher<BDDFirewallSessionTraceInfo> hasSessionScope(
      Matcher<? super SessionScope> matcher) {
    return new HasSessionScope(matcher);
  }

  public static Matcher<BDDFirewallSessionTraceInfo> hasSessionFlows(BDD bdd) {
    return new HasSessionFlows(equalTo(bdd));
  }

  public static Matcher<BDDFirewallSessionTraceInfo> hasTransformation(Transition transformation) {
    return new HasTransformation(equalTo(transformation));
  }

  private static final class HasAction
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, SessionAction> {
    public HasAction(Matcher<? super SessionAction> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with action:", "action");
    }

    @Override
    protected @Nonnull SessionAction featureValueOf(
        BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getAction();
    }
  }

  private static final class HasHostname
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, String> {
    public HasHostname(Matcher<? super String> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with hostname:", "hostname");
    }

    @Override
    protected String featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getHostname();
    }
  }

  private static final class HasSessionScope
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, SessionScope> {
    public HasSessionScope(Matcher<? super SessionScope> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with sessionScope:", "sessionScope");
    }

    @Override
    protected SessionScope featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getSessionScope();
    }
  }

  private static final class HasSessionFlows
      extends FeatureMatcher<BDDFirewallSessionTraceInfo, BDD> {
    public HasSessionFlows(Matcher<? super BDD> subMatcher) {
      super(subMatcher, "A BDDFirewallSessionTraceInfo with sessionFlows:", "sessionFlows");
    }

    @Override
    protected BDD featureValueOf(BDDFirewallSessionTraceInfo bddFirewallSessionTraceInfo) {
      return bddFirewallSessionTraceInfo.getSessionFlows();
    }
  }

  private static final class HasTransformation
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
