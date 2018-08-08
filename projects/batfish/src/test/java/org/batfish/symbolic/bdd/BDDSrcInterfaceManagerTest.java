package org.batfish.symbolic.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.junit.Test;

public class BDDSrcInterfaceManagerTest {
  private static final String IFACE1 = "iface1";

  private static final String IFACE2 = "iface2";

  private static final List<String> IFACES = ImmutableList.of(IFACE1, IFACE2);

  @Test
  public void test() {
    BDDPacket pkt = new BDDPacket();
    BDDSrcManager mgr = new BDDSrcManager(pkt, IFACES);
    BDD bdd1 = mgr.getSrcInterfaceBDD(IFACE1);
    BDD bdd2 = mgr.getSrcInterfaceBDD(IFACE2);
    assertThat(mgr.getInterfaceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));
    assertThat(mgr.getInterfaceFromAssignment(bdd2), equalTo(Optional.of(IFACE2)));
    assertThat(
        mgr.getInterfaceFromAssignment(mgr.getSrcInterfaceVar().value(0)),
        equalTo(Optional.empty()));
  }
}
