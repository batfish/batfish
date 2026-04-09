package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class VrfMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's BGP
   * process.
   */
  public static Matcher<Vrf> hasBgpProcess(Matcher<? super BgpProcess> subMatcher) {
    return new HasBgpProcess(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's EIGRP
   * process.
   */
  public static Matcher<Vrf> hasEigrpProcesses(
      Matcher<? super Map<Long, EigrpProcess>> subMatcher) {
    return new HasEigrpProcesses(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's generated
   * routes.
   */
  public static Matcher<Vrf> hasGeneratedRoutes(
      Matcher<? super SortedSet<GeneratedRoute>> subMatcher) {
    return new HasGeneratedRoutes(subMatcher);
  }

  /**
   * Provides a matcher that matches if the VRF has an OSPF process named {@code name} that matches
   * the provided {@code subMatcher}.
   */
  public static Matcher<Vrf> hasOspfProcess(String name, Matcher<? super OspfProcess> subMatcher) {
    return new HasOspfProcesses(hasEntry(equalTo(name), subMatcher));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's kernel
   * routes.
   */
  public static Matcher<Vrf> hasKernelRoutes(Matcher<? super SortedSet<KernelRoute>> subMatcher) {
    return new HasKernelRoutes(subMatcher);
  }

  /** Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's name. */
  public static Matcher<Vrf> hasName(Matcher<? super String> subMatcher) {
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
  public static Matcher<Vrf> hasStaticRoutes(Matcher<? super SortedSet<StaticRoute>> subMatcher) {
    return new HasStaticRoutes(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's VXLAN VNI
   * settings.
   */
  public static @Nonnull Matcher<Vrf> hasLayer2Vnis(
      Matcher<? super Map<Integer, Layer2Vni>> subMatcher) {
    return new HasLayer2Vnis(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VRF's
   * isisProcess.
   */
  public static @Nonnull Matcher<Vrf> hasIsisProcess(
      @Nonnull Matcher<? super IsisProcess> subMatcher) {
    return new HasIsisProcess(subMatcher);
  }

  private static final class HasBgpProcess extends FeatureMatcher<Vrf, BgpProcess> {
    HasBgpProcess(@Nonnull Matcher<? super BgpProcess> subMatcher) {
      super(subMatcher, "A VRF with bgpProcess:", "bgpProcess");
    }

    @Override
    protected BgpProcess featureValueOf(Vrf actual) {
      return actual.getBgpProcess();
    }
  }

  private static final class HasEigrpProcesses
      extends FeatureMatcher<Vrf, Map<Long, EigrpProcess>> {
    HasEigrpProcesses(@Nonnull Matcher<? super Map<Long, EigrpProcess>> subMatcher) {
      super(subMatcher, "A Vrf with eigrpProcesses:", "eigrpProcesses");
    }

    @Override
    protected Map<Long, EigrpProcess> featureValueOf(Vrf actual) {
      return actual.getEigrpProcesses();
    }
  }

  private static final class HasGeneratedRoutes
      extends FeatureMatcher<Vrf, SortedSet<GeneratedRoute>> {
    HasGeneratedRoutes(@Nonnull Matcher<? super SortedSet<GeneratedRoute>> subMatcher) {
      super(subMatcher, "A VRF with generatedRoutes:", "generatedRoutes");
    }

    @Override
    protected SortedSet<GeneratedRoute> featureValueOf(Vrf actual) {
      return actual.getGeneratedRoutes();
    }
  }

  private static final class HasIsisProcess extends FeatureMatcher<Vrf, IsisProcess> {
    HasIsisProcess(@Nonnull Matcher<? super IsisProcess> subMatcher) {
      super(subMatcher, "A Vrf with isisProcess:", "isisProcess");
    }

    @Override
    protected IsisProcess featureValueOf(Vrf actual) {
      return actual.getIsisProcess();
    }
  }

  private static final class HasKernelRoutes extends FeatureMatcher<Vrf, SortedSet<KernelRoute>> {
    HasKernelRoutes(@Nonnull Matcher<? super SortedSet<KernelRoute>> subMatcher) {
      super(subMatcher, "A VRF with kernelRoutes:", "kernelRoutes");
    }

    @Override
    protected SortedSet<KernelRoute> featureValueOf(Vrf actual) {
      return actual.getKernelRoutes();
    }
  }

  private static final class HasOspfProcesses
      extends FeatureMatcher<Vrf, Map<String, OspfProcess>> {
    HasOspfProcesses(@Nonnull Matcher<? super Map<String, OspfProcess>> subMatcher) {
      super(subMatcher, "A VRF with ospfProcesses:", "ospfProcesses");
    }

    @Override
    protected Map<String, OspfProcess> featureValueOf(Vrf actual) {
      return actual.getOspfProcesses();
    }
  }

  private static final class HasName extends FeatureMatcher<Vrf, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A VRF with name:", "name");
    }

    @Override
    protected String featureValueOf(Vrf actual) {
      return actual.getName();
    }
  }

  private static final class HasSnmpServer extends FeatureMatcher<Vrf, SnmpServer> {
    HasSnmpServer(@Nonnull Matcher<? super SnmpServer> subMatcher) {
      super(subMatcher, "A Vrf with snmpServer:", "snmpServer");
    }

    @Override
    protected SnmpServer featureValueOf(Vrf actual) {
      return actual.getSnmpServer();
    }
  }

  private static final class HasStaticRoutes extends FeatureMatcher<Vrf, SortedSet<StaticRoute>> {
    HasStaticRoutes(@Nonnull Matcher<? super SortedSet<StaticRoute>> subMatcher) {
      super(subMatcher, "A VRF with staticRoutes:", "staticRoutes");
    }

    @Override
    protected SortedSet<StaticRoute> featureValueOf(Vrf actual) {
      return actual.getStaticRoutes();
    }
  }

  private static final class HasLayer2Vnis extends FeatureMatcher<Vrf, Map<Integer, Layer2Vni>> {
    HasLayer2Vnis(@Nonnull Matcher<? super Map<Integer, Layer2Vni>> subMatcher) {
      super(subMatcher, "A VRF with layer2Vnis:", "layer2Vnis");
    }

    @Override
    protected Map<Integer, Layer2Vni> featureValueOf(Vrf actual) {
      return actual.getLayer2Vnis();
    }
  }
}
