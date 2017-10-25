package org.batfish.bgp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.Iterator;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.plugin.ExternalBgpAdvertisementPlugin;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@AutoService(Plugin.class)
public class JsonExternalBgpAdvertisementPlugin extends ExternalBgpAdvertisementPlugin {

  @Override
  protected void externalBgpAdvertisementPluginInitialize() {}

  @Override
  public AdvertisementSet loadExternalBgpAdvertisements() {
    AdvertisementSet advertSet = new AdvertisementSet();
    String externalBgpAnnouncementsFileContents = _batfish.readExternalBgpAnnouncementsFile();
    if (externalBgpAnnouncementsFileContents != null) {
      // Populate advertSet with BgpAdvertisements that
      // gets passed to populatePrecomputedBgpAdvertisements.
      // See populatePrecomputedBgpAdvertisements for the things that get
      // extracted from these advertisements.

      try {
        JSONObject jsonObj = new JSONObject(externalBgpAnnouncementsFileContents);

        JSONArray announcements = jsonObj.getJSONArray(BfConsts.PROP_BGP_ANNOUNCEMENTS);

        ObjectMapper mapper = new ObjectMapper();

        for (int index = 0; index < announcements.length(); index++) {
          JSONObject announcement = new JSONObject();
          announcement.put("@id", index);
          JSONObject announcementSrc = announcements.getJSONObject(index);
          for (Iterator<?> i = announcementSrc.keys(); i.hasNext(); ) {
            String key = (String) i.next();
            if (!key.equals("@id")) {
              announcement.put(key, announcementSrc.get(key));
            }
          }
          BgpAdvertisement bgpAdvertisement =
              mapper.readValue(announcement.toString(), BgpAdvertisement.class);
          advertSet.add(bgpAdvertisement);
        }

      } catch (JSONException | IOException e) {
        throw new BatfishException("Error processing external BGP advertisements file", e);
      }
    }
    return advertSet;
  }
}
