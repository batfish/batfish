package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.matchers.AclIpSpaceMatchersImpl.HasLines;
import org.batfish.datamodel.matchers.AclIpSpaceMatchersImpl.IsAclIpSpaceThat;
import org.hamcrest.Matcher;

public class AclIpSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * AclIpSpace}'s lines.
   */
  public static HasLines hasLines(@Nonnull Matcher<? super List<AclIpSpaceLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is an {@link AclIpSpace} matched by the provided
   * {@code subMatcher}.
   */
  public static IsAclIpSpaceThat isAclIpSpaceThat(@Nonnull Matcher<? super AclIpSpace> subMatcher) {
    return new IsAclIpSpaceThat(subMatcher);
  }

  private AclIpSpaceMatchers() {}
}
