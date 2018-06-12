package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class FlowMatchersImpl {
  private FlowMatchersImpl() {}

  public static class HasDstIp extends FeatureMatcher<Flow, Ip> {
    HasDstIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "dstIp", "dstIp");
    }

    @Override
    protected Ip featureValueOf(Flow flow) {
      return flow.getDstIp();
    }
  }

  public static class HasIngressNode extends FeatureMatcher<Flow, String> {
    HasIngressNode(Matcher<? super String> subMatcher) {
      super(subMatcher, "ingressNode", "ingressNode");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressNode();
    }
  }

  public static class HasSrcIp extends FeatureMatcher<Flow, Ip> {
    HasSrcIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "srcIp", "srcIp");
    }

    @Override
    protected Ip featureValueOf(Flow flow) {
      return flow.getSrcIp();
    }
  }
}
