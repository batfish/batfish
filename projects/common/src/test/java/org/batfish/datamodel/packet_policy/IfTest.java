package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

/** Tests of {@link If} */
public class IfTest {

  @Test
  public void testEquals() {
    If ifExpr = new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of());
    new EqualsTester()
        .addEqualityGroup(
            ifExpr, ifExpr, new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of()))
        .addEqualityGroup(new If(new PacketMatchExpr(TrueExpr.INSTANCE), ImmutableList.of()))
        .addEqualityGroup(
            new If(
                new PacketMatchExpr(FalseExpr.INSTANCE),
                ImmutableList.of(new Return(Drop.instance()))))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    If ifExpr = new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of());
    assertThat(SerializationUtils.clone(ifExpr), equalTo(ifExpr));
  }

  @Test
  public void testJsonSerialization() {
    If ifExpr = new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of());
    assertThat(BatfishObjectMapper.clone(ifExpr, If.class), equalTo(ifExpr));
  }

  @Test
  public void testToString() {
    If ifExpr = new If(new PacketMatchExpr(FalseExpr.INSTANCE), ImmutableList.of());
    assertTrue(ifExpr.toString().contains(ifExpr.getClass().getSimpleName()));
  }
}
