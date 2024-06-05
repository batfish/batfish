package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
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

/** Test for {@link HostSystemService} */
public class HostSystemServiceTest {

  @Test
  public void testGetMatchExpr_bgp() {
    HostSystemService from = HostSystemService.DNS;
    Optional<AclLineMatchExpr> matchExpr = from.getMatchExpr();
    assert matchExpr.isPresent();

    assertThat(
        matchExpr.get(),
        equalTo(
            new MatchHeaderSpace(
                HeaderSpace.builder()
                    .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                    .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.DOMAIN.number())))
                    .build(),
                HostSystemService.DNS.getTraceElement())));
  }

  @Test
  public void testGetMatchExpr_all_traceElement() {
    HostSystemService from = HostSystemService.ALL;
    Optional<AclLineMatchExpr> matchExpr = from.getMatchExpr();
    assert matchExpr.isPresent();

    TraceElement expectedTraceElement = from.getTraceElement();
    assertThat(matchExpr.get().getTraceElement(), equalTo(expectedTraceElement));

    List<HostSystemService> unhandledServices =
        ImmutableList.of(
            HostSystemService.ALL,
            HostSystemService.ANY_SERVICE,
            HostSystemService.IDENT_RESET,
            HostSystemService.LSPING);

    List<TraceElement> expectedChildTraceElements =
        Stream.of(HostSystemService.values())
            .filter(hs -> !unhandledServices.contains(hs))
            .map(HostSystemService::getTraceElement)
            .collect(ImmutableList.toImmutableList());

    assertThat(
        ((OrMatchExpr) matchExpr.get())
            .getDisjuncts().stream()
                .map(AclLineMatchExpr::getTraceElement)
                .collect(ImmutableList.toImmutableList()),
        equalTo(expectedChildTraceElements));
  }
}
