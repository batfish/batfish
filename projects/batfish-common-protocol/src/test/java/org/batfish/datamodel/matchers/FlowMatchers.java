package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasDstIp;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressNode;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasSrcIp;
import org.hamcrest.Matcher;

public final class FlowMatchers {
  private FlowMatchers() {}

  public static Matcher<Flow> hasDstIp(Ip ip) {
    return new HasDstIp(equalTo(ip));
  }

  public static Matcher<Flow> hasDstIp(Matcher<? super Ip> ipMatcher) {
    return new HasDstIp(ipMatcher);
  }

  public static Matcher<Flow> hasIngressNode(String ingressNode) {
    return new HasIngressNode(equalTo(ingressNode));
  }

  public static Matcher<Flow> hasIngressNode(Matcher<? super String> ingressNodeMatcher) {
    return new HasIngressNode(ingressNodeMatcher);
  }

  public static Matcher<Flow> hasSrcIp(Ip ip) {
    return new HasSrcIp(equalTo(ip));
  }

  public static Matcher<Flow> hasSrcIp(Matcher<? super Ip> ipMatcher) {
    return new HasSrcIp(ipMatcher);
  }
}
