package org.batfish.datamodel.visitors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.ip.AclIpSpace;
import org.batfish.common.ip.EmptyIpSpace;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.IpSpace;
import org.batfish.common.ip.IpSpaceReference;
import org.batfish.common.ip.Prefix;
import org.batfish.common.ip.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceDereferencerTest {
  private static final Map<String, IpSpace> NAMED_IP_SPACES =
      ImmutableMap.of(
          "empty",
          EmptyIpSpace.INSTANCE,
          "ip",
          Ip.parse("1.2.3.4").toIpSpace(),
          "prefix",
          Prefix.parse("1.0.0.0/8").toIpSpace(),
          "namedIp",
          new IpSpaceReference("ip"));

  @Test
  public void testAclIpSpace() {
    IpSpaceDereferencer dereferencer = new IpSpaceDereferencer(NAMED_IP_SPACES);
    AclIpSpace input =
        (AclIpSpace)
            AclIpSpace.builder()
                .thenPermitting(new IpSpaceReference("empty"))
                .thenRejecting(new IpSpaceReference("prefix"))
                .thenPermitting(new IpSpaceReference("namedIp"))
                .thenRejecting(UniverseIpSpace.INSTANCE)
                .build();

    IpSpace expected =
        AclIpSpace.builder()
            .thenPermitting(NAMED_IP_SPACES.get("empty"))
            .thenRejecting(NAMED_IP_SPACES.get("prefix"))
            .thenPermitting(NAMED_IP_SPACES.get("ip"))
            .thenRejecting(UniverseIpSpace.INSTANCE)
            .build();
    assertThat(dereferencer.visitAclIpSpace(input), equalTo(expected));
  }
}
