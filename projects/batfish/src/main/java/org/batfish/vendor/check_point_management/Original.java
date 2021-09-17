package org.batfish.vendor.check_point_management;

import com.google.common.annotations.VisibleForTesting;

/**
 * When assigned to {@code translated-destination},{@code translated-service}, or {@code
 * translated-source} field of a {@link NatRule}, indicates that the original value should be
 * retained for that field when applying the rule.
 */
public final class Original extends Global
    implements NatTranslatedDestination, NatTranslatedSource, NatTranslatedService {
  @Override
  public <T> T accept(NatTranslatedServiceVisitor<T> visitor) {
    return visitor.visitOriginal(this);
  }

  @Override
  public <T> T accept(NatTranslatedSourceVisitor<T> visitor) {
    return visitor.visitOriginal(this);
  }

  @VisibleForTesting
  public Original(Uid uid) {
    super(NAME_ORIGINAL, uid);
  }

  @Override
  public <T> T accept(NatTranslatedDestinationVisitor<T> visitor) {
    return visitor.visitOriginal(this);
  }
}
