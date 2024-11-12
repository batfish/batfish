package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

public class MatchDestinationIpTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new MatchDestinationIp(Ip.parse("1.1.1.1").toIpSpace(), null),
            new MatchDestinationIp(Ip.parse("1.1.1.1").toIpSpace(), null))
        .addEqualityGroup(new MatchDestinationIp(Ip.parse("2.2.2.2").toIpSpace(), null))
        .addEqualityGroup(
            new MatchDestinationIp(Ip.parse("1.1.1.1").toIpSpace(), TraceElement.of("test")))
        .testEquals();
  }

  @Test
  public void testSerialization() throws IOException {
    MatchDestinationIp test1 = new MatchDestinationIp(Ip.parse("1.1.1.1").toIpSpace(), null);
    MatchDestinationIp test2 =
        new MatchDestinationIp(Ip.parse("1.1.1.1").toIpSpace(), TraceElement.of("test"));
    for (MatchDestinationIp t : ImmutableList.of(test1, test2)) {
      assertThat(BatfishObjectMapper.clone(t, AclLineMatchExpr.class), equalTo(t));
      assertThat(SerializationUtils.clone(t), equalTo(t));
    }
  }
}
