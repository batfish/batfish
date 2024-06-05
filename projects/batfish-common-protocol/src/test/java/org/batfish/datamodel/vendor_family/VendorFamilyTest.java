package org.batfish.datamodel.vendor_family;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import org.batfish.datamodel.vendor_family.VendorFamily.Type;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.juniper.JuniperFamily;
import org.junit.Test;

public class VendorFamilyTest {

  @Test
  public void toStringMulitipleFamilies() {
    VendorFamily family = new VendorFamily();
    family.setCisco(new CiscoFamily());
    family.setJuniper(new JuniperFamily());
    assertThat(
        family.toString(),
        equalTo(String.join(" ", Arrays.asList(Type.CISCO.toString(), Type.JUNIPER.toString()))));
  }

  @Test
  public void toStringNoFamily() {
    VendorFamily family = new VendorFamily();
    assertThat(family.toString(), equalTo(""));
  }

  @Test
  public void toStringOneFamily() {
    VendorFamily family = new VendorFamily();
    family.setCisco(new CiscoFamily());
    assertThat(family.toString(), equalTo(Type.CISCO.toString()));
  }
}
