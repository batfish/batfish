package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link PacketPolicy} */
public class PacketPolicyTest {

  private final Return _defaultAction = new Return(new FibLookup(new LiteralVrfName("default")));

  @Test
  public void testEquals() {
    PacketPolicy p =
        new PacketPolicy("name", ImmutableList.of(new Return(Drop.instance())), _defaultAction);
    new EqualsTester()
        .addEqualityGroup(
            p,
            p,
            new PacketPolicy("name", ImmutableList.of(new Return(Drop.instance())), _defaultAction))
        .addEqualityGroup(
            new PacketPolicy(
                "otherName", ImmutableList.of(new Return(Drop.instance())), _defaultAction))
        .addEqualityGroup(new PacketPolicy("name", ImmutableList.of(), _defaultAction))
        .addEqualityGroup(new PacketPolicy("name", ImmutableList.of(), new Return(Drop.instance())))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    PacketPolicy p =
        new PacketPolicy("name", ImmutableList.of(new Return(Drop.instance())), _defaultAction);
    assertThat(SerializationUtils.clone(p), equalTo(p));
  }

  @Test
  public void testJsonSerialization() {
    PacketPolicy p =
        new PacketPolicy("name", ImmutableList.of(new Return(Drop.instance())), _defaultAction);
    assertThat(BatfishObjectMapper.clone(p, PacketPolicy.class), equalTo(p));
  }

  @Test
  public void testToString() {
    PacketPolicy p =
        new PacketPolicy("name", ImmutableList.of(new Return(Drop.instance())), _defaultAction);
    assertTrue(p.toString().contains(p.getClass().getSimpleName()));
    assertTrue(p.toString().contains("name"));
    assertTrue(p.toString().contains(p.getName()));
  }
}
