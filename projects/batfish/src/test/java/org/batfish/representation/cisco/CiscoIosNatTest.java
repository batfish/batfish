package org.batfish.representation.cisco;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import org.junit.Test;

public class CiscoIosNatTest {
  @Test
  public void testCompareStaticVsDynamic() {
    // Static rules should always come before dynamic
    CiscoIosNat staticNat = new CiscoIosStaticNat();
    CiscoIosNat dynamicNat = new CiscoIosDynamicNat();
    assertThat(staticNat.compareTo(dynamicNat), lessThan(0));
    assertThat(dynamicNat.compareTo(staticNat), greaterThan(0));
  }
}
