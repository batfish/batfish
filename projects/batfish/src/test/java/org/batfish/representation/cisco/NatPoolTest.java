package org.batfish.representation.cisco;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class NatPoolTest {
  @Test
  public void testCreate() {
    Prefix p = Prefix.parse("1.1.1.0/24");
    Ip first = Ip.parse("1.1.1.10");
    Ip last = Ip.parse("1.1.1.20");
    NatPool pool = NatPool.create(first, last, p);
    assertEquals(first, pool.getFirst());
    assertEquals(last, pool.getLast());
  }

  @Test
  public void testCreate_network() {
    Prefix p = Prefix.parse("1.1.1.0/24");
    Ip ip = p.getStartIp();
    NatPool pool = NatPool.create(ip, ip, p);
    assertEquals(p.getFirstHostIp(), pool.getFirst());
    assertEquals(p.getFirstHostIp(), pool.getLast());
  }

  @Test
  public void testCreate_broadcast() {
    Prefix p = Prefix.parse("1.1.1.0/24");
    Ip ip = p.getEndIp();
    NatPool pool = NatPool.create(ip, ip, p);
    assertEquals(p.getLastHostIp(), pool.getFirst());
    assertEquals(p.getLastHostIp(), pool.getLast());
  }

  @Test
  public void testCreate_outside() {
    Prefix p = Prefix.parse("1.1.1.0/24");
    Ip first = Ip.parse("1.1.0.1");
    Ip last = Ip.parse("1.1.2.1");
    NatPool pool = NatPool.create(first, last, p);
    assertEquals(p.getFirstHostIp(), pool.getFirst());
    assertEquals(p.getLastHostIp(), pool.getLast());
  }
}
