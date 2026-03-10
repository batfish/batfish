package org.batfish.datamodel;

import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

/** Tests of {@link ExprAclLine}. */
public class ExprAclLineTest {
  @Test
  public void testEquals() {
    TraceElement traceElement1 = TraceElement.of("a");
    TraceElement traceElement2 = TraceElement.of("b");
    VendorStructureId vsId1 = new VendorStructureId("a", "b", "c");
    VendorStructureId vsId2 = null;
    ExprAclLine.Builder lineBuilder =
        ExprAclLine.builder()
            .setAction(LineAction.PERMIT)
            .setMatchCondition(FalseExpr.INSTANCE)
            .setName("name")
            .setTraceElement(traceElement1)
            .setVendorStructureId(vsId1);
    new EqualsTester()
        .addEqualityGroup(
            lineBuilder.build(),
            lineBuilder.build(),
            new ExprAclLine(LineAction.PERMIT, FalseExpr.INSTANCE, "name", traceElement1, vsId1))
        .addEqualityGroup(lineBuilder.setAction(LineAction.DENY).build())
        .addEqualityGroup(lineBuilder.setMatchCondition(TrueExpr.INSTANCE).build())
        .addEqualityGroup(lineBuilder.setName("another name").build())
        .addEqualityGroup(lineBuilder.setTraceElement(traceElement2).build())
        .addEqualityGroup(lineBuilder.setVendorStructureId(vsId2).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    ExprAclLine line =
        ExprAclLine.builder()
            .setAction(LineAction.PERMIT)
            .setMatchCondition(TRUE)
            .setName("name")
            .setTraceElement(TraceElement.of("a"))
            .build();
    assertEquals(line, line.toBuilder().build());
  }

  @Test
  public void testJsonSerialization() {
    {
      ExprAclLine l = new ExprAclLine(LineAction.PERMIT, OriginatingFromDevice.INSTANCE, "name");
      ExprAclLine clone = (ExprAclLine) BatfishObjectMapper.clone(l, AclLine.class);
      assertEquals(l, clone);
    }
    {
      ExprAclLine l =
          new ExprAclLine(
              LineAction.PERMIT,
              OriginatingFromDevice.INSTANCE,
              "name",
              TraceElement.builder().add("a").build(),
              new VendorStructureId("a", "b", "c"));
      ExprAclLine clone = (ExprAclLine) BatfishObjectMapper.clone(l, AclLine.class);
      assertEquals(l, clone);
    }
  }
}
