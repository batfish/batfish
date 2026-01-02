package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.util.Optional;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class GlobalBroadcastNoPointToPointTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            GlobalBroadcastNoPointToPoint.instance(),
            SerializationUtils.clone(GlobalBroadcastNoPointToPoint.instance()))
        .testEquals();
  }

  @Test
  public void testMethods() {
    NodeInterfacePair nip = NodeInterfacePair.of("h", "i");
    NodeInterfacePair nip2 = NodeInterfacePair.of("h2", "i2");
    assertThat(
        GlobalBroadcastNoPointToPoint.instance().inSameBroadcastDomain(nip, nip2), equalTo(true));
    assertThat(
        GlobalBroadcastNoPointToPoint.instance().inSamePointToPointDomain(nip, nip2),
        equalTo(false));
    assertThat(
        GlobalBroadcastNoPointToPoint.instance().pairedPointToPointL3Interface(nip),
        equalTo(Optional.empty()));
  }
}
