package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class FlowMatchersImpl {
  private FlowMatchersImpl() {}

  public static final class HasDstIp extends FeatureMatcher<Flow, Ip> {
    HasDstIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A Flow with dstIp:", "dstIp");
    }

    @Override
    protected Ip featureValueOf(Flow flow) {
      return flow.getDstIp();
    }
  }

  public static final class HasIngressInterface extends FeatureMatcher<Flow, String> {
    HasIngressInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with ingressInterface:", "ingressInterface");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressInterface();
    }
  }

  public static final class HasIngressNode extends FeatureMatcher<Flow, String> {
    HasIngressNode(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with ingressNode:", "ingressNode");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressNode();
    }
  }

  public static final class HasIngressVrf extends FeatureMatcher<Flow, String> {
    HasIngressVrf(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with ingressVrf:", "ingressVrf");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressVrf();
    }
  }

  public static final class HasSrcIp extends FeatureMatcher<Flow, Ip> {
    HasSrcIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A Flow with srcIp:", "srcIp");
    }

    @Override
    protected Ip featureValueOf(Flow flow) {
      return flow.getSrcIp();
    }
  }

  public static final class HasTag extends FeatureMatcher<Flow, String> {
    HasTag(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with tag:", "tag");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getTag();
    }
  }
}
