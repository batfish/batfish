package org.batfish.datamodel.matchers;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.SourceNat;
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

  static final class HasSourceNats extends FeatureMatcher<Interface, List<SourceNat>> {
    HasSourceNats(@Nonnull Matcher<? super List<SourceNat>> subMatcher) {
      super(subMatcher, "sourceNats", "sourceNats");
    }

    @Override
    protected List<SourceNat> featureValueOf(Interface actual) {
      return actual.getSourceNats();
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

  private InterfaceMatchersImpl() {}
}
