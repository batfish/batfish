package org.batfish.common.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

public class BDDRepresentativePickerTest {
  @Test
  public void testBDDRepresentativePicker1() {
    BDDPacket pkt = new BDDPacket();
    BDD bdd1 = pkt.getIpProtocol().value(IpProtocol.UDP.number());
    BDD bdd2 = pkt.getIpProtocol().value(IpProtocol.TCP.number());
    BDDRepresentativePicker picker = new BDDRepresentativePicker(ImmutableList.of(bdd1, bdd2));
    BDD bdd = pkt.getIpProtocol().value(IpProtocol.UDP.number());
    BDD pickedBDD = picker.pickRepresentative(bdd);
    // check pickedBDD is in bdd
    assertThat(pickedBDD.and(bdd), equalTo(pickedBDD));
  }

  @Test
  public void testBDDRepresentativePicker2() {
    BDDPacket pkt = new BDDPacket();
    BDD bdd1 = pkt.getIpProtocol().value(IpProtocol.UDP.number());
    BDD bdd2 = pkt.getIpProtocol().value(IpProtocol.TCP.number());
    BDDRepresentativePicker picker = new BDDRepresentativePicker(ImmutableList.of(bdd1, bdd2));
    BDD bdd = pkt.getIpProtocol().value(IpProtocol.TCP.number());
    BDD pickedBDD = picker.pickRepresentative(bdd);
    // check pickedBDD is in bdd
    assertThat(pickedBDD.and(bdd), equalTo(pickedBDD));
  }
}
