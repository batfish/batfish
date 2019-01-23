package org.batfish.question.traceroute;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressVrf;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_FORWARD_FLOW;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_FORWARD_TRACE;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_REVERSE_FLOW;
import static org.batfish.question.traceroute.BidirectionalTracerouteAnswerer.COL_REVERSE_TRACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BidirectionalTracerouteAnswerer}. */
public class BidirectionalTracerouteAnswererTest {
  /** */
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  @Test
  public void testAnswer() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();

    Interface.Builder ib = nf.interfaceBuilder().setOwner(c).setVrf(vrf);

    Interface i1 = ib.setAddress(new InterfaceAddress("1.0.0.1/29")).build();
    Interface i2 = ib.setAddress(new InterfaceAddress("2.0.0.1/29")).build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _tempFolder);
    batfish.computeDataPlane(false);

    Ip i1HostIp = Ip.parse("1.0.0.2");
    Ip i2HostIp = Ip.parse("2.0.0.2");
    Ip i2Ip = i2.getAddress().getIp();

    // enter(i1) -> exit(i2)
    {
      BidirectionalTracerouteQuestion question =
          new BidirectionalTracerouteQuestion(
              String.format("enter(%s[%s])", c.getHostname(), i1.getName()),
              PacketHeaderConstraints.builder()
                  .setDstIp(
                      String.format("ofLocation(enter(%s[%s]))", c.getHostname(), i2.getName()))
                  .build(),
              false,
              5);

      TableAnswerElement answer =
          (TableAnswerElement) new BidirectionalTracerouteAnswerer(question, batfish).answer();
      assertThat(
          answer,
          hasRows(
              contains(
                  allOf(
                      hasColumn(
                          COL_FORWARD_FLOW,
                          allOf(
                              hasDstIp(i2HostIp),
                              hasSrcIp(i1HostIp),
                              hasIngressNode(c.getHostname()),
                              hasIngressInterface(i1.getName()),
                              hasIngressVrf(nullValue())),
                          Schema.FLOW),
                      hasColumn(
                          COL_FORWARD_TRACE, hasDisposition(DELIVERED_TO_SUBNET), Schema.TRACE),
                      /* Since forward trace is successful, there's a reverse flow. Since the
                       * forward trace exited i2, the reverse flow enters i2.
                       */
                      hasColumn(
                          COL_REVERSE_FLOW,
                          allOf(
                              hasDstIp(i1HostIp),
                              hasSrcIp(i2HostIp),
                              hasIngressNode(c.getHostname()),
                              hasIngressInterface(i2.getName()),
                              hasIngressVrf(nullValue())),
                          Schema.FLOW),
                      // since there's a reverse flow, there's also a reverse trace
                      hasColumn(COL_REVERSE_TRACE, notNullValue(), Schema.TRACE)))));
    }

    // enter(i1) -> i2
    {
      BidirectionalTracerouteQuestion question =
          new BidirectionalTracerouteQuestion(
              String.format("enter(%s[%s])", c.getHostname(), i1.getName()),
              PacketHeaderConstraints.builder()
                  .setDstIp(String.format("ofLocation(%s[%s])", c.getHostname(), i2.getName()))
                  .build(),
              false,
              5);

      TableAnswerElement answer =
          (TableAnswerElement) new BidirectionalTracerouteAnswerer(question, batfish).answer();
      assertThat(
          answer,
          hasRows(
              contains(
                  allOf(
                      hasColumn(
                          COL_FORWARD_FLOW,
                          allOf(
                              hasDstIp(i2Ip),
                              hasSrcIp(i1HostIp),
                              hasIngressNode(c.getHostname()),
                              hasIngressInterface(i1.getName()),
                              hasIngressVrf(nullValue())),
                          Schema.FLOW),
                      hasColumn(COL_FORWARD_TRACE, hasDisposition(ACCEPTED), Schema.TRACE),
                      /* Since forward trace is successful, there's a reverse flow. Since the
                       * forward trace was ACCEPTED, the reverse trace starts at the VRF.
                       */
                      hasColumn(
                          COL_REVERSE_FLOW,
                          allOf(
                              hasDstIp(i1HostIp),
                              hasSrcIp(i2Ip),
                              hasIngressNode(c.getHostname()),
                              hasIngressInterface(nullValue()),
                              hasIngressVrf(vrf.getName())),
                          Schema.FLOW),
                      // since there's a reverse flow, there's also a reverse trace
                      hasColumn(COL_REVERSE_TRACE, notNullValue(), Schema.TRACE)))));
    }

    // enter(i1) -> unknown Ip
    {
      Ip dstIp = Ip.parse("5.5.5.5");
      BidirectionalTracerouteQuestion question =
          new BidirectionalTracerouteQuestion(
              String.format("enter(%s[%s])", c.getHostname(), i1.getName()),
              PacketHeaderConstraints.builder().setDstIp(dstIp.toString()).build(),
              false,
              5);

      TableAnswerElement answer =
          (TableAnswerElement) new BidirectionalTracerouteAnswerer(question, batfish).answer();
      assertThat(
          answer,
          hasRows(
              contains(
                  allOf(
                      hasColumn(
                          COL_FORWARD_FLOW,
                          allOf(
                              hasDstIp(dstIp),
                              hasSrcIp(i1HostIp),
                              hasIngressNode(c.getHostname()),
                              hasIngressInterface(i1.getName()),
                              hasIngressVrf(nullValue())),
                          Schema.FLOW),
                      hasColumn(COL_FORWARD_TRACE, hasDisposition(NO_ROUTE), Schema.TRACE),
                      // Since forward trace fails, there's no reverse flow.
                      hasColumn(COL_REVERSE_FLOW, nullValue(), Schema.FLOW),
                      // since there's no reverse flow, there's no reverse trace either
                      hasColumn(COL_REVERSE_TRACE, nullValue(), Schema.TRACE)))));
    }
  }
}
