package org.batfish.datamodel.applications;

import static org.batfish.datamodel.applications.ApplicationRefiner.refine;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

/** Tests for {@link ApplicationRefiner} */
public class ApplicationRefinerTest {
  @Test
  public void refine_empty() {
    List<Application> apps = ImmutableList.of();
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(), refinedApps);
  }

  @Test
  public void refine_tcpApplicationAll() {
    List<Application> apps = ImmutableList.of(TcpApplication.ALL);
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new TcpApplication(0)), refinedApps);
  }

  @Test
  public void refine_tcpApplicationRange() {
    List<Application> apps =
        ImmutableList.of(
            new TcpApplication(ImmutableList.of(new SubRange(1, 2), new SubRange(4, 5))));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new TcpApplication(1)), refinedApps);
  }

  @Test
  public void refine_tcpApplicationNoRange() {
    List<Application> apps = ImmutableList.of(new TcpApplication(ImmutableList.of()));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(), refinedApps);
  }

  @Test
  public void refine_udpApplicationAll() {
    List<Application> apps = ImmutableList.of(UdpApplication.ALL);
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new UdpApplication(0)), refinedApps);
  }

  @Test
  public void refine_udpApplicationRange() {
    List<Application> apps =
        ImmutableList.of(
            new UdpApplication(ImmutableList.of(new SubRange(1, 2), new SubRange(4, 5))));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new UdpApplication(1)), refinedApps);
  }

  @Test
  public void refine_udpApplicationNoRange() {
    List<Application> apps = ImmutableList.of(new UdpApplication(ImmutableList.of()));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(), refinedApps);
  }

  @Test
  public void refine_icmpTypesApplicationAll() {
    List<Application> apps = ImmutableList.of(IcmpTypesApplication.ALL);
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new IcmpTypeCodesApplication(0, 0)), refinedApps);
  }

  @Test
  public void refine_icmpTypesApplicationRange() {
    List<Application> apps =
        ImmutableList.of(
            new IcmpTypesApplication(ImmutableList.of(new SubRange(1, 2), new SubRange(4, 5))));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new IcmpTypeCodesApplication(1, 0)), refinedApps);
  }

  @Test
  public void refine_icmpTypesApplicationNoRange() {
    List<Application> apps = ImmutableList.of(new IcmpTypesApplication(ImmutableList.of()));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(), refinedApps);
  }

  @Test
  public void refine_icmpTypeCodesApplicationAll() {
    List<Application> apps =
        ImmutableList.of(
            new IcmpTypeCodesApplication(3, ImmutableList.of(IcmpTypeCodesApplication.ALL_CODES)));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new IcmpTypeCodesApplication(3, 0)), refinedApps);
  }

  @Test
  public void refine_icmpTypeCodesApplicationRange() {
    List<Application> apps =
        ImmutableList.of(
            new IcmpTypeCodesApplication(
                3, ImmutableList.of(new SubRange(1, 2), new SubRange(4, 5))));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(new IcmpTypeCodesApplication(3, 1)), refinedApps);
  }

  @Test
  public void refine_icmpTypeCodesApplicationNoRange() {
    List<Application> apps = ImmutableList.of(new IcmpTypeCodesApplication(3, ImmutableList.of()));
    List<Application> refinedApps = refine(apps, 1);
    assertEquals(ImmutableList.of(), refinedApps);
  }
}
