package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromHostService} */
public class FwFromHostServiceTest {

  @Test
  public void testApplyTo_bgp() {
    FwFromHostService from = new FwFromHostService(HostSystemService.DNS);
    List<ExprAclLine> lines = new ArrayList<>();
    from.applyTo(lines, null);

    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                            .setDstPorts(
                                ImmutableSet.of(SubRange.singleton(NamedPort.DOMAIN.number())))
                            .build()),
                    null,
                    HostSystemService.DNS.getTraceElement()))));
  }

  @Test
  public void testApplyTo_all_traceElement() {
    FwFromHostService from = new FwFromHostService(HostSystemService.ALL);
    List<ExprAclLine> lines = new ArrayList<>();
    from.applyTo(lines, null);

    List<HostSystemService> unhandledServices =
        ImmutableList.of(
            HostSystemService.ALL,
            HostSystemService.ANY_SERVICE,
            HostSystemService.IDENT_RESET,
            HostSystemService.LSPING);

    List<TraceElement> expectedTraceElements =
        Stream.of(HostSystemService.values())
            .filter(hs -> !unhandledServices.contains(hs))
            .map(HostSystemService::getTraceElement)
            .collect(ImmutableList.toImmutableList());

    assertThat(
        lines.stream().map(ExprAclLine::getTraceElement).collect(ImmutableList.toImmutableList()),
        equalTo(expectedTraceElements));
  }
}
