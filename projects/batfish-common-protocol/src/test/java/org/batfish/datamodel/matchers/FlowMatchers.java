package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasDstIp;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressInterface;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressNode;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressVrf;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasSrcIp;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTag;
import org.hamcrest.Matcher;

public final class FlowMatchers {
  private FlowMatchers() {}

  public static Matcher<Flow> hasDstIp(Ip ip) {
    return new HasDstIp(equalTo(ip));
  }

  public static Matcher<Flow> hasDstIp(Matcher<? super Ip> ipMatcher) {
    return new HasDstIp(ipMatcher);
  }

  public static Matcher<Flow> hasIngressInterface(String ingressInterface) {
    return new HasIngressInterface(equalTo(ingressInterface));
  }

  public static Matcher<Flow> hasIngressInterface(Matcher<? super String> ingressInterfaceMatcher) {
    return new HasIngressInterface(ingressInterfaceMatcher);
  }

  public static Matcher<Flow> hasIngressNode(String ingressNode) {
    return new HasIngressNode(equalTo(ingressNode));
  }

  public static Matcher<Flow> hasIngressNode(Matcher<? super String> ingressNodeMatcher) {
    return new HasIngressNode(ingressNodeMatcher);
  }

  public static Matcher<Flow> hasIngressVrf(String ingressVrf) {
    return new HasIngressVrf(equalTo(ingressVrf));
  }

  public static Matcher<Flow> hasIngressVrf(Matcher<? super String> ingressVrfMatcher) {
    return new HasIngressVrf(ingressVrfMatcher);
  }

  public static Matcher<Flow> hasSrcIp(Ip ip) {
    return new HasSrcIp(equalTo(ip));
  }

  public static Matcher<Flow> hasSrcIp(Matcher<? super Ip> ipMatcher) {
    return new HasSrcIp(ipMatcher);
  }

  public static Matcher<Flow> hasTag(String tag) {
    return new HasTag(equalTo(tag));
  }

  public static Matcher<Flow> hasTag(Matcher<? super String> tagMatcher) {
    return new HasTag(tagMatcher);
  }
}
