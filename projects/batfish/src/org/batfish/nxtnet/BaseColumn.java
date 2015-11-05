package org.batfish.nxtnet;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class BaseColumn<T> implements Column {

   protected List<T> _items;

   private ColumnType _type;

   public BaseColumn(T[] items, ColumnType type) {
      _items = ImmutableList.<T> builder().addAll(Arrays.asList(items)).build();
      _type = type;
   }

   public List<T> getRows() {

      return _items;
   }

   @Override
   public final ColumnType getType() {
      return _type;
   }

}
