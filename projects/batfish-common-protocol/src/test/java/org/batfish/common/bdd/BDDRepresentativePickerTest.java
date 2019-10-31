package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

public class BDDRepresentativePickerTest {
  @Test
  public void testBDDRepresentativePicker1() {
    BDDPacket pkt = new BDDPacket();
    BDD bdd1 = pkt.getIpProtocol().value(IpProtocol.UDP);
    BDD bdd2 = pkt.getIpProtocol().value(IpProtocol.TCP);
    BDD bdd = pkt.getIpProtocol().value(IpProtocol.UDP);
    BDD pickedBDD = BDDRepresentativePicker.pickRepresentative(bdd, ImmutableList.of(bdd1, bdd2));
    // check pickedBDD is in bdd
    assertThat(pickedBDD.and(bdd), equalTo(pickedBDD));
  }

  @Test
  public void testBDDRepresentativePicker2() {
    BDDPacket pkt = new BDDPacket();
    BDD bdd1 = pkt.getIpProtocol().value(IpProtocol.UDP);
    BDD bdd2 = pkt.getIpProtocol().value(IpProtocol.TCP);
    BDD bdd = pkt.getIpProtocol().value(IpProtocol.TCP);
    BDD pickedBDD = BDDRepresentativePicker.pickRepresentative(bdd, ImmutableList.of(bdd1, bdd2));
    // check pickedBDD is in bdd
    assertThat(pickedBDD.and(bdd), equalTo(pickedBDD));
  }
}
