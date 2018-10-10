package org.batfish.grammar.cisco;

import static java.util.Objects.requireNonNull;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.IpMatchers.containedByPrefix;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.NavigableMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TransformationList;
import org.batfish.datamodel.transformation.Transformation.Direction;
import org.batfish.main.BatfishTestUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CiscoNatTest {
  private static final String HOST_NAME = "ios-nat-test";
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco/testconfigs/";
  private static final String INSIDE_IFACE_NAME = "Ethernet1";
  private static final String OUTSIDE_IFACE_NAME = "Ethernet2";
  private static final String SOME_OTHER_IP_ADDER = "123.123.123.123";
  private static final Ip SOME_OTHER_IP = new Ip(SOME_OTHER_IP_ADDER);

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  private Configuration _c;
  private TransformationList _egressNats;
  private TransformationList _ingressNats;

  @Before
  public void setUp() throws IOException {
    _c = parseConfig();

    NavigableMap<String, Interface> interfaces = _c.getAllInterfaces();

    assertThat(interfaces, Matchers.hasKey(INSIDE_IFACE_NAME));
    assertThat(interfaces, hasKey(OUTSIDE_IFACE_NAME));

    Interface inside = interfaces.get(INSIDE_IFACE_NAME);
    Interface outside = interfaces.get(OUTSIDE_IFACE_NAME);

    // No NATs on inside interface
    assertThat(inside.getEgressNats(), nullValue());
    assertThat(inside.getIngressNats(), nullValue());

    _egressNats = requireNonNull(outside.getEgressNats());
    _ingressNats = requireNonNull(outside.getIngressNats());
  }

  @Test
  public void testExplicitInsideSourceAddress_SourceChanged() {
    Flow flow = createFlow("1.1.1.1", SOME_OTHER_IP_ADDER, INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("2.2.2.2")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Test
  public void testExplicitInsideSourceAddressReturn_DestinationChanged() {
    Flow flow = createFlow(SOME_OTHER_IP_ADDER, "2.2.2.2", OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(SOME_OTHER_IP));
    assertThat(transformed, hasDstIp(new Ip("1.1.1.1")));
  }

  @Test
  public void testNetworkPrefixInsideSourceAddress_SourceChanged() {
    Flow flow = createFlow("1.1.2.1", SOME_OTHER_IP_ADDER, INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(containedByPrefix("2.2.2.0/24")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Test
  public void testNetworkPrefixInsideSourceAddressReturn_DestinationChanged() {
    Flow flow = createFlow(SOME_OTHER_IP_ADDER, "2.2.2.1", OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(SOME_OTHER_IP));
    assertThat(transformed, hasDstIp(new Ip("1.1.2.1")));
  }

  @Test
  public void testNetworkNetmaskInsideSourceAddress_SourceChanged() {
    Flow flow = createFlow("1.1.3.1", SOME_OTHER_IP_ADDER, INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(containedByPrefix("2.2.3.0/24")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Test
  public void testNetworkNetmaskInsideSourceAddressReturn_DestinationChanged() {
    Flow flow = createFlow(SOME_OTHER_IP_ADDER, "1.1.3.1", OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(SOME_OTHER_IP));
    assertThat(transformed, hasDstIp(new Ip("1.1.3.1")));
  }

  @Test
  public void testAclPoolInsideSourceAddress_SourceChanged() {
    Flow flow = createFlow("10.10.10.10", SOME_OTHER_IP_ADDER, INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(containedByPrefix("3.3.3.0/24")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Ignore("Dynamic rules are not fully implemented")
  @Test
  public void testAclPoolInsideSourceAddressReturn_DestinationChanged() {
    Flow flow = createFlow(SOME_OTHER_IP_ADDER, "3.3.3.1", OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(SOME_OTHER_IP));
    assertThat(transformed, hasDstIp(new Ip("10.10.10.10")));
  }

  @Test
  public void testAclPoolInsideDestinationAddress_DestinationChanged() {
    Flow flow = createFlow(SOME_OTHER_IP_ADDER, "11.11.11.11", INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(SOME_OTHER_IP));
    assertThat(transformed, hasDstIp(containedByPrefix("3.3.4.0/24")));
  }

  @Ignore("Dynamic rules are not fully implemented")
  @Test
  public void testAclPoolInsideDestinationAddressReturn_SourceChanged() {
    Flow flow = createFlow("3.3.4.1", SOME_OTHER_IP_ADDER, OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("11.11.11.11")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Test
  public void testExplicitInsideSrcAndDestAddress_SrcAndDestChanged() {
    Flow flow = createFlow("1.1.1.1", "11.11.11.11", INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("2.2.2.2")));
    assertThat(transformed, hasDstIp(containedByPrefix("3.3.4.0/24")));
  }

  @Ignore("Dynamic rules are not fully implemented")
  @Test
  public void testExplicitInsideSrcAndDestAddressReturn_SrcAndDestChanged() {
    Flow flow = createFlow("3.3.4.1", "2.2.2.2", INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("11.11.11.11")));
    assertThat(transformed, hasDstIp(new Ip("1.1.1.1")));
  }

  @Test
  public void testExplicitInsideSrcAndOutsideSrc_SrcAndDestChanged() {
    Flow flow = createFlow("1.1.1.1", "7.7.7.7", INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("2.2.2.2")));
    assertThat(transformed, hasDstIp(new Ip("6.6.6.6")));
  }

  @Test
  public void testExplicitInsideSrcAndOutsideSrcReturn_SrcAndDestChanged() {
    Flow flow = createFlow("6.6.6.6", "2.2.2.2", OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("7.7.7.7")));
    assertThat(transformed, hasDstIp(new Ip("1.1.1.1")));
  }

  @Test
  public void testExplicitOutsideSourceAddress_SourceChanged() {
    Flow flow = createFlow("6.6.6.6", SOME_OTHER_IP_ADDER, OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(new Ip("7.7.7.7")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Test
  public void testExplicitOutsideSourceAddressReturn_DestinationChanged() {
    Flow flow = createFlow(SOME_OTHER_IP_ADDER, "7.7.7.7", INSIDE_IFACE_NAME);

    Flow transformed = applyEgressTransform(flow);
    assertThat(transformed, hasSrcIp(SOME_OTHER_IP));
    assertThat(transformed, hasDstIp(new Ip("6.6.6.6")));
  }

  @Ignore("Outside dynamic rules are not fully implemented")
  @Test
  public void testAclPoolOutsideSourceAddress_SourceChanged() {
    Flow flow = createFlow("22.22.22.22", SOME_OTHER_IP_ADDER, OUTSIDE_IFACE_NAME);

    Flow transformed = applyIngressTransform(flow);
    assertThat(transformed, hasSrcIp(containedByPrefix("4.4.4.0/24")));
    assertThat(transformed, hasDstIp(SOME_OTHER_IP));
  }

  @Test
  public void testInsideWrongDirection_FlowUnchanged() {
    assertNotTransformed(
        "1.1.1.1", SOME_OTHER_IP_ADDER, Direction.INGRESS, OUTSIDE_IFACE_NAME, _egressNats);
    assertNotTransformed(
        "1.1.2.1", SOME_OTHER_IP_ADDER, Direction.INGRESS, OUTSIDE_IFACE_NAME, _egressNats);
    assertNotTransformed(
        "1.1.3.1", SOME_OTHER_IP_ADDER, Direction.INGRESS, OUTSIDE_IFACE_NAME, _egressNats);
    // TODO: uncomment when dynamic rules are fully implemented
    // assertNotTransformed(
    //     "10.10.10.10", SOME_OTHER_IP_ADDER, Direction.INGRESS, OUTSIDE_IFACE_NAME, _egressNats);
    // assertNotTransformed(
    //     SOME_OTHER_IP_ADDER, "11.11.11.11", Direction.INGRESS, OUTSIDE_IFACE_NAME, _egressNats);
  }

  @Test
  public void testOutsideWrongDirection_FlowUnchanged() {
    assertNotTransformed(
        "6.6.6.6", SOME_OTHER_IP_ADDER, Direction.EGRESS, INSIDE_IFACE_NAME, _ingressNats);
    // TODO: uncomment when dynamic rules are fully implemented
    // assertNotTransformed(
    //     SOME_OTHER_IP_ADDER, "22.22.22.22", Direction.EGRESS, INSIDE_IFACE_NAME, _ingressNats);
  }

  private Flow applyEgressTransform(Flow flow) {
    return applyTransform(_egressNats, flow, Direction.EGRESS, INSIDE_IFACE_NAME);
  }

  private Flow applyIngressTransform(Flow flow) {
    return applyTransform(_ingressNats, flow, Direction.INGRESS, OUTSIDE_IFACE_NAME);
  }

  private Flow applyTransform(
      TransformationList nats, Flow flow, Direction direction, String srcInterface) {
    return nats.apply(flow, direction, srcInterface, _c.getIpAccessLists(), _c.getIpSpaces());
  }

  private void assertNotTransformed(
      String srcIp,
      String dstIp,
      Direction direction,
      String srcInterface,
      TransformationList nats) {
    Flow notNatted = createFlow(srcIp, dstIp, srcInterface);
    assertThat(applyTransform(nats, notNatted, direction, srcInterface), is(notNatted));
  }

  private Configuration parseConfig() throws IOException {
    String[] names = new String[] {TESTCONFIGS_PREFIX + HOST_NAME};
    return BatfishTestUtils.parseTextConfigs(_folder, names).get(HOST_NAME);
  }

  private static Flow createFlow(String srcIp, String dstIp, String ingressInterface) {
    return Flow.builder()
        .setIngressNode("")
        .setTag("")
        .setSrcIp(new Ip(srcIp))
        .setDstIp(new Ip(dstIp))
        .setIngressInterface(ingressInterface)
        .build();
  }
}
