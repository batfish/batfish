package org.batfish.nxtnet;

import java.util.List;

import org.batfish.common.datamodel.BgpAdvertisement;
import org.batfish.common.datamodel.Flow;
import org.batfish.common.datamodel.Ip;
import org.batfish.common.datamodel.LBValueType;
import org.batfish.common.datamodel.PrecomputedRoute;
import org.batfish.common.datamodel.Prefix;

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
