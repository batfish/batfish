package org.batfish.nxtnet;

import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.LBValueType;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.RoutingProtocol;

public final class StringColumn extends BaseColumn<String> {

   public StringColumn(String[] items) {
      super(items, ColumnType.STRING);
   }

   @Override
   public List<String> asStringList() {
      return _items;
   }

   @Override
   public Object getItem(int i, EntityTable entityTable, LBValueType valueType) {
      String s = _items.get(i);
      switch (valueType) {

      case ENTITY_REF_ADVERTISEMENT_TYPE:
      case ENTITY_REF_AS_PATH:
      case ENTITY_REF_FLOW_TAG:
      case ENTITY_REF_INTERFACE:
      case ENTITY_REF_NODE:
      case ENTITY_REF_POLICY_MAP:
      case ENTITY_REF_STRING:
      case STRING:
         return s;

      case ENTITY_REF_ORIGIN_TYPE:
         return OriginType.fromString(s);

      case ENTITY_REF_ROUTING_PROTOCOL:
         return RoutingProtocol.fromProtocolName(s);

      case ENTITY_INDEX_BGP_ADVERTISEMENT:
      case ENTITY_INDEX_FLOW:
      case ENTITY_INDEX_NETWORK:
      case ENTITY_INDEX_ROUTE:
      case ENTITY_REF_IP:
      case ENTITY_INDEX_INT:
      case ENTITY_REF_AUTONOMOUS_SYSTEM:
      case ENTITY_REF_INT:
      case INT:
      case FLOAT:
      default:
         throw new BatfishException("invalid value type");

      }
   }

}
