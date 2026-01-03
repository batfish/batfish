package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasDstIp;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasDstPort;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIcmpCode;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIcmpType;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressInterface;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressNode;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIngressVrf;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasIpProtocol;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasSrcIp;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasSrcPort;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsAck;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsCwr;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsEce;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsFin;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsPsh;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsRst;
import org.batfish.datamodel.matchers.FlowMatchersImpl.HasTcpFlagsUrg;
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
}
