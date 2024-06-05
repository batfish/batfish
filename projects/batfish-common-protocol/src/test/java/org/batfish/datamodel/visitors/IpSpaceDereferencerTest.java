package org.batfish.datamodel.visitors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
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
