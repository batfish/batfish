package org.batfish.representation.cumulus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Test for {@link CumulusNcluConfiguration}. */
public class CumulusNcluConfigurationTest {
  @Test
  public void testToInterface_active() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_inactive() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertFalse(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_active() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_inactive() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertFalse(viIface.getActive());
  }
}
