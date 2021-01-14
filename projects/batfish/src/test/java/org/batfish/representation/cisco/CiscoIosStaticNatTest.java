package org.batfish.representation.cisco;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class CiscoIosStaticNatTest {

  @Test
  public void testNatCompare() {
    CiscoIosStaticNat nat16 = natWithLocalNetwork("1.2.0.0/16");
    CiscoIosStaticNat nat24_1 = natWithLocalNetwork("1.2.3.0/24");
    CiscoIosStaticNat nat24_2 = natWithLocalNetwork("4.5.6.0/24");
    CiscoIosStaticNat nat32 = natWithLocalNetwork("1.2.3.4/32");

    // Rules with the same prefix length are considered the same priority (they match mutually
    // exclusive traffic)
    assertThat(nat24_1.compareTo(nat24_2), equalTo(0));
    assertThat(nat24_2.compareTo(nat24_1), equalTo(0));

    // Rules whose local networks are longer prefixes should come first
    List<CiscoIosStaticNat> ordered = ImmutableList.of(nat32, nat24_1, nat16);
    for (int i = 0; i < ordered.size() - 1; i++) {
      for (int j = i + 1; j < ordered.size(); j++) {
        assertThat(ordered.get(i).compareTo(ordered.get(j)), lessThan(0));
        assertThat(ordered.get(j).compareTo(ordered.get(i)), greaterThan(0));
      }
    }
  }

  private static CiscoIosStaticNat natWithLocalNetwork(String prefix) {
    CiscoIosStaticNat nat = new CiscoIosStaticNat();
    nat.setLocalNetwork(Prefix.parse(prefix));
    return nat;
  }
}
