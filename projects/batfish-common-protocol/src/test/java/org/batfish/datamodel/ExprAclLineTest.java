package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

/** Tests of {@link ExprAclLine}. */
public class ExprAclLineTest {
  @Test
  public void testEquals() {
    ExprAclLine.Builder lineBuilder =
        ExprAclLine.builder()
            .setAction(LineAction.PERMIT)
            .setMatchCondition(FalseExpr.INSTANCE)
            .setName("name");
    new EqualsTester()
        .addEqualityGroup(
            lineBuilder.build(),
            lineBuilder.build(),
            new ExprAclLine(LineAction.PERMIT, FalseExpr.INSTANCE, "name"))
        .addEqualityGroup(lineBuilder.setAction(LineAction.DENY).build())
        .addEqualityGroup(lineBuilder.setMatchCondition(TrueExpr.INSTANCE).build())
        .addEqualityGroup(lineBuilder.setName("another name").build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    ExprAclLine l = new ExprAclLine(LineAction.PERMIT, OriginatingFromDevice.INSTANCE, "name");
    assertThat(BatfishObjectMapper.clone(l, ExprAclLine.class), equalTo(l));
  }
}
