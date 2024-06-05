package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link FwFromJunosApplication} */
public class FwFromJunosApplicationTest {
  private final JuniperConfiguration _jc = new JuniperConfiguration();

  @Before
  public void setUp() {
    _jc.setFilename("host");
  }

  @Test
  public void testApplyTo() {
    JunosApplication junosApp = JunosApplication.JUNOS_BGP;
    FwFromJunosApplication from = new FwFromJunosApplication(junosApp);

    List<ExprAclLine> lines = new ArrayList<>();
    HeaderSpace.Builder hsBuilder = HeaderSpace.builder();

    from.applyTo(null, hsBuilder, LineAction.PERMIT, lines, null);

    assertThat(
        lines,
        equalTo(
            ImmutableList.of(
                new ExprAclLine(
                    LineAction.PERMIT,
                    new MatchHeaderSpace(
                        HeaderSpace.builder()
                            .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                            .setDstPorts(
                                ImmutableSet.of(SubRange.singleton(NamedPort.BGP.number())))
                            .build()),
                    null,
                    junosApp.getBaseApplication().getTermTraceElement(null),
                    null))));
  }

  /**
   * Tests that converting this FwFromApplication to an AclLineMatchExpr just directly returns the
   * app's AclLineMatchExpr.
   */
  @Test
  public void testToAclLineMatchExpr() {
    JunosApplication junosApp = JunosApplication.JUNOS_BGP;

    assertEquals(
        new FwFromJunosApplication(junosApp).toAclLineMatchExpr(_jc, null),
        junosApp.toAclLineMatchExpr(_jc, null));
  }
}
