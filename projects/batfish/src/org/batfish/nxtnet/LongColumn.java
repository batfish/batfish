package org.batfish.nxtnet;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.common.datamodel.BgpAdvertisement;
import org.batfish.common.datamodel.Flow;
import org.batfish.common.datamodel.Ip;
import org.batfish.common.datamodel.LBValueType;
import org.batfish.common.datamodel.PrecomputedRoute;
import org.batfish.common.datamodel.Prefix;

public final class LongColumn extends BaseColumn<Long> {

   public LongColumn(Long[] items) {
      super(items, ColumnType.LONG);
   }

   @Override
   public List<BgpAdvertisement> asBgpAdvertisementList(EntityTable entityTable) {
      List<BgpAdvertisement> advertisementList = new ArrayList<BgpAdvertisement>();
      for (Long index : _items) {
         BgpAdvertisement advert = entityTable.getBgpAdvertisement(index);
         advertisementList.add(advert);
      }
      return advertisementList;
   }

   @Override
   public List<Flow> asFlowList(EntityTable entityTable) {
      List<Flow> flowList = new ArrayList<Flow>();
      for (Long index : _items) {
         Flow advert = entityTable.getFlow(index);
         flowList.add(advert);
      }
      return flowList;
   }

   @Override
   public List<Ip> asIpList() {
      List<Ip> ips = new ArrayList<Ip>();
      for (Long l : _items) {
         ips.add(new Ip(l));
      }
      return ips;
   }

   @Override
   public List<Prefix> asPrefixList(EntityTable entityTable) {
      List<Prefix> prefixList = new ArrayList<Prefix>();
      for (Long index : _items) {
         Prefix advert = entityTable.getNetwork(index);
         prefixList.add(advert);
      }
      return prefixList;
   }

   @Override
   public List<PrecomputedRoute> asRouteList(EntityTable entityTable) {
      List<PrecomputedRoute> routeList = new ArrayList<PrecomputedRoute>();
      for (Long index : _items) {
         PrecomputedRoute advert = entityTable.getRoute(index);
         routeList.add(advert);
      }
      return routeList;
   }

   @Override
   public Object getItem(int i, EntityTable entityTable, LBValueType valueType) {
      Long l = _items.get(i);
      switch (valueType) {
      case ENTITY_INDEX_BGP_ADVERTISEMENT:
         return entityTable.getBgpAdvertisement(l);

      case ENTITY_INDEX_FLOW:
         return entityTable.getFlow(l);

      case ENTITY_INDEX_NETWORK:
         return entityTable.getNetwork(l);

      case ENTITY_INDEX_ROUTE:
         return entityTable.getRoute(l);

      case ENTITY_REF_IP:
         return new Ip(l);

      case ENTITY_INDEX_INT:
      case ENTITY_REF_AUTONOMOUS_SYSTEM:
      case ENTITY_REF_INT:
      case INT:
         return l;

      case ENTITY_REF_ADVERTISEMENT_TYPE:
      case ENTITY_REF_AS_PATH:
      case ENTITY_REF_FLOW_TAG:
      case ENTITY_REF_INTERFACE:
      case ENTITY_REF_NODE:
      case ENTITY_REF_ORIGIN_TYPE:
      case ENTITY_REF_POLICY_MAP:
      case ENTITY_REF_ROUTING_PROTOCOL:
      case ENTITY_REF_STRING:
      case FLOAT:
      case STRING:
      default:
         throw new BatfishException("invalid value type");

      }
   }

}
