package org.batfish.job;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
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
import org.batfish.datamodel.references.StructureManager;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.VendorStructureId;
import org.junit.Before;
import org.junit.Test;

public class InvalidVendorStructureIdEraserTest {
  private enum TestStructureType implements StructureType {
    VALID;

    @Override
    public String getDescription() {
      return name();
    }
  }

  private static final String TEST_STRUCTURE_NAME = "struct";
  private static final String TEST_FILENAME = "filename";

  @Before
  public void setup() {
    StructureManager structureManager = StructureManager.create();
    structureManager.getOrDefine(TestStructureType.VALID, TEST_STRUCTURE_NAME);
    _eraser = new InvalidVendorStructureIdEraser(TEST_FILENAME, structureManager);
  }

  private final VendorStructureId _validVsid =
      new VendorStructureId(
          TEST_FILENAME, TestStructureType.VALID.getDescription(), TEST_STRUCTURE_NAME);
  private final VendorStructureId _invalidVsid =
      new VendorStructureId(
          TEST_FILENAME, TestStructureType.VALID.getDescription(), "otherStructureName");

  private final TraceElement _validTe = TraceElement.builder().add("valid", _validVsid).build();
  private final TraceElement _invalidTe =
      TraceElement.builder().add("invalid", _invalidVsid).build();
  // Same as above trace element, but with VSID removed
  private final TraceElement _erasedVsid = TraceElement.builder().add("invalid").build();

  private InvalidVendorStructureIdEraser _eraser;

  private void assertExprHandled(Function<TraceElement, AclLineMatchExpr> creator) {
    // Valid VSID is left alone
    assertThat(_eraser.visit(creator.apply(_validTe)), equalTo(creator.apply(_validTe)));
    // Invalid VSID is removed
    assertThat(_eraser.visit(creator.apply(_invalidTe)), equalTo(creator.apply(_erasedVsid)));
  }

  @Test
  public void testAclLineMatchExprsTrueExpr() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction = TrueExpr::new;
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsFalseExpr() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction = FalseExpr::new;
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsOriginatingFromDevice() {
    assertEquals(OriginatingFromDevice.INSTANCE, _eraser.visit(OriginatingFromDevice.INSTANCE));
  }

  @Test
  public void testAclLineMatchExprsPermittedByAcl() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new PermittedByAcl("acl", te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsDeniedByAcl() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new DeniedByAcl("acl", te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsNotMatchExpr() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new NotMatchExpr(TrueExpr.INSTANCE, te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsMatchSrcInterface() {
    List<String> ifaces = ImmutableList.of("i");
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new MatchSrcInterface(ifaces, te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsAndMatchExpr() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new AndMatchExpr(ImmutableList.of(new TrueExpr(te)), te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsOrMatchExpr() {
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new OrMatchExpr(ImmutableList.of(new TrueExpr(te)), te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testAclLineMatchExprsMatchHeaderSpace() {
    HeaderSpace headerSpace = HeaderSpace.builder().setDstIps(UniverseIpSpace.INSTANCE).build();
    Function<TraceElement, AclLineMatchExpr> traceElementTFunction =
        te -> new MatchHeaderSpace(headerSpace, te);
    assertExprHandled(traceElementTFunction);
  }

  @Test
  public void testSimpleMatches() {
    assertExprHandled(te -> (MatchDestinationIp) matchDst(Ip.ZERO.toIpSpace(), te));
    assertExprHandled(te -> (MatchSourceIp) matchSrc(Ip.ZERO.toIpSpace(), te));
    assertExprHandled(te -> (MatchDestinationPort) matchDstPort(1, te));
    assertExprHandled(te -> (MatchSourcePort) matchSrcPort(1, te));
    assertExprHandled(te -> (MatchIpProtocol) matchIpProtocol(IpProtocol.TCP, te));
  }

  @Test
  public void testAcl() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                new AclAclLine("name1", "acl1", _validTe, _validVsid),
                new AclAclLine("name2", "acl2", _invalidTe, _invalidVsid))
            .build();
    IpAccessList aclErased =
        IpAccessList.builder()
            .setName("acl")
            .setLines(
                new AclAclLine("name1", "acl1", _validTe, _validVsid),
                new AclAclLine("name2", "acl2", _erasedVsid, null))
            .build();

    assertEquals(_eraser.visit(acl), aclErased);
  }

  @Test
  public void testTraceElement() {
    // Valid and flat-text trace elements are left unchanged
    assertEquals(
        TraceElement.builder().add("flatText").add("valid", _validVsid).build(),
        _eraser.eraseInvalid(
            TraceElement.builder().add("flatText").add("valid", _validVsid).build()));
    // Link fragment with invalid VSID should be flattened to a text fragment
    assertEquals(
        TraceElement.builder().add("invalid").build(),
        _eraser.eraseInvalid(TraceElement.builder().add("invalid", _invalidVsid).build()));
  }

  @Test
  public void testAclLines() {
    // Valid VSIDs are left alone
    assertEquals(
        new AclAclLine("name", "acl", _validTe, _validVsid),
        _eraser.visit(new AclAclLine("name", "acl", _validTe, _validVsid)));
    assertEquals(
        new ExprAclLine(LineAction.PERMIT, new TrueExpr(_validTe), "name", _validTe, _validVsid),
        _eraser.visit(
            ExprAclLine.accepting()
                .setName("name")
                .setTraceElement(_validTe)
                .setMatchCondition(new TrueExpr(_validTe))
                .setVendorStructureId(_validVsid)
                .build()));

    // Invalid VSIDs are erased (even if present inside traceElement)
    assertEquals(
        new AclAclLine("name", "acl", _erasedVsid, null),
        _eraser.visit(new AclAclLine("name", "acl", _invalidTe, _invalidVsid)));
    assertEquals(
        new ExprAclLine(LineAction.PERMIT, new TrueExpr(_erasedVsid), "name", _erasedVsid, null),
        _eraser.visit(
            ExprAclLine.accepting()
                .setName("name")
                .setTraceElement(_invalidTe)
                .setMatchCondition(new TrueExpr(_invalidTe))
                .setVendorStructureId(_invalidVsid)
                .build()));
  }
}
