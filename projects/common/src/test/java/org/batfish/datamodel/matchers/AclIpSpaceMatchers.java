package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.IpSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class AclIpSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * AclIpSpace}'s lines.
   */
  public static Matcher<AclIpSpace> hasLines(
      @Nonnull Matcher<? super List<AclIpSpaceLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is an {@link AclIpSpace} matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<IpSpace> isAclIpSpaceThat(@Nonnull Matcher<? super AclIpSpace> subMatcher) {
    return new IsAclIpSpaceThat(subMatcher);
  }

  private AclIpSpaceMatchers() {}

  private static final class HasLines extends FeatureMatcher<AclIpSpace, List<AclIpSpaceLine>> {

    public HasLines(Matcher<? super List<AclIpSpaceLine>> subMatcher) {
      super(subMatcher, "an AclIpSpace with lines:", "lines");
    }

    @Override
    protected List<AclIpSpaceLine> featureValueOf(AclIpSpace actual) {
      return actual.getLines();
    }
  }

  private static final class IsAclIpSpaceThat extends IsInstanceThat<IpSpace, AclIpSpace> {
    IsAclIpSpaceThat(Matcher<? super AclIpSpace> subMatcher) {
      super(AclIpSpace.class, subMatcher);
    }
  }
}
