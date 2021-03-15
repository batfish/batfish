package org.batfish.bddreachability;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class IpsRoutedOutInterfacesFactoryTest {
  private final BDDPacket _pkt = new BDDPacket();
  private final IpSpaceToBDD _dst = _pkt.getDstIpSpaceToBDD();

  @Test
  public void testComputeIpsRoutedOutInterfacesMap() {
    String iface1 = "iface1";
    String iface2 = "iface2";

    Prefix prefix1 = Prefix.parse("1.2.3.0/24");
    Prefix prefix2 = Prefix.parse("2.2.3.0/24");
    ConnectedRoute route1 = new ConnectedRoute(prefix1, iface1);
    ConnectedRoute route2 = new ConnectedRoute(prefix2, iface2);
    BDD prefix1Bdd = _dst.toBDD(prefix1);
    BDD prefix2Bdd = _dst.toBDD(prefix2);

    // empty fib
    {
      Fib fib = MockFib.builder().build();
      assertEquals(
          ImmutableMap.of(), IpsRoutedOutInterfacesFactory.computeIpsRoutedOutInterfacesMap(fib));
    }

    // single fib entry with missing matching Ips
    {
      Fib fib =
          MockFib.builder()
              .setFibEntries(
                  ImmutableMap.of(
                      Ip.ZERO,
                      ImmutableSet.of(
                          new FibEntry(new FibForward(Ip.ZERO, iface1), ImmutableList.of(route1)))))
              .build();
      Map<String, IpSpace> map =
          IpsRoutedOutInterfacesFactory.computeIpsRoutedOutInterfacesMap(fib);
      assertThat(map, equalTo(ImmutableMap.of(iface1, EmptyIpSpace.INSTANCE)));
    }

    // single fib entry with matching Ips
    {
      Fib fib =
          MockFib.builder()
              .setFibEntries(
                  ImmutableMap.of(
                      Ip.ZERO,
                      ImmutableSet.of(
                          new FibEntry(new FibForward(Ip.ZERO, iface1), ImmutableList.of(route1)))))
              .setMatchingIps(ImmutableMap.of(prefix1, prefix1.toIpSpace()))
              .build();
      Map<String, IpSpace> map =
          IpsRoutedOutInterfacesFactory.computeIpsRoutedOutInterfacesMap(fib);
      assertEquals(1, map.size());
      assertEquals(prefix1Bdd, _dst.visit(map.get(iface1)));
    }

    // two fib entries
    {
      Fib fib =
          MockFib.builder()
              .setFibEntries(
                  ImmutableMap.of(
                      Ip.ZERO,
                      ImmutableSet.of(
                          new FibEntry(new FibForward(Ip.ZERO, iface1), ImmutableList.of(route1)),
                          new FibEntry(new FibForward(Ip.ZERO, iface2), ImmutableList.of(route2)))))
              .setMatchingIps(
                  ImmutableMap.of(prefix1, prefix1.toIpSpace(), prefix2, prefix2.toIpSpace()))
              .build();
      Map<String, IpSpace> map =
          IpsRoutedOutInterfacesFactory.computeIpsRoutedOutInterfacesMap(fib);
      assertEquals(2, map.size());
      assertEquals(prefix1Bdd, _dst.visit(map.get(iface1)));
      assertEquals(prefix2Bdd, _dst.visit(map.get(iface2)));
    }
  }
}
