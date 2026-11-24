package org.batfish.common.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Suppliers;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.batfish.common.util.NonRecursiveSupplier.NonRecursiveSupplierException;
import org.junit.jupiter.api.Test;

class NonRecursiveSupplierTest {

  @Test
  void testNonReentrantSupplier() {
    Map<String, Supplier<String>> suppliers = new HashMap<>();
    suppliers.put("foo", new NonRecursiveSupplier<>(() -> suppliers.get("bar").get()));
    suppliers.put("bar", new NonRecursiveSupplier<>(() -> suppliers.get("foo").get()));
    assertThrows(NonRecursiveSupplierException.class, () -> suppliers.get("foo").get());
  }

  @Test
  void testMemoize() {
    Map<String, Supplier<String>> suppliers = new HashMap<>();
    suppliers.put(
        "foo", Suppliers.memoize(new NonRecursiveSupplier<>(() -> suppliers.get("bar").get())));
    suppliers.put(
        "bar", Suppliers.memoize(new NonRecursiveSupplier<>(() -> suppliers.get("foo").get())));
    assertThrows(NonRecursiveSupplierException.class, () -> suppliers.get("foo").get());
  }
}
