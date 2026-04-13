package org.batfish.datamodel.vendor_family;

import javax.annotation.Nonnull;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class VendorFamilyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the VendorFamily's
   * cisco
   */
  public static Matcher<VendorFamily> hasCisco(Matcher<? super CiscoFamily> subMatcher) {
    return new HasCisco(subMatcher);
  }

  private VendorFamilyMatchers() {}

  private static final class HasCisco extends FeatureMatcher<VendorFamily, CiscoFamily> {
    HasCisco(@Nonnull Matcher<? super CiscoFamily> subMatcher) {
      super(subMatcher, "VendorFamily with cisco", "cisco");
    }

    @Override
    protected CiscoFamily featureValueOf(VendorFamily actual) {
      return actual.getCisco();
    }
  }
}
