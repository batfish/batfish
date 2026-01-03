package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests of {@link BgpAggregate} */
public final class BgpAggregateTest {

  @Test
  public void testJavaSerialization() {
    BgpAggregate obj = BgpAggregate.of(Prefix.ZERO, "a", "b", "c");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJsonSerialization() {
    BgpAggregate obj = BgpAggregate.of(Prefix.ZERO, "a", "b", "c");
    assertThat(BatfishObjectMapper.clone(obj, BgpAggregate.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    BgpAggregate obj = BgpAggregate.of(Prefix.ZERO, null, null, null);
    new EqualsTester()
        .addEqualityGroup(obj, BgpAggregate.of(Prefix.ZERO, null, null, null))
        .addEqualityGroup(BgpAggregate.of(Prefix.ZERO, "a", null, null))
        .addEqualityGroup(BgpAggregate.of(Prefix.ZERO, "a", "b", null))
        .addEqualityGroup(BgpAggregate.of(Prefix.ZERO, "a", "b", "c"))
        .addEqualityGroup(BgpAggregate.of(Prefix.MULTICAST, "a", "b", "c"))
        .testEquals();
  }
}
