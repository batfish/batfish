package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.CryptoMapEntry;
import org.batfish.datamodel.CryptoMapSet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class CryptoMapSetMatchersImpl {

  static final class HasDynamic extends FeatureMatcher<CryptoMapSet, Boolean> {
    HasDynamic(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A CryptoMapSet with Dynamic", "Dynamic");
    }

    @Override
    protected Boolean featureValueOf(CryptoMapSet actual) {
      return actual.getDynamic();
    }
  }

  static class HasCryptoMapEntries extends FeatureMatcher<CryptoMapSet, List<CryptoMapEntry>> {

    public HasCryptoMapEntries(@Nonnull Matcher<? super List<CryptoMapEntry>> subMatcher) {
      super(subMatcher, "A CryptoMapSet with CryptoMapEntries:", "CryptoMapEntries");
    }

    @Override
    protected List<CryptoMapEntry> featureValueOf(CryptoMapSet actual) {
      return actual.getCryptoMapEntries();
    }
  }
}
