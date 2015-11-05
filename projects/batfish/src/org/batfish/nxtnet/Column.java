package org.batfish.nxtnet;

import java.util.List;

import org.batfish.representation.BgpAdvertisement;
import org.batfish.representation.Flow;
import org.batfish.representation.Ip;
import org.batfish.representation.PrecomputedRoute;
import org.batfish.representation.Prefix;

public interface Column {

   List<BgpAdvertisement> asBgpAdvertisementList(EntityTable entityTable);

   List<Flow> asFlowList(EntityTable entityTable);

   List<Ip> asIpList();

   List<Prefix> asPrefixList(EntityTable entityTable);

   List<PrecomputedRoute> asRouteList(EntityTable entityTable);

   List<String> asStringList();

   Object getItem(int i, EntityTable entityTable, LBValueType valueType);

   ColumnType getType();

}
