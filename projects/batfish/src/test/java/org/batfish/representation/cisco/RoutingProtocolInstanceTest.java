package org.batfish.representation.cisco;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.junit.Test;

/** Tests of {@link RoutingProtocolInstance}. */
public class RoutingProtocolInstanceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(RoutingProtocolInstance.bgp(10L), RoutingProtocolInstance.bgp(10L))
        .addEqualityGroup(RoutingProtocolInstance.bgp(20L))
        .addEqualityGroup(RoutingProtocolInstance.connected())
        .addEqualityGroup(RoutingProtocolInstance.eigrp(10L), RoutingProtocolInstance.eigrp(10L))
        .addEqualityGroup(RoutingProtocolInstance.eigrp(20L))
        .addEqualityGroup(RoutingProtocolInstance.ospf())
        .addEqualityGroup(RoutingProtocolInstance.rip())
        .addEqualityGroup(RoutingProtocolInstance.isis_l1())
        .addEqualityGroup(RoutingProtocolInstance.staticRoutingProtocol())
        .testEquals();
  }

  @Test
  public void testSorting() {
    List<RoutingProtocolInstance> entries =
        ImmutableList.of(
            RoutingProtocolInstance.bgp(5),
            RoutingProtocolInstance.bgp(6),
            RoutingProtocolInstance.eigrp(6),
            RoutingProtocolInstance.eigrp(7),
            RoutingProtocolInstance.ospf());
    assertThat(ImmutableList.sortedCopyOf(entries), equalTo(entries));
    assertThat(ImmutableList.sortedCopyOf(Lists.reverse(entries)), equalTo(entries));
  }
}
