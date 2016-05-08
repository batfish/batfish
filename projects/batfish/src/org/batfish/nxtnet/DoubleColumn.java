package org.batfish.nxtnet;

import org.batfish.common.BatfishException;
import org.batfish.common.datamodel.LBValueType;

public final class DoubleColumn extends BaseColumn<Double> {

   public DoubleColumn(Double[] items) {
      super(items, ColumnType.DOUBLE);
   }

   @Override
   public Object getItem(int i, EntityTable entityTable, LBValueType valueType) {
      Double d = _items.get(i);
      switch (valueType) {
      case FLOAT:
         return d;

      case ENTITY_INDEX_BGP_ADVERTISEMENT:
      case ENTITY_INDEX_FLOW:
      case ENTITY_INDEX_INT:
      case ENTITY_INDEX_NETWORK:
      case ENTITY_INDEX_ROUTE:
      case ENTITY_REF_ADVERTISEMENT_TYPE:
      case ENTITY_REF_AS_PATH:
      case ENTITY_REF_AUTONOMOUS_SYSTEM:
      case ENTITY_REF_FLOW_TAG:
      case ENTITY_REF_INT:
      case ENTITY_REF_INTERFACE:
      case ENTITY_REF_IP:
      case ENTITY_REF_NODE:
      case ENTITY_REF_ORIGIN_TYPE:
      case ENTITY_REF_POLICY_MAP:
      case ENTITY_REF_ROUTING_PROTOCOL:
      case ENTITY_REF_STRING:
      case INT:
      case STRING:
      default:
         throw new BatfishException("invalid value type");

      }
   }

}
