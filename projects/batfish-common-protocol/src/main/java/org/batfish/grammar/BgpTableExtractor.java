package org.batfish.grammar;

import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;

public interface BgpTableExtractor extends BatfishExtractor {

  BgpAdvertisementsByVrf getBgpAdvertisementsByVrf();
}
