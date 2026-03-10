package org.batfish.question.filterlinereachability;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchDestinationPort;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchIpProtocol;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSourcePort;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.VendorStructureId;
import org.junit.Before;
import org.junit.Test;

public class AclEraserTest {
  private AclEraser _eraser;
  private TraceElement _traceElem;
  private VendorStructureId _vsId;

  @Before
  public void setup() {
    _eraser = AclEraser.INSTANCE;
    _traceElem = TraceElement.of("trace elem");
    _vsId = new VendorStructureId("file", "type", "name");
  }

  @Test
  public void testAclLineMatchExprs() {
    TrueExpr trueExpr = new TrueExpr(_traceElem);
    assertEquals(TrueExpr.INSTANCE, _eraser.visit(trueExpr));
    assertEquals(FalseExpr.INSTANCE, _eraser.visit(new FalseExpr(_traceElem)));
    assertEquals(OriginatingFromDevice.INSTANCE, _eraser.visit(OriginatingFromDevice.INSTANCE));
    assertEquals(new PermittedByAcl("acl"), _eraser.visit(new PermittedByAcl("acl", _traceElem)));
    assertEquals(new DeniedByAcl("acl"), _eraser.visit(new DeniedByAcl("acl", _traceElem)));
    assertEquals(
        new NotMatchExpr(TrueExpr.INSTANCE), _eraser.visit(new NotMatchExpr(trueExpr, _traceElem)));
    {
      List<String> ifaces = ImmutableList.of("i");
      assertEquals(
          new MatchSrcInterface(ifaces), _eraser.visit(new MatchSrcInterface(ifaces, _traceElem)));
    }
    {
      List<AclLineMatchExpr> origInners = ImmutableList.of(trueExpr);
      List<AclLineMatchExpr> erasedInners = ImmutableList.of(TrueExpr.INSTANCE);
      assertEquals(
          new OrMatchExpr(erasedInners), _eraser.visit(new OrMatchExpr(origInners, _traceElem)));
      assertEquals(
          new AndMatchExpr(erasedInners), _eraser.visit(new AndMatchExpr(origInners, _traceElem)));
    }
    {
      HeaderSpace headerSpace = HeaderSpace.builder().setDstIps(UniverseIpSpace.INSTANCE).build();
      assertEquals(
          new MatchHeaderSpace(headerSpace),
          _eraser.visit(new MatchHeaderSpace(headerSpace, _traceElem)));
    }
    {
      MatchIpProtocol noTrace = (MatchIpProtocol) matchIpProtocol(IpProtocol.TCP);
      MatchIpProtocol trace = (MatchIpProtocol) matchIpProtocol(IpProtocol.TCP, _traceElem);
      assertThat(_eraser.visit(noTrace), sameInstance(noTrace));
      assertThat(_eraser.visit(trace), equalTo(noTrace));
    }
    {
      MatchDestinationIp noTrace = (MatchDestinationIp) matchDst(Ip.parse("1.1.1.1"));
      MatchDestinationIp trace =
          (MatchDestinationIp) matchDst(Ip.parse("1.1.1.1").toIpSpace(), _traceElem);
      assertThat(_eraser.visit(noTrace), sameInstance(noTrace));
      assertThat(_eraser.visit(trace), equalTo(noTrace));
    }
    {
      MatchSourceIp noTrace = (MatchSourceIp) matchSrc(Ip.parse("1.1.1.1"));
      MatchSourceIp trace = (MatchSourceIp) matchSrc(Ip.parse("1.1.1.1").toIpSpace(), _traceElem);
      assertThat(_eraser.visit(noTrace), sameInstance(noTrace));
      assertThat(_eraser.visit(trace), equalTo(noTrace));
    }
    {
      MatchDestinationPort noTrace = (MatchDestinationPort) matchDstPort(1);
      MatchDestinationPort trace =
          (MatchDestinationPort) matchDstPort(IntegerSpace.of(1), _traceElem);
      assertThat(_eraser.visit(noTrace), sameInstance(noTrace));
      assertThat(_eraser.visit(trace), equalTo(noTrace));
    }
    {
      MatchSourcePort noTrace = (MatchSourcePort) matchSrcPort(1);
      MatchSourcePort trace = (MatchSourcePort) matchSrcPort(1, _traceElem);
      assertThat(_eraser.visit(noTrace), sameInstance(noTrace));
      assertThat(_eraser.visit(trace), equalTo(noTrace));
    }
  }

  @Test
  public void testAclLines() {
    assertEquals(
        new AclAclLine("name", "acl"),
        _eraser.visit(new AclAclLine("name", "acl", _traceElem, _vsId)));

    assertEquals(
        new ExprAclLine(LineAction.PERMIT, TrueExpr.INSTANCE, "name"),
        _eraser.visit(
            ExprAclLine.accepting()
                .setName("name")
                .setVendorStructureId(_vsId)
                .setTraceElement(_traceElem)
                .setMatchCondition(new TrueExpr(_traceElem))
                .build()));
  }
}
