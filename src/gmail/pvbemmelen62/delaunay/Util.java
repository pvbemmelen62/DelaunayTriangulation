package gmail.pvbemmelen62.delaunay;

import java.util.*;
import java.util.stream.*;

public class Util {

  /** Assert that b is true; if it isn't, throw AssertionError . */
  public static void myAssert(boolean b) {
    if(!b) {
      throw new AssertionError();
    }
  }
  /** Assert that b is true; if it isn't, throw AssertionError . */
  public static void myAssert(boolean b, String msg) {
    if(!b) {
      throw new AssertionError(msg);
    }
  }
  public static boolean equalsApprox(double d0, double d1) {
    // ieee double: mantissa 52 bits
    // 10 bits approx 1e-3 , so 52 bits approx 1e-15 .
    double epsilon = 1e-8;
    return d0-d1 < epsilon;
  }
  public static <T> void ensureIndex(ArrayList<T> arr, int index, T defVal) {
    arr.ensureCapacity(index+1);
    while(arr.size() < index+1) {
      arr.add(defVal);
    }
  }
  public static void arrayListIncr(ArrayList<Integer> numbers, int index,
      int change) {
    numbers.set(index, numbers.get(index)+change);
  }
  public static int sqr(int i) {
    return i*i;
  }
  public static long sqr(long i) {
    return i*i;
  }
  public static double sqr(double x) {
    return x*x;
  }
  /**
   * Returns numbers 0..max in random order.
   */
  public static int[] permutation(Random random, int max) {
    int[] numbers = IntStream.range(0, max+1).toArray();
    for(int i=max; i>=1; --i) {
      int j = random.nextInt(i+1);   // j in 0...i
      swap(numbers, i, j);
    }
    return numbers;
  }
  /**
   * Returns numbers min..max in random order.
   */
  public static int[] permutation(Random random, int min, int max) {
    int[] numbers = IntStream.range(min, max+1).toArray();
    for(int i=max-min; i>=1; --i) {
      int j = random.nextInt(i+1);   // j in 0...i
      swap(numbers, i, j);
    }
    return numbers;
  }
  /** Swaps values in ns at index i and j. */
  public static void swap(int[] ns, int i, int j) {
    if(i==j) {
      return;
    }
    int tmp = ns[i];
    ns[i] = ns[j];
    ns[j] = tmp;
  }
  /** Returns index of first occurrence of value, or -1 if not found. */
  public static int findFirst(int[] ns, int value) {
    int i0 = -1;
    for(int i=0; i<ns.length; ++i) {
      if(ns[i]==value) {
        i0 = i;
        break;
      }
    }
    return i0;
  }
  /** Performs a deep copy .
   * Returns null if <code>arr</code> is null.
   */
  public static int[][] copy(int[][] arr) {
    if(arr==null) {
      return null;
    }
    int[][] rv = new int[arr.length][];
    for(int i=0; i<arr.length; ++i) {
      rv[i] = new int[arr[i].length];
      for(int j=0; j<arr[i].length; ++j) {
        rv[i][j] = arr[i][j];
      }
    }
    return arr;
  }
  public static int indexOf(int[] numbers, int value) {
    int index = -1;
    for(int i=0; i<numbers.length; ++i) {
      if(numbers[i]==value) {
        index = i;
        break;
      }
    }
    return index;
  }
  public static int indexOfMinimum(int[] numbers) {
    int index = -1;
    int min = Integer.MAX_VALUE;
    for(int i=0; i<numbers.length; ++i) {
      if(numbers[i] < min) {
        min = numbers[i];
        index = i;
      }
    }
    return index;
  }
  /** Rotate elements, so that element at index moves to start of array.
   * @return a new array; the old array is unchanged.
   */
  public static int[] rotate(int[] numbers, int index) {
    int len = numbers.length;
    int[] rv = new int[len];
    for(int i=0; i<len; ++i) {
      rv[i] = numbers[(i+index)%len];
    }
    return rv;
  }
  /** Rotate elements, so that element at index moves to start of array;
   *  modifies the original array.
   */
  public static void rotateInPlace(int[] numbers, int index) {
    int[] newNumbers = Util.rotate(numbers, index);
    for(int i=0; i<numbers.length; ++i) {
      numbers[i] = newNumbers[i];
    }
  }
  public static int[][] array2D(int size0, int size1) {
    int[][] rv = new int[size0][];
    for(int i=0; i<size0; ++i) {
      rv[i] = new int[size1];
    }
    return rv;
  }
  public static void fillArray2D(int[][] array2D, int value) {
    for(int i=0; i<array2D.length; ++i) {
      Arrays.fill(array2D[i], value);
    }
  }
  public static int[][][] array3D(int size0, int size1, int size2) {
    int[][][] rv = new int[size0][][];
    for(int i=0; i<size0; ++i) {
      rv[i] = array2D(size1, size2);
    }
    return rv;
  }
  public static void fillArray3D(int[][][] array3D, int value) {
    for(int i=0; i<array3D.length; ++i) {
      fillArray2D(array3D[i], value);
    }
  }
  public static int[] toIntArray(ArrayList<Integer> list) {
    int[] rv = new int[list.size()];
    for(int i=0; i<list.size(); ++i) {
      rv[i] = list.get(i);
    }
    return rv;
  }
}
