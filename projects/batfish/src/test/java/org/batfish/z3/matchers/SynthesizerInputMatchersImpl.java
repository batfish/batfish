package org.batfish.z3.matchers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BooleanExpr;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class SynthesizerInputMatchersImpl {

  static final class HasAclActions
      extends FeatureMatcher<SynthesizerInput, Map<String, Map<String, Map<Integer, LineAction>>>> {
    HasAclActions(
        @Nonnull Matcher<? super Map<String, Map<String, Map<Integer, LineAction>>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with ACL actions", "ACL actions");
    }

    @Override
    protected Map<String, Map<String, Map<Integer, LineAction>>> featureValueOf(
        SynthesizerInput actual) {
      return actual.getAclActions();
    }
  }

  static final class HasAclConditions
      extends FeatureMatcher<
          SynthesizerInput, Map<String, Map<String, Map<Integer, BooleanExpr>>>> {
    HasAclConditions(
        @Nonnull Matcher<? super Map<String, Map<String, Map<Integer, BooleanExpr>>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with ACL conditions", "ACL conditions");
    }

    @Override
    protected Map<String, Map<String, Map<Integer, BooleanExpr>>> featureValueOf(
        SynthesizerInput actual) {
      return actual.getAclConditions();
    }
  }

  static final class HasEnabledEdges extends FeatureMatcher<SynthesizerInput, Set<Edge>> {
    HasEnabledEdges(@Nonnull Matcher<? super Set<Edge>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled edges", "enabled edges");
    }

    @Override
    protected Set<Edge> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledEdges();
    }
  }

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
      extends FeatureMatcher<SynthesizerInput, Map<String, Set<String>>> {
    HasEnabledInterfaces(@Nonnull Matcher<? super Map<String, Set<String>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled interfaces", "enabled interfaces");
    }

    @Override
    protected Map<String, Set<String>> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledInterfaces();
    }
  }

  static final class HasEnabledNodes extends FeatureMatcher<SynthesizerInput, Set<String>> {
    HasEnabledNodes(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled nodes", "enabled nodes");
    }

    @Override
    protected Set<String> featureValueOf(SynthesizerInput actual) {
      return actual.getEnabledNodes();
    }
  }

  static final class HasEnabledVrfs
      extends FeatureMatcher<SynthesizerInput, Map<String, Set<String>>> {
    HasEnabledVrfs(@Nonnull Matcher<? super Map<String, Set<String>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with enabled VRFs", "enabled VRFs");
    }

    @Override
    protected Map<String, Set<String>> featureValueOf(SynthesizerInput actual) {
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

  static final class HasSourceNats
      extends FeatureMatcher<
          SynthesizerInput, Map<String, Map<String, List<Entry<BooleanExpr, BooleanExpr>>>>> {
    HasSourceNats(
        @Nonnull
            Matcher<? super Map<String, Map<String, List<Entry<BooleanExpr, BooleanExpr>>>>>
                subMatcher) {
      super(subMatcher, "SynthesizerInput with source NATs", "source NATs");
    }

    @Override
    protected Map<String, Map<String, List<Entry<BooleanExpr, BooleanExpr>>>> featureValueOf(
        SynthesizerInput actual) {
      return actual.getSourceNats();
    }
  }

  static final class HasTopologyInterfaces
      extends FeatureMatcher<SynthesizerInput, Map<String, Set<String>>> {
    HasTopologyInterfaces(@Nonnull Matcher<? super Map<String, Set<String>>> subMatcher) {
      super(subMatcher, "SynthesizerInput with topology interfaces", "topology interfaces");
    }

    @Override
    protected Map<String, Set<String>> featureValueOf(SynthesizerInput actual) {
      return actual.getTopologyInterfaces();
    }
  }

  private SynthesizerInputMatchersImpl() {}
}
