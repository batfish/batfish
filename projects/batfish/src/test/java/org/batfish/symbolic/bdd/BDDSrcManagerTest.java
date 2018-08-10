package org.batfish.symbolic.bdd;

import static org.batfish.symbolic.bdd.BDDMatchers.isZero;
import static org.batfish.symbolic.bdd.BDDOps.orNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.junit.Test;

public class BDDSrcManagerTest {
  private static final String IFACE1 = "iface1";

  private static final String IFACE2 = "iface2";

  private static final List<String> IFACES = ImmutableList.of(IFACE1, IFACE2);

  BDDPacket _pkt = new BDDPacket();

  BDDSrcManager _mgr = new BDDSrcManager(_pkt, IFACES);

  @Test
  public void test() {
    BDD bdd1 = _mgr.getSrcInterfaceBDD(IFACE1);
    BDD bdd2 = _mgr.getSrcInterfaceBDD(IFACE2);
    assertThat(_mgr.getInterfaceFromAssignment(bdd1), equalTo(Optional.of(IFACE1)));
    assertThat(_mgr.getInterfaceFromAssignment(bdd2), equalTo(Optional.of(IFACE2)));
    assertThat(
        _mgr.getInterfaceFromAssignment(_mgr.getSrcInterfaceVar().value(0)),
        equalTo(Optional.empty()));
  }

  @Test
  public void testSane() {
    BDD noSource =
        orNull(
                _mgr.getOriginatingFromDeviceBDD(),
                _mgr.getSrcInterfaceBDD(IFACE1),
                _mgr.getSrcInterfaceBDD(IFACE2))
            .not();
    assertThat(_mgr.isSane().and(noSource), isZero());
  }
}
