package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
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
}
