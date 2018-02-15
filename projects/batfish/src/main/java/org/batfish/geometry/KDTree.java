package org.batfish.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

class KDTree {

  private int _dimensions;
  private KNode _root;

  KDTree(int dim) {
    _dimensions = dim;
    _root = null;
  }

  private int nextDim(int cd) {
    return (cd + 1) % (_dimensions + 1);
  }

  private long getDim(EquivalenceClass r, int dim) {
    return r.getBounds()[dim];
  }

  // A utility function to find minimum of three integers
  @Nullable
  private EquivalenceClass min(@Nullable EquivalenceClass x, @Nullable EquivalenceClass y, int dim) {
    if (x == null) {
      return y;
    }
    if (y == null) {
      return x;
    }
    if (getDim(x, dim) > getDim(y, dim)) {
      return y;
    }
    return x;
  }

  @Nullable
  private EquivalenceClass min(int dim, @Nullable EquivalenceClass x, @Nullable EquivalenceClass y) {
    if (x == null) {
      return y;
    }
    if (y == null) {
      return x;
    }
    long xx = getDim(x, dim);
    long yy = getDim(y, dim);
    if (xx < yy) {
      return x;
    } else {
      return y;
    }
  }

  private KNode insert(EquivalenceClass r, @Nullable KNode t, int cd) {
    if (t == null) {
      return new KNode(r, null, null);
    }
    long x1 = getDim(r, cd);
    long x2 = getDim(t._rectangle, cd);
    if (r.equals(t._rectangle)) {
      System.out.println("ADDING DUPLICATE: " + r);
    } else if (x1 < x2) {
      t._left = insert(r, t._left, nextDim(cd));
    } else {
      t._right = insert(r, t._right, nextDim(cd));
    }
    return t;
  }

  @Nullable
  private EquivalenceClass findMin(KNode t, int dim, int cd) {
    if (t == null) {
      return null;
    }
    if (cd == dim) {
      if (t._left == null) {
        return t._rectangle;
      } else {
        return findMin(t._left, dim, nextDim(cd));
      }
    } else {
      EquivalenceClass x = findMin(t._left, dim, nextDim(cd));
      EquivalenceClass y = findMin(t._right, dim, nextDim(cd));
      return min(min(x, y, dim), t._rectangle, dim);
    }
  }

  @Nullable
  private KNode delete(@Nullable EquivalenceClass r, KNode t, int cd) {
    if (t == null) {
      throw new BatfishException("Delete KD tree not found: " + r);
    }
    int nextCd = nextDim(cd);
    if (Objects.equals(r, t._rectangle)) {
      if (t._right != null) {
        t._rectangle = findMin(t._right, cd, nextCd);
        t._right = delete(t._rectangle, t._right, nextCd);
      } else if (t._left != null) {
        t._rectangle = findMin(t._left, cd, nextCd);
        t._right = delete(t._rectangle, t._left, nextCd);
        t._left = null;
      } else {
        t = null;
      }
      return t;
    }
    if (getDim(r, cd) < getDim(t._rectangle, cd)) {
      t._left = delete(r, t._left, nextCd);
    } else {
      t._right = delete(r, t._right, nextCd);
    }
    return t;
  }

  private void intersect(EquivalenceClass r, KNode t, int cd, List<EquivalenceClass> result) {
    if (t == null) {
      return;
    }
    int nextCd = nextDim(cd);

    if (r.overlap(t._rectangle) != null) {
      result.add(t._rectangle);
    }

    boolean low = (cd % 2 == 0);
    int idx1 = low ? cd : cd - 1;
    int idx2 = low ? cd + 1 : cd;

    long rx1 = getDim(r, idx1);
    long rx2 = getDim(r, idx2);
    long tx1 = getDim(t._rectangle, idx1);
    long tx2 = getDim(t._rectangle, idx2);

    if (low) {
      // branching on low, so if search rect has high lower than low, we skip
      if (rx2 < tx1) {
        intersect(r, t._left, nextCd, result);
      } else {
        intersect(r, t._left, nextCd, result);
        intersect(r, t._right, nextCd, result);
      }
    } else {
      // branching on high, so if seach rect has low higher than high, we skip
      if (rx1 > tx2) {
        intersect(r, t._right, nextCd, result);
      } else {
        intersect(r, t._left, nextCd, result);
        intersect(r, t._right, nextCd, result);
      }
    }
  }

  private int size(KNode t) {
    if (t == null) {
      return 0;
    }
    return 1 + size(t._left) + size(t._right);
  }

  private int depth(KNode t) {
    if (t == null) {
      return 0;
    }
    return 1 + Math.max(depth(t._left), depth(t._right));
  }

  private void elements(KNode t, List<EquivalenceClass> elems) {
    if (t == null) {
      return;
    }
    elems.add(t._rectangle);
    elements(t._left, elems);
    elements(t._right, elems);
  }

  int size() {
    return size(_root);
  }

  int depth() {
    return depth(_root);
  }

  List<EquivalenceClass> elements() {
    List<EquivalenceClass> elems = new ArrayList<>();
    elements(_root, elems);
    return elems;
  }

  List<EquivalenceClass> intersect(EquivalenceClass r) {
    List<EquivalenceClass> allIntersects = new ArrayList<>();
    intersect(r, _root, 0, allIntersects);
    return allIntersects;
  }

  void insert(EquivalenceClass r) {
    _root = insert(r, _root, 0);
  }

  void delete(EquivalenceClass r) {
    _root = delete(r, _root, 0);
  }

  class KNode {
    private EquivalenceClass _rectangle;
    private KNode _left;
    private KNode _right;

    KNode(EquivalenceClass rect, @Nullable KNode l, @Nullable KNode r) {
      this._rectangle = rect;
      this._left = l;
      this._right = r;
    }
  }
}
