package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Test;

public class MemoizedIpSpaceToBDDTest {
  private static final IpSpace IP1_IP_SPACE = Ip.parse("1.1.1.1").toIpSpace();
  private static final IpSpace IP2_IP_SPACE = Ip.parse("2.2.2.2").toIpSpace();
  private static final IpSpace ACL_IP_SPACE =
      AclIpSpace.builder().thenPermitting(IP1_IP_SPACE).thenPermitting(IP2_IP_SPACE).build();

  private MemoizedIpSpaceToBDD _toBdd;

  @Before
  public void setup() {
    BDDPacket pkt = new BDDPacket();
    _toBdd = new MemoizedIpSpaceToBDD(pkt.getDstIp(), ImmutableMap.of());
  }

  @Test
  public void testVisit() {
    assertThat(_toBdd.getMemoizedBdd(IP1_IP_SPACE), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(IP1_IP_SPACE);
    assertThat(_toBdd.getMemoizedBdd(IP1_IP_SPACE), equalTo(Optional.of(bdd)));
  }

  @Test
  public void testRecursion() {
    assertThat(_toBdd.getMemoizedBdd(IP1_IP_SPACE), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBdd(IP2_IP_SPACE), equalTo(Optional.empty()));
    assertThat(_toBdd.getMemoizedBdd(ACL_IP_SPACE), equalTo(Optional.empty()));
    BDD bdd = _toBdd.visit(ACL_IP_SPACE);
    assertTrue("IP1_IP_SPACE should be memoized", _toBdd.getMemoizedBdd(IP1_IP_SPACE).isPresent());
    assertTrue("IP2_IP_SPACE should be memoized", _toBdd.getMemoizedBdd(IP2_IP_SPACE).isPresent());
    assertThat(_toBdd.getMemoizedBdd(ACL_IP_SPACE), equalTo(Optional.of(bdd)));
  }

  /**
   * A test that {@link AclIpSpace} memoization is feasible. This test finishes in well under a
   * second on a laptop (2017 Macbook Pro 13"), but times out in several prior implementations of
   * {@link MemoizedIpSpaceToBDD}.
   */
  @Test(timeout = 60_000)
  public void testDeepAclIpSpace() {
    int depth = 15;
    long ip = Ip.parse("1.2.3.3").asLong();
    IpSpace current =
        AclIpSpace.builder()
            .thenRejecting(Ip.parse("1.2.3.4").toIpSpace())
            .thenPermitting(Prefix.parse("1.0.0.0/8").toIpSpace())
            .thenRejecting(Ip.parse("2.2.3.4").toIpSpace())
            .thenPermitting(Prefix.parse("2.0.0.0/8").toIpSpace())
            .build();
    assertThat(current, instanceOf(AclIpSpace.class));
    for (int i = 0; i < depth; ++i) {
      current =
          AclIpSpace.permitting(Ip.create(ip++).toIpSpace())
              .thenRejecting(current)
              .thenPermitting(current.complement())
              .thenRejecting(current.complement())
              .thenPermitting(current.complement())
              .thenRejecting(current.complement())
              .thenPermitting(current.complement())
              .build();
    }
    _toBdd.visit(current);
    IpSpace cloned = SerializationUtils.clone(current);
    _toBdd.visit(cloned);
  }
}
