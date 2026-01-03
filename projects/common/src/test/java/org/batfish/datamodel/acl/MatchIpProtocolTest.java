package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

public class MatchIpProtocolTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new MatchIpProtocol(IpProtocol.TCP, null), new MatchIpProtocol(IpProtocol.TCP, null))
        .addEqualityGroup(new MatchIpProtocol(IpProtocol.UDP, null))
        .addEqualityGroup(new MatchIpProtocol(IpProtocol.TCP, TraceElement.of("test")))
        .testEquals();
  }

  @Test
  public void testSerialization() throws IOException {
    MatchIpProtocol test1 = new MatchIpProtocol(IpProtocol.TCP, null);
    MatchIpProtocol test2 = new MatchIpProtocol(IpProtocol.TCP, TraceElement.of("test"));
    for (MatchIpProtocol t : ImmutableList.of(test1, test2)) {
      assertThat(BatfishObjectMapper.clone(t, AclLineMatchExpr.class), equalTo(t));
      assertThat(SerializationUtils.clone(t), equalTo(t));
    }
  }
}
