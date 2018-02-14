package org.batfish.datamodel.matchers;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.Vrf;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class InterfaceMatchersImpl {

  static final class HasDeclaredNames extends FeatureMatcher<Interface, Set<String>> {
    HasDeclaredNames(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "declared names", "declared names");
    }

    @Override
    protected Set<String> featureValueOf(Interface actual) {
      return actual.getDeclaredNames();
    }
  }

  static final class HasOspfArea extends FeatureMatcher<Interface, OspfArea> {
    HasOspfArea(@Nonnull Matcher<? super OspfArea> subMatcher) {
      super(subMatcher, "ospfArea", "ospfArea");
    }

    @Override
    protected OspfArea featureValueOf(Interface actual) {
      return actual.getOspfArea();
    }
  }

  static final class HasSourceNats extends FeatureMatcher<Interface, List<SourceNat>> {
    HasSourceNats(@Nonnull Matcher<? super List<SourceNat>> subMatcher) {
      super(subMatcher, "sourceNats", "sourceNats");
    }

    @Override
    protected List<SourceNat> featureValueOf(Interface actual) {
      return actual.getSourceNats();
    }
  }

  static final class HasVrf extends FeatureMatcher<Interface, Vrf> {
    HasVrf(@Nonnull Matcher<? super Vrf> subMatcher) {
      super(subMatcher, "an interface with vrf", "vrf");
    }

    @Override
    protected Vrf featureValueOf(Interface actual) {
      return actual.getVrf();
    }
  }

  static final class IsActive extends FeatureMatcher<Interface, Boolean> {
    IsActive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "active", "active");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getActive();
    }
  }

  static final class IsOspfPassive extends FeatureMatcher<Interface, Boolean> {
    IsOspfPassive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPassive", "ospfPassive");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfPassive();
    }
  }

  static final class IsOspfPointToPoint extends FeatureMatcher<Interface, Boolean> {
    IsOspfPointToPoint(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "ospfPointToPoint", "ospfPointToPoint");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfPointToPoint();
    }
  }

  private InterfaceMatchersImpl() {}
}
