package org.batfish.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.SortedMap;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class InvalidVendorStructureIdEraserTest {

  @Test
  public void testAclLineMatchExprs() {
    SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
        definedStructures =
            ImmutableSortedMap.of(
                "filename",
                ImmutableSortedMap.of(
                    "structureType",
                    ImmutableSortedMap.of("structureName", new DefinedStructureInfo())));
    InvalidVendorStructureIdEraser eraser = new InvalidVendorStructureIdEraser(definedStructures);
    VendorStructureId validVsid =
        new VendorStructureId("filename", "structureType", "structureName");
    VendorStructureId invalidVsid =
        new VendorStructureId("filename", "structureType", "otherStructureName");

    TraceElement validTe = TraceElement.builder().add("valid", validVsid).build();
    TraceElement invalidTe = TraceElement.builder().add("invalid", invalidVsid).build();
    // Same as above trace element, but with VSID removed
    TraceElement erasedVsid = TraceElement.builder().add("invalid").build();

    {
      TrueExpr exprValid = new TrueExpr(validTe);
      TrueExpr exprInvalid = new TrueExpr(invalidTe);
      TrueExpr exprInvalidErased = new TrueExpr(erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      FalseExpr exprValid = new FalseExpr(validTe);
      FalseExpr exprInvalid = new FalseExpr(invalidTe);
      FalseExpr exprInvalidErased = new FalseExpr(erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      assertEquals(OriginatingFromDevice.INSTANCE, eraser.visit(OriginatingFromDevice.INSTANCE));
    }

    {
      PermittedByAcl exprValid = new PermittedByAcl("acl", validTe);
      PermittedByAcl exprInvalid = new PermittedByAcl("acl", invalidTe);
      PermittedByAcl exprInvalidErased = new PermittedByAcl("acl", erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      DeniedByAcl exprValid = new DeniedByAcl("acl", validTe);
      DeniedByAcl exprInvalid = new DeniedByAcl("acl", invalidTe);
      DeniedByAcl exprInvalidErased = new DeniedByAcl("acl", erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      NotMatchExpr exprValid = new NotMatchExpr(TrueExpr.INSTANCE, validTe);
      NotMatchExpr exprInvalid = new NotMatchExpr(TrueExpr.INSTANCE, invalidTe);
      NotMatchExpr exprInvalidErased = new NotMatchExpr(TrueExpr.INSTANCE, erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      List<String> ifaces = ImmutableList.of("i");
      MatchSrcInterface exprValid = new MatchSrcInterface(ifaces, validTe);
      MatchSrcInterface exprInvalid = new MatchSrcInterface(ifaces, invalidTe);
      MatchSrcInterface exprInvalidErased = new MatchSrcInterface(ifaces, erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      List<AclLineMatchExpr> validInners = ImmutableList.of(new TrueExpr(validTe));
      List<AclLineMatchExpr> invalidInners = ImmutableList.of(new TrueExpr(invalidTe));
      List<AclLineMatchExpr> erasedInners = ImmutableList.of(new TrueExpr(erasedVsid));
      AndMatchExpr exprValid = new AndMatchExpr(validInners, validTe);
      AndMatchExpr exprInvalid = new AndMatchExpr(invalidInners, invalidTe);
      AndMatchExpr exprInvalidErased = new AndMatchExpr(erasedInners, erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      List<AclLineMatchExpr> validInners = ImmutableList.of(new TrueExpr(validTe));
      List<AclLineMatchExpr> invalidInners = ImmutableList.of(new TrueExpr(invalidTe));
      List<AclLineMatchExpr> erasedInners = ImmutableList.of(new TrueExpr(erasedVsid));
      OrMatchExpr exprValid = new OrMatchExpr(validInners, validTe);
      OrMatchExpr exprInvalid = new OrMatchExpr(invalidInners, invalidTe);
      OrMatchExpr exprInvalidErased = new OrMatchExpr(erasedInners, erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }

    {
      HeaderSpace headerSpace = HeaderSpace.builder().setDstIps(UniverseIpSpace.INSTANCE).build();
      MatchHeaderSpace exprValid = new MatchHeaderSpace(headerSpace, validTe);
      MatchHeaderSpace exprInvalid = new MatchHeaderSpace(headerSpace, invalidTe);
      MatchHeaderSpace exprInvalidErased = new MatchHeaderSpace(headerSpace, erasedVsid);
      // Valid VSID is left alone
      assertEquals(exprValid, eraser.visit(exprValid));
      // Invalid VSID is removed
      assertEquals(exprInvalidErased, eraser.visit(exprInvalid));
    }
  }

  @Test
  public void testTraceElement() {
    SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
        definedStructures =
            ImmutableSortedMap.of(
                "filename",
                ImmutableSortedMap.of(
                    "structureType",
                    ImmutableSortedMap.of("structureName", new DefinedStructureInfo())));
    InvalidVendorStructureIdEraser eraser = new InvalidVendorStructureIdEraser(definedStructures);
    VendorStructureId validVsid =
        new VendorStructureId("filename", "structureType", "structureName");
    VendorStructureId invalidVsid =
        new VendorStructureId("filename", "structureType", "otherStructureName");

    // Valid and flat-text trace elements are left unchanged
    assertEquals(
        TraceElement.builder().add("flatText").add("valid", validVsid).build(),
        eraser.eraseInvalid(
            TraceElement.builder().add("flatText").add("valid", validVsid).build()));
    // Link fragment with invalid VSID should be flattened to a text fragment
    assertEquals(
        TraceElement.builder().add("invalid").build(),
        eraser.eraseInvalid(TraceElement.builder().add("invalid", invalidVsid).build()));
  }

  @Test
  public void testAclLines() {
    SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
        definedStructures =
            ImmutableSortedMap.of(
                "filename",
                ImmutableSortedMap.of(
                    "structureType",
                    ImmutableSortedMap.of("structureName", new DefinedStructureInfo())));
    InvalidVendorStructureIdEraser eraser = new InvalidVendorStructureIdEraser(definedStructures);
    VendorStructureId validVsid =
        new VendorStructureId("filename", "structureType", "structureName");
    VendorStructureId invalidVsid =
        new VendorStructureId("filename", "structureType", "otherStructureName");
    TraceElement validTraceElement =
        TraceElement.builder().add("valid link text", validVsid).build();
    TraceElement invalidTraceElement =
        TraceElement.builder().add("invalid link text", invalidVsid).build();
    TraceElement erasedTraceElement = TraceElement.builder().add("invalid link text").build();

    // Valid VSIDs are left alone
    assertEquals(
        new AclAclLine("name", "acl", validTraceElement, validVsid),
        eraser.visit(new AclAclLine("name", "acl", validTraceElement, validVsid)));
    assertEquals(
        new ExprAclLine(
            LineAction.PERMIT,
            new TrueExpr(validTraceElement),
            "name",
            validTraceElement,
            validVsid),
        eraser.visit(
            ExprAclLine.accepting()
                .setName("name")
                .setTraceElement(validTraceElement)
                .setMatchCondition(new TrueExpr(validTraceElement))
                .setVendorStructureId(validVsid)
                .build()));

    // Invalid VSIDs are erased (even if present inside traceElement)
    assertEquals(
        new AclAclLine("name", "acl", erasedTraceElement, null),
        eraser.visit(new AclAclLine("name", "acl", invalidTraceElement, invalidVsid)));
    assertEquals(
        new ExprAclLine(
            LineAction.PERMIT, new TrueExpr(erasedTraceElement), "name", erasedTraceElement, null),
        eraser.visit(
            ExprAclLine.accepting()
                .setName("name")
                .setTraceElement(invalidTraceElement)
                .setMatchCondition(new TrueExpr(invalidTraceElement))
                .setVendorStructureId(invalidVsid)
                .build()));
  }

  @Test
  public void testIsVendorStructureIdValid() {
    SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
        definedStructures =
            ImmutableSortedMap.of(
                "filename",
                ImmutableSortedMap.of(
                    "structureType",
                    ImmutableSortedMap.of("structureName", new DefinedStructureInfo())));
    InvalidVendorStructureIdEraser eraser = new InvalidVendorStructureIdEraser(definedStructures);

    assertTrue(
        eraser.isVendorStructureIdValid(
            new VendorStructureId("filename", "structureType", "structureName")));
    assertFalse(
        eraser.isVendorStructureIdValid(
            new VendorStructureId("otherFilename", "structureType", "structureName")));
    assertFalse(
        eraser.isVendorStructureIdValid(
            new VendorStructureId("filename", "otherStructureType", "structureName")));
    assertFalse(
        eraser.isVendorStructureIdValid(
            new VendorStructureId("filename", "structureType", "otherStructureName")));
  }
}
