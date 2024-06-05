package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.junit.Test;

/** Test for {@link HostProtocol} */
public class HostProtocolTest {

  @Test
  public void testGetMatchExpr_bgp() {
    HostProtocol from = HostProtocol.BGP;
    Optional<AclLineMatchExpr> matchExpr = from.getMatchExpr();
    assert matchExpr.isPresent();

    assertThat(
        matchExpr.get(),
        equalTo(
            new MatchHeaderSpace(
                HeaderSpace.builder()
                    .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                    .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.BGP.number())))
                    .build(),
                TraceElement.of("Matched host-inbound-traffic protocol BGP"))));
  }

  @Test
  public void testGetMatchExpr_all_traceElements() {
    HostProtocol from = HostProtocol.ALL;
    Optional<AclLineMatchExpr> matchExpr = from.getMatchExpr();
    assert matchExpr.isPresent();

    TraceElement expectedTraceElement =
        TraceElement.of(String.format("Matched host-inbound-traffic protocol %s", from));
    assertThat(matchExpr.get().getTraceElement(), equalTo(expectedTraceElement));

    List<HostProtocol> unhandledProtocols =
        ImmutableList.of(HostProtocol.ALL, HostProtocol.OSPF3, HostProtocol.RIPNG);

    List<TraceElement> expectedChildTraceElements =
        Stream.of(HostProtocol.values())
            .filter(hp -> !unhandledProtocols.contains(hp))
            .map(
                hp ->
                    TraceElement.of(
                        String.format("Matched host-inbound-traffic protocol %s", hp.toString())))
            .collect(ImmutableList.toImmutableList());

    assertThat(
        ((OrMatchExpr) matchExpr.get())
            .getDisjuncts().stream()
                .map(AclLineMatchExpr::getTraceElement)
                .collect(ImmutableList.toImmutableList()),
        equalTo(expectedChildTraceElements));
  }
}
