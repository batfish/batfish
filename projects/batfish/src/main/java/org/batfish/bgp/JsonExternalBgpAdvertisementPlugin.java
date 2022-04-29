package org.batfish.bgp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.ExternalBgpAdvertisementPlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpAdvertisement;

@AutoService(Plugin.class)
public class JsonExternalBgpAdvertisementPlugin extends ExternalBgpAdvertisementPlugin {

  private static class Announcements {
    @JsonProperty("Announcements")
    List<BgpAdvertisement> _announcements;
  }

  @Override
  protected void externalBgpAdvertisementPluginInitialize() {}

  @Override
  public Set<BgpAdvertisement> loadExternalBgpAdvertisements(NetworkSnapshot snapshot) {
    String externalBgpAnnouncementsFileContents =
        _batfish.readExternalBgpAnnouncementsFile(snapshot);
    if (externalBgpAnnouncementsFileContents != null) {
      // Populate advertSet with BgpAdvertisements that
      // gets passed to populatePrecomputedBgpAdvertisements.
      // See populatePrecomputedBgpAdvertisements for the things that get
      // extracted from these advertisements.

      try {
        Announcements parsedAdverts =
            BatfishObjectMapper.mapper()
                .readValue(externalBgpAnnouncementsFileContents, Announcements.class);
        return new LinkedHashSet<>(parsedAdverts._announcements);
      } catch (IOException e) {
        throw new BatfishException("Error processing external BGP advertisements file", e);
      }
    }
    return new LinkedHashSet<>();
  }
}
