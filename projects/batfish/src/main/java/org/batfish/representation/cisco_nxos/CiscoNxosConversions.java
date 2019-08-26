package org.batfish.representation.cisco_nxos;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform;

/** Conversion helpers for {@link CiscoNxosConfiguration}. */
public final class CiscoNxosConversions {

  /**
   * Infers {@code NexusPlatform} of a configuration based on names of boot image files. Returns
   * {@link NexusPlatform#UNKNOWN} if unique inference cannot be made.
   */
  public static @Nonnull NexusPlatform inferPlatform(CiscoNxosConfiguration vc) {
    return Stream.of(
            vc.getBootNxosSup1(),
            vc.getBootNxosSup2(),
            vc.getBootSystemSup1(),
            vc.getBootSystemSup2(),
            vc.getBootKickstartSup1(),
            vc.getBootKickstartSup2())
        .filter(Objects::nonNull)
        .map(CiscoNxosConversions::inferPlatformFromImage)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(NexusPlatform.UNKNOWN);
  }

  /**
   * Infers {@code NexusPlatform} of a configuration based on name of boot image file. Returns
   * {@code null} if unique inference cannot be made.
   */
  @VisibleForTesting
  static @Nullable NexusPlatform inferPlatformFromImage(String image) {
    if (image.contains("n3000")) {
      return NexusPlatform.NEXUS_3000;
    } else if (image.contains("n5000")) {
      return NexusPlatform.NEXUS_5000;
    } else if (image.contains("n6000")) {
      return NexusPlatform.NEXUS_6000;
    } else if (image.contains("n7000") || image.contains("n7700") || image.contains("titanium")) {
      return NexusPlatform.NEXUS_7000;
    } else {
      return null;
    }
  }

  private CiscoNxosConversions() {}
}
