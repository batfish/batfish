package org.batfish.datamodel.matchers;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.matchers.IpAccessListMatchersImpl.Accepts;
import org.batfish.datamodel.matchers.IpAccessListMatchersImpl.HasLines;
import org.batfish.datamodel.matchers.IpAccessListMatchersImpl.Rejects;
import org.hamcrest.Matcher;

public final class IpAccessListMatchers {

  /**
   * Provides a matcher that matches if the IpAccessList accepts a flow at the provided source
   * interface given the definitions in the provided configuration.
   */
  public static Accepts accepts(
      @Nonnull Flow flow, @Nullable String srcInterface, @Nonnull Configuration configuration) {
    return new Accepts(
        flow, srcInterface, configuration.getIpAccessLists(), configuration.getIpSpaces());
  }

  /**
   * Provides a matcher that matches if the IpAccessList accepts a flow at the provided source
   * interface given the provided acl definitions.
   */
  public static Accepts accepts(
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return new Accepts(flow, srcInterface, availableAcls, namedIpSpaces);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IpAccessList's
   * lines.
   */
  public static HasLines hasLines(@Nonnull Matcher<? super List<IpAccessListLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  /**
   * Provides a matcher that matches if the IpAccessList rejects a flow at the provided source
   * interface given the provided acl definitions.
   */
  public static Rejects rejects(
      @Nonnull Flow flow, @Nullable String srcInterface, @Nonnull Configuration configuration) {
    return new Rejects(
        flow, srcInterface, configuration.getIpAccessLists(), configuration.getIpSpaces());
  }

  /**
   * Provides a matcher that matches if the IpAccessList rejects a flow at the provided source
   * interface given the provided acl definitions.
   */
  public static Rejects rejects(
      @Nonnull Flow flow,
      @Nullable String srcInterface,
      @Nonnull Map<String, IpAccessList> availableAcls,
      @Nonnull Map<String, IpSpace> namedIpSpaces) {
    return new Rejects(flow, srcInterface, availableAcls, namedIpSpaces);
  }

  private IpAccessListMatchers() {}
}
