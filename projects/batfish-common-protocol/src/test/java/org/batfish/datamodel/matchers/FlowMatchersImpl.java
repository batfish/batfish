package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
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

  public static final class HasDstPort extends FeatureMatcher<Flow, Integer> {
    HasDstPort(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with dstPort:", "dstPort");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getDstPort();
    }
  }

  public static final class HasIcmpCode extends FeatureMatcher<Flow, Integer> {
    HasIcmpCode(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with icmpCode:", "icmpCode");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getIcmpCode();
    }
  }

  public static final class HasIcmpType extends FeatureMatcher<Flow, Integer> {
    HasIcmpType(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with icmpType:", "icmpType");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getIcmpType();
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

  public static final class HasIpProtocol extends FeatureMatcher<Flow, IpProtocol> {
    HasIpProtocol(Matcher<? super IpProtocol> subMatcher) {
      super(subMatcher, "A Flow with ipProtocol:", "ipProtocol");
    }

    @Override
    protected IpProtocol featureValueOf(Flow flow) {
      return flow.getIpProtocol();
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

  public static final class HasSrcPort extends FeatureMatcher<Flow, Integer> {
    HasSrcPort(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with srcPort:", "srcPort");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getSrcPort();
    }
  }

  public static final class HasTcpFlagsAck extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsAck(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsAck:", "tcpFlagsAck");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsAck();
    }
  }

  public static final class HasTcpFlagsCwr extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsCwr(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsCwr:", "tcpFlagsCwr");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsCwr();
    }
  }

  public static final class HasTcpFlagsEce extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsEce(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsEce:", "tcpFlagsEce");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsEce();
    }
  }

  public static final class HasTcpFlagsFin extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsFin(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsFin:", "tcpFlagsFin");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsFin();
    }
  }

  public static final class HasTcpFlagsPsh extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsPsh(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsPsh:", "tcpFlagsPsh");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsPsh();
    }
  }

  public static final class HasTcpFlagsRst extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsRst(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsRst:", "tcpFlagsRst");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsRst();
    }
  }

  public static final class HasTcpFlagsUrg extends FeatureMatcher<Flow, Integer> {
    HasTcpFlagsUrg(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsUrg:", "tcpFlagsUrg");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getTcpFlagsUrg();
    }
  }
}
