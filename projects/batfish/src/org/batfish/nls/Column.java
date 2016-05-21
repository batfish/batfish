package org.batfish.nls;

import java.util.List;

import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LBValueType;
import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.datamodel.Prefix;

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
