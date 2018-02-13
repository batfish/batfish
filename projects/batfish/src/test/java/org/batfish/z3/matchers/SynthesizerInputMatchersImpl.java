package org.batfish.z3.matchers;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BooleanExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class SynthesizerInputMatchersImpl {

  static final class HasEnabledFlowSinks
      extends FeatureMatcher<SynthesizerInput, Set<NodeInterfacePair>> {
    HasEnabledFlowSinks(@Nonnull Matcher<? super Set<NodeInterfacePair>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled flowSinks", "enabled flowSinks");
    }

    @Override
    protected Set<NodeInterfacePair> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledFlowSinks();
    }
  }

  static final class HasEnabledInterfaces
      extends FeatureMatcher<SynthesizerInput, Map<String, Map<String, Interface>>> {
    HasEnabledInterfaces(@Nonnull Matcher<? super Map<String, Map<String, Interface>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled interfaces", "enabled interfaces");
    }

    @Override
    protected Map<String, Map<String, Interface>> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledInterfaces();
    }
  }

  static final class HasEnabledNodes
      extends FeatureMatcher<SynthesizerInput, Map<String, Configuration>> {
    HasEnabledNodes(@Nonnull Matcher<? super Map<String, Configuration>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled nodes", "enabled nodes");
    }

    @Override
    protected Map<String, Configuration> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledNodes();
    }
  }

  static final class HasEnabledVrfs
      extends FeatureMatcher<SynthesizerInput, Map<String, Map<String, Vrf>>> {
    HasEnabledVrfs(@Nonnull Matcher<? super Map<String, Map<String, Vrf>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled VRFs", "enabled VRFs");
    }

    @Override
    protected Map<String, Map<String, Vrf>> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledVrfs();
    }
  }

  static final class HasFibConditions
      extends FeatureMatcher<
          SynthesizerInput,
          Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>> {
    HasFibConditions(
        @Nonnull
            Matcher<
                    ? super
                        Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>>
                subMatcher) {
      super(subMatcher, "SynthesizerInput with fibConditions", "fibConditions");
    }

    @Override
    protected Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>>
        featureValueOf(SynthesizerInput actual) {
      return actual.getFibConditions();
    }
  }

  static final class HasIpsByHostname
      extends FeatureMatcher<SynthesizerInput, Map<String, Set<Ip>>> {
    HasIpsByHostname(@Nonnull Matcher<? super Map<String, Set<Ip>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with IPs by hostname", "IPs by hostname");
    }

    @Override
    protected Map<String, Set<Ip>> featureValueOf(SynthesizerInput actual) {
      return actual.getIpsByHostname();
    }
  }

  private SynthesizerInputMatchersImpl() {}
}
