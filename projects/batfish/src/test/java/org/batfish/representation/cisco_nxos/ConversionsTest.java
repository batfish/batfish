package org.batfish.representation.cisco_nxos;

import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_3000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_5000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_6000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform.NEXUS_7000;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NxosMajorVersion.NXOS5;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NxosMajorVersion.NXOS6;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NxosMajorVersion.NXOS7;
import static org.batfish.datamodel.vendor_family.cisco_nxos.NxosMajorVersion.NXOS9;
import static org.batfish.representation.cisco_nxos.Conversions.inferMajorVersion;
import static org.batfish.representation.cisco_nxos.Conversions.inferMajorVersionFromImage;
import static org.batfish.representation.cisco_nxos.Conversions.inferMajorVersionFromVersion;
import static org.batfish.representation.cisco_nxos.Conversions.inferPlatform;
import static org.batfish.representation.cisco_nxos.Conversions.inferPlatformFromImage;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform;
import org.batfish.datamodel.vendor_family.cisco_nxos.NxosMajorVersion;
import org.junit.Test;

/** Test of {@link Conversions}. */
public final class ConversionsTest {

  @Test
  public void testInferMajorVersionFromImage() {
    //// Nexus 3000
    // kickstart
    assertThat(
        inferMajorVersionFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U3.2.bin"),
        equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U3.5.bin"),
        equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U3.7.bin"),
        equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n3000-uk9-kickstart.6.0.2.U6.6.bin"),
        equalTo(NXOS6));
    // system
    assertThat(inferMajorVersionFromImage("bootflash:/n3000-uk9.6.0.2.U3.2.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n3000-uk9.6.0.2.U3.5.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n3000-uk9.6.0.2.U3.7.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n3000-uk9.6.0.2.U6.6.bin"), equalTo(NXOS6));

    //// Nexus 5000
    // kickstart
    assertThat(
        inferMajorVersionFromImage("bootflash:/n5000-uk9-kickstart.5.1.3.N1.1a.bin"),
        equalTo(NXOS5));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n5000-uk9-kickstart.5.1.3.N2.1c.bin"),
        equalTo(NXOS5));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n5000-uk9-kickstart.5.2.1.N1.9.bin"),
        equalTo(NXOS5));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n5000-uk9-kickstart.6.0.2.N2.5.bin"),
        equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n5000-uk9-kickstart.7.0.6.N1.1.bin"),
        equalTo(NXOS7));
    // system
    assertThat(inferMajorVersionFromImage("bootflash:/n5000-uk9.5.1.3.N1.1a.bin"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromImage("bootflash:/n5000-uk9.5.1.3.N2.1c.bin"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromImage("bootflash:/n5000-uk9.5.2.1.N1.9.bin"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromImage("bootflash:/n5000-uk9.6.0.2.N2.5.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n5000-uk9.7.0.6.N1.1.bin"), equalTo(NXOS7));

    //// Nexus 6000
    // kickstart
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.6.0.2.N2.3.bin"),
        equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.6.0.2.N2.4.bin"),
        equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.0.2.N1.1.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.0.5.N1.1a.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.0.5.N1.1a.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.0.8.N1.1a.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.0.8.N1.1.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.1.0.N1.1b.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.1.4.N1.1.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.3.0.N1.1.bin"),
        equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n6000-uk9-kickstart.7.3.2.N1.1.bin"),
        equalTo(NXOS7));
    // system
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.6.0.2.N2.3.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.6.0.2.N2.4.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.0.2.N1.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.0.5.N1.1a.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.0.5.N1.1a.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.0.8.N1.1a.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.0.8.N1.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.1.0.N1.1b.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.1.4.N1.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.3.0.N1.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/n6000-uk9.7.3.2.N1.1.bin"), equalTo(NXOS7));

    //// Nexus 7000
    // kickstart
    assertThat(
        inferMajorVersionFromImage("bootflash:/n7000-s2-kickstart.6.2.16.bin"), equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n7000-s2-kickstart.6.2.16.bin"), equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n7700-s2-kickstart.6.2.16.bin"), equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/n7700-s2-kickstart.6.2.16.bin"), equalTo(NXOS6));
    assertThat(
        inferMajorVersionFromImage("bootflash:/titanium-d1-kickstart.7.3.0.D1.1.bin"),
        equalTo(NXOS7));
    // system
    assertThat(inferMajorVersionFromImage("bootflash:/n7000-s2-dk9.6.2.16.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n7000-s2-dk9.6.2.16.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n7700-s2-dk9.6.2.16.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/n7700-s2-dk9.6.2.16.bin"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromImage("bootflash:/titanium-d1.7.3.0.D1.1.bin"), equalTo(NXOS7));

    //// Nexus 3000/9000
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.9.2.3.bin"), equalTo(NXOS9));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I2.2b.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I2.3.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I3.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I3.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I4.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I4.3.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I4.5.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I4.5.bin"), equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I4.5.comp3048.bin"), equalTo(NXOS7));
    assertThat(
        inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I4.5.comp3172.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I5.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I5.2.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I7.2.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.7.0.3.I7.4.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/poap/nxos.7.0.3.I3.1.bin"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.9.2.1.bin"), equalTo(NXOS9));
    assertThat(inferMajorVersionFromImage("bootflash:/nxos.9.2.3.bin"), equalTo(NXOS9));
  }

  @Test
  public void testInferMajorVersionFromVersion() {
    assertThat(inferMajorVersionFromVersion("5.0(3)N2(4.01)"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromVersion("5.1(3)N1(1a)"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromVersion("5.1(3)N2(1c)"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromVersion("5.2(1)N1(9)"), equalTo(NXOS5));
    assertThat(inferMajorVersionFromVersion("6.0(2)N2(3)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.0(2)N2(4)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.0(2)N2(5)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.0(2)U3(2)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.0(2)U3(5)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.0(2)U3(7)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.0(2)U6(6)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("6.2(16)"), equalTo(NXOS6));
    assertThat(inferMajorVersionFromVersion("7.0(2)N1(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I2(2a)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I2(2b)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I2(3)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I3(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I3(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I4(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I4(3)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I4(5)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I5(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I5(2)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I7(2)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(3)I7(4)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(5)N1(1a)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(5)N1(1a)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(6)N1(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(8)N1(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.0(8)N1(1a)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.1(0)N1(1b)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.1(4)N1(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.3(0)N1(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("7.3(2)N1(1)"), equalTo(NXOS7));
    assertThat(inferMajorVersionFromVersion("9.2(1) Bios:version 05.31"), equalTo(NXOS9));
    assertThat(inferMajorVersionFromVersion("9.2(3) Bios:version"), equalTo(NXOS9));
  }

  @Test
  public void testInferMajorVersion() {
    CiscoNxosConfiguration vc = new CiscoNxosConfiguration();
    assertThat(inferMajorVersion(vc), equalTo(NxosMajorVersion.UNKNOWN));
    vc.setBootKickstartSup2("bootflash:/titanium-d1-kickstart.7.3.0.D1.1.bin");
    assertThat(inferMajorVersion(vc), equalTo(NXOS7));
    vc.setBootKickstartSup1("bootflash:/n7700-s2-kickstart.6.2.16.bin");
    assertThat(inferMajorVersion(vc), equalTo(NXOS6));
    vc.setBootSystemSup2("bootflash:/titanium-d1.7.3.0.D1.1.bin");
    assertThat(inferMajorVersion(vc), equalTo(NXOS7));
    vc.setBootSystemSup1("bootflash:/n3000-uk9.6.0.2.U3.2.bin");
    assertThat(inferMajorVersion(vc), equalTo(NXOS6));
    vc.setBootNxosSup2("bootflash:/nxos.9.2.1.bin");
    assertThat(inferMajorVersion(vc), equalTo(NXOS9));
    vc.setBootNxosSup1("bootflash:/nxos.7.0.3.I7.2.bin");
    assertThat(inferMajorVersion(vc), equalTo(NXOS7));
    vc.setVersion("9.2.3 Bios:version");
    assertThat(inferMajorVersion(vc), equalTo(NXOS9));
  }

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
    // NOTE: these could be either Nexus 3000 or Nexus 9000.
    // TODO: find some other way to uniquely identify Nexus 9000.
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I2.2b.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I2.3.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I3.1.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I3.1.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I4.1.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I4.3.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I4.5.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I4.5.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I4.5.comp3048.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I4.5.comp3172.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I5.1.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I5.2.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I7.2.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.7.0.3.I7.4.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/poap/nxos.7.0.3.I3.1.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.9.2.1.bin"), nullValue());
    assertThat(inferPlatformFromImage("bootflash:/nxos.9.2.3.bin"), nullValue());
  }

  @Test
  public void testInferPlatform() {
    {
      CiscoNxosConfiguration vc = new CiscoNxosConfiguration();
      // TODO: something better with multi-platform images
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NexusPlatform.UNKNOWN));
      vc.setBootNxosSup2("bootflash:/nxos.9.2.3.bin");
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NexusPlatform.UNKNOWN));
      vc.setBootNxosSup1("bootflash:/nxos.9.2.3.bin");
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NexusPlatform.UNKNOWN));
    }
    {
      CiscoNxosConfiguration vc = new CiscoNxosConfiguration();
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NexusPlatform.UNKNOWN));
      vc.setBootKickstartSup2("bootflash:/n7000-s2-kickstart.6.2.16.bin");
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NEXUS_7000));
      vc.setBootKickstartSup1("bootflash:/n6000-uk9-kickstart.6.0.2.N2.3.bin");
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NEXUS_6000));
      vc.setBootSystemSup2("bootflash:/n5000-uk9.5.1.3.N1.1a.bin");
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NEXUS_5000));
      vc.setBootSystemSup1("bootflash:/n3000-uk9.6.0.2.U3.2.bin");
      assertThat(inferPlatform(vc, NxosMajorVersion.UNKNOWN), equalTo(NEXUS_3000));
    }
  }
}
