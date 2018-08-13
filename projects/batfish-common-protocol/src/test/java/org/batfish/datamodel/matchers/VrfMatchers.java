package org.batfish.datamodel.matchers;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasBgpProcess;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasEigrpProcesses;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasGeneratedRoutes;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasInterfaces;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasName;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasOspfProcess;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasSnmpServer;
import org.batfish.datamodel.matchers.VrfMatchersImpl.HasStaticRoutes;
import org.batfish.datamodel.ospf.OspfProcess;
import org.hamcrest.Matcher;

public class VrfMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's BGP
   * process.
   */
  public static HasBgpProcess hasBgpProcess(Matcher<? super BgpProcess> subMatcher) {
    return new HasBgpProcess(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's EIGRP
   * process.
   */
  public static HasEigrpProcesses hasEigrpProcesses(
      Matcher<? super Map<Long, EigrpProcess>> subMatcher) {
    return new HasEigrpProcesses(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's generated
   * routes.
   */
  public static HasGeneratedRoutes hasGeneratedRoutes(
      Matcher<? super SortedSet<GeneratedRoute>> subMatcher) {
    return new HasGeneratedRoutes(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's
   * interfaces.
   */
  public static Matcher<Vrf> hasInterfaces(Matcher<? super SortedSet<String>> subMatcher) {
    return new HasInterfaces(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's OSPF
   * process.
   */
  public static HasOspfProcess hasOspfProcess(Matcher<? super OspfProcess> subMatcher) {
    return new HasOspfProcess(subMatcher);
  }

  /** Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's name. */
  public static HasName hasName(Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  public static @Nonnull Matcher<Vrf> hasSnmpServer(
      @Nonnull Matcher<? super SnmpServer> subMatcher) {
    return new HasSnmpServer(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's static
   * routes.
   */
  public static HasStaticRoutes hasStaticRoutes(
      Matcher<? super SortedSet<StaticRoute>> subMatcher) {
    return new HasStaticRoutes(subMatcher);
  }
}
