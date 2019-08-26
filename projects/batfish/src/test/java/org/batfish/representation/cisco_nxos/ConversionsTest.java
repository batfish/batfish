package org.batfish.representation.cisco_nxos;

import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_3000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_5000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_6000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_7000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.UNKNOWN;
import static org.batfish.representation.cisco_nxos.Conversions.inferPlatform;
import static org.batfish.representation.cisco_nxos.Conversions.inferPlatformFromImage;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Test of {@link Conversions}. */
public final class ConversionsTest {

  @Test
  public void testInferPlatformFromImage() {
    //// Nexus 3000
    // kickstart
    assertThat(
        inferPlatformFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U3.2.bin"),
        equalTo(NEXUS_3000));
    assertThat(
        inferPlatformFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U3.5.bin"),
        equalTo(NEXUS_3000));
    assertThat(
        inferPlatformFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U3.7.bin"),
        equalTo(NEXUS_3000));
    assertThat(
        inferPlatformFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U6.6.bin"),
        equalTo(NEXUS_3000));
    // system
    assertThat(inferPlatformFromImage("bootflash:/n3000-uk9.6.0.2.U3.2.bin"), equalTo(NEXUS_3000));
    assertThat(inferPlatformFromImage("bootflash:/n3000-uk9.6.0.2.U3.5.bin"), equalTo(NEXUS_3000));
    assertThat(inferPlatformFromImage("bootflash:/n3000-uk9.6.0.2.U3.7.bin"), equalTo(NEXUS_3000));
    assertThat(inferPlatformFromImage("bootflash:/n3000-uk9.6.0.2.U6.6.bin"), equalTo(NEXUS_3000));

    //// Nexus 5000
    // kickstart
    assertThat(
        inferPlatformFromImage("bootflash:/n5000-uk9-kickstart.5.1.3.N1.1a.bin"),
        equalTo(NEXUS_5000));
    assertThat(
        inferPlatformFromImage("bootflash:/n5000-uk9-kickstart.5.1.3.N2.1c.bin"),
        equalTo(NEXUS_5000));
    assertThat(
        inferPlatformFromImage("bootflash:/n5000-uk9-kickstart.5.2.1.N1.9.bin"),
        equalTo(NEXUS_5000));
    assertThat(
        inferPlatformFromImage("bootflash:/n5000-uk9-kickstart.6.0.2.N2.5.bin"),
        equalTo(NEXUS_5000));
    assertThat(
        inferPlatformFromImage("bootflash:/n5000-uk9-kickstart.7.0.6.N1.1.bin"),
        equalTo(NEXUS_5000));
    // system
    assertThat(inferPlatformFromImage("bootflash:/n5000-uk9.5.1.3.N1.1a.bin"), equalTo(NEXUS_5000));
    assertThat(inferPlatformFromImage("bootflash:/n5000-uk9.5.1.3.N2.1c.bin"), equalTo(NEXUS_5000));
    assertThat(inferPlatformFromImage("bootflash:/n5000-uk9.5.2.1.N1.9.bin"), equalTo(NEXUS_5000));
    assertThat(inferPlatformFromImage("bootflash:/n5000-uk9.6.0.2.N2.5.bin"), equalTo(NEXUS_5000));
    assertThat(inferPlatformFromImage("bootflash:/n5000-uk9.7.0.6.N1.1.bin"), equalTo(NEXUS_5000));

    //// Nexus 6000
    // kickstart
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.6.0.2.N2.3.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.6.0.2.N2.4.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.0.2.N1.1.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.0.5.N1.1a.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.0.5.N1.1a.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.0.8.N1.1a.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.0.8.N1.1.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.1.0.N1.1b.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.1.4.N1.1.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.3.0.N1.1.bin"),
        equalTo(NEXUS_6000));
    assertThat(
        inferPlatformFromImage("bootflash:/n6000-uk9-kickstart.7.3.2.N1.1.bin"),
        equalTo(NEXUS_6000));
    // system
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.6.0.2.N2.3.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.6.0.2.N2.4.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.0.2.N1.1.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.0.5.N1.1a.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.0.5.N1.1a.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.0.8.N1.1a.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.0.8.N1.1.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.1.0.N1.1b.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.1.4.N1.1.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.3.0.N1.1.bin"), equalTo(NEXUS_6000));
    assertThat(inferPlatformFromImage("bootflash:/n6000-uk9.7.3.2.N1.1.bin"), equalTo(NEXUS_6000));

    //// Nexus 7000
    // kickstart
    assertThat(
        inferPlatformFromImage("bootflash:/n7000-s2-kickstart.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(
        inferPlatformFromImage("bootflash:/n7000-s2-kickstart.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(
        inferPlatformFromImage("bootflash:/n7700-s2-kickstart.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(
        inferPlatformFromImage("bootflash:/n7700-s2-kickstart.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(
        inferPlatformFromImage("bootflash:/titanium-d1-kickstart.7.3.0.D1.1.bin"),
        equalTo(NEXUS_7000));
    // system
    assertThat(inferPlatformFromImage("bootflash:/n7000-s2-dk9.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(inferPlatformFromImage("bootflash:/n7000-s2-dk9.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(inferPlatformFromImage("bootflash:/n7700-s2-dk9.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(inferPlatformFromImage("bootflash:/n7700-s2-dk9.6.2.16.bin"), equalTo(NEXUS_7000));
    assertThat(
        inferPlatformFromImage("bootflash:/titanium-d1.7.3.0.D1.1.bin"), equalTo(NEXUS_7000));

    //// Nexus 9000
    // NOTE: nxos.9.2.3.bin could be either Nexus 3000 or Nexus 9000.
    // TODO: find some other way to uniquely identify Nexus 9000.
    assertThat(inferPlatformFromImage("bootflash:/nxos.9.2.3.bin"), nullValue());
  }

  @Test
  public void testInferPlatform() {
    {
      CiscoNxosConfiguration vc = new CiscoNxosConfiguration();
      // TODO: something better with multi-platform images
      assertThat(inferPlatform(vc), equalTo(UNKNOWN));
      vc.setBootNxosSup2("bootflash:/nxos.9.2.3.bin");
      assertThat(inferPlatform(vc), equalTo(UNKNOWN));
      vc.setBootNxosSup1("bootflash:/nxos.9.2.3.bin");
      assertThat(inferPlatform(vc), equalTo(UNKNOWN));
    }
    {
      CiscoNxosConfiguration vc = new CiscoNxosConfiguration();
      assertThat(inferPlatform(vc), equalTo(UNKNOWN));
      vc.setBootKickstartSup2("bootflash:/n7000-s2-kickstart.6.2.16.bin");
      assertThat(inferPlatform(vc), equalTo(NEXUS_7000));
      vc.setBootKickstartSup1("bootflash:/n6000-uk9-kickstart.6.0.2.N2.3.bin");
      assertThat(inferPlatform(vc), equalTo(NEXUS_6000));
      vc.setBootSystemSup2("bootflash:/n5000-uk9.5.1.3.N1.1a.bin");
      assertThat(inferPlatform(vc), equalTo(NEXUS_5000));
      vc.setBootSystemSup1("bootflash:/n3000-uk9.6.0.2.U3.2.bin");
      assertThat(inferPlatform(vc), equalTo(NEXUS_3000));
    }
  }
}
