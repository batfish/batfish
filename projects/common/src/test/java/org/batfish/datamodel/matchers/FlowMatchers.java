package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class FlowMatchers {
  private FlowMatchers() {}

  public static Matcher<Flow> hasDstIp(Ip ip) {
    return new HasDstIp(equalTo(ip));
  }

  public static Matcher<Flow> hasDstIp(Matcher<? super Ip> ipMatcher) {
    return new HasDstIp(ipMatcher);
  }

  public static Matcher<Flow> hasDstPort(int port) {
    return new HasDstPort(equalTo(port));
  }

  public static Matcher<Flow> hasDstPort(Matcher<? super Integer> portMatcher) {
    return new HasDstPort(portMatcher);
  }

  public static Matcher<Flow> hasIcmpCode(int icmpCode) {
    return new HasIcmpCode(equalTo(icmpCode));
  }

  public static Matcher<Flow> hasIcmpCode(Matcher<? super Integer> icmpCodeMatcher) {
    return new HasIcmpCode(icmpCodeMatcher);
  }

  public static Matcher<Flow> hasIcmpType(int icmpType) {
    return new HasIcmpType(equalTo(icmpType));
  }

  public static Matcher<Flow> hasIcmpType(Matcher<? super Integer> matcher) {
    return new HasIcmpType(matcher);
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

  public static Matcher<Flow> hasIpProtocol(IpProtocol ipProtocol) {
    return new HasIpProtocol(equalTo(ipProtocol));
  }

  public static Matcher<Flow> hasIpProtocol(Matcher<? super IpProtocol> ipProtocolMatcher) {
    return new HasIpProtocol(ipProtocolMatcher);
  }

  public static Matcher<Flow> hasSrcIp(Ip ip) {
    return new HasSrcIp(equalTo(ip));
  }

  public static Matcher<Flow> hasSrcIp(Matcher<? super Ip> ipMatcher) {
    return new HasSrcIp(ipMatcher);
  }

  public static Matcher<Flow> hasSrcPort(int port) {
    return new HasSrcPort(equalTo(port));
  }

  public static Matcher<Flow> hasSrcPort(Matcher<? super Integer> portMatcher) {
    return new HasSrcPort(portMatcher);
  }

  public static Matcher<Flow> hasTcpFlagsAck(boolean tcpAck) {
    return new HasTcpFlagsAck(equalTo(tcpAck));
  }

  public static Matcher<Flow> hasTcpFlagsAck(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsAck(matcher);
  }

  public static Matcher<Flow> hasTcpFlagsCwr(boolean tcpCwr) {
    return new HasTcpFlagsCwr(equalTo(tcpCwr));
  }

  public static Matcher<Flow> hasTcpFlagsCwr(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsCwr(matcher);
  }

  public static Matcher<Flow> hasTcpFlagsEce(boolean tcpEce) {
    return new HasTcpFlagsEce(equalTo(tcpEce));
  }

  public static Matcher<Flow> hasTcpFlagsEce(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsEce(matcher);
  }

  public static Matcher<Flow> hasTcpFlagsFin(boolean tcpFin) {
    return new HasTcpFlagsFin(equalTo(tcpFin));
  }

  public static Matcher<Flow> hasTcpFlagsFin(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsFin(matcher);
  }

  public static Matcher<Flow> hasTcpFlagsPsh(boolean tcpPsh) {
    return new HasTcpFlagsPsh(equalTo(tcpPsh));
  }

  public static Matcher<Flow> hasTcpFlagsPsh(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsPsh(matcher);
  }

  public static Matcher<Flow> hasTcpFlagsRst(boolean tcpRst) {
    return new HasTcpFlagsRst(equalTo(tcpRst));
  }

  public static Matcher<Flow> hasTcpFlagsRst(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsRst(matcher);
  }

  public static Matcher<Flow> hasTcpFlagsUrg(boolean tcpUrg) {
    return new HasTcpFlagsUrg(equalTo(tcpUrg));
  }

  public static Matcher<Flow> hasTcpFlagsUrg(Matcher<? super Boolean> matcher) {
    return new HasTcpFlagsUrg(matcher);
  }

  private static final class HasDstIp extends FeatureMatcher<Flow, Ip> {
    HasDstIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A Flow with dstIp:", "dstIp");
    }

    @Override
    protected Ip featureValueOf(Flow flow) {
      return flow.getDstIp();
    }
  }

  private static final class HasDstPort extends FeatureMatcher<Flow, Integer> {
    HasDstPort(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with dstPort:", "dstPort");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getDstPort();
    }
  }

  private static final class HasIcmpCode extends FeatureMatcher<Flow, Integer> {
    HasIcmpCode(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with icmpCode:", "icmpCode");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getIcmpCode();
    }
  }

  private static final class HasIcmpType extends FeatureMatcher<Flow, Integer> {
    HasIcmpType(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with icmpType:", "icmpType");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getIcmpType();
    }
  }

  private static final class HasIngressInterface extends FeatureMatcher<Flow, String> {
    HasIngressInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with ingressInterface:", "ingressInterface");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressInterface();
    }
  }

  private static final class HasIngressNode extends FeatureMatcher<Flow, String> {
    HasIngressNode(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with ingressNode:", "ingressNode");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressNode();
    }
  }

  private static final class HasIngressVrf extends FeatureMatcher<Flow, String> {
    HasIngressVrf(Matcher<? super String> subMatcher) {
      super(subMatcher, "A Flow with ingressVrf:", "ingressVrf");
    }

    @Override
    protected String featureValueOf(Flow flow) {
      return flow.getIngressVrf();
    }
  }

  private static final class HasIpProtocol extends FeatureMatcher<Flow, IpProtocol> {
    HasIpProtocol(Matcher<? super IpProtocol> subMatcher) {
      super(subMatcher, "A Flow with ipProtocol:", "ipProtocol");
    }

    @Override
    protected IpProtocol featureValueOf(Flow flow) {
      return flow.getIpProtocol();
    }
  }

  private static final class HasSrcIp extends FeatureMatcher<Flow, Ip> {
    HasSrcIp(Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A Flow with srcIp:", "srcIp");
    }

    @Override
    protected Ip featureValueOf(Flow flow) {
      return flow.getSrcIp();
    }
  }

  private static final class HasSrcPort extends FeatureMatcher<Flow, Integer> {
    HasSrcPort(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Flow with srcPort:", "srcPort");
    }

    @Override
    protected Integer featureValueOf(Flow flow) {
      return flow.getSrcPort();
    }
  }

  private static final class HasTcpFlagsAck extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsAck(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsAck:", "tcpFlagsAck");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getAck();
    }
  }

  private static final class HasTcpFlagsCwr extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsCwr(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsCwr:", "tcpFlagsCwr");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getCwr();
    }
  }

  private static final class HasTcpFlagsEce extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsEce(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsEce:", "tcpFlagsEce");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getEce();
    }
  }

  private static final class HasTcpFlagsFin extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsFin(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsFin:", "tcpFlagsFin");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getFin();
    }
  }

  private static final class HasTcpFlagsPsh extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsPsh(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsPsh:", "tcpFlagsPsh");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getPsh();
    }
  }

  private static final class HasTcpFlagsRst extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsRst(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsRst:", "tcpFlagsRst");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getRst();
    }
  }

  private static final class HasTcpFlagsUrg extends FeatureMatcher<Flow, Boolean> {
    HasTcpFlagsUrg(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A Flow with tcpFlagsUrg:", "tcpFlagsUrg");
    }

    @Override
    protected Boolean featureValueOf(Flow flow) {
      return flow.getTcpFlags().getUrg();
    }
  }
}
