package io.github.martinschneider.kommpeiler.examples;

public class LongArrays {
  public static void main(String[] args) {
    long[] a = new long[] {1, 2, 3};
    long[] b = new long[] {4, 5, 6};
    printArray(a);
    printArray(b);
    a[0] = 7;
    a[1] = b[0] * 2;
    a[2] *= 3;
    printArray(a);
    printArray(b);
    b = a;
    printArray(a);
    printArray(b);
  }

  public static void printArray(long[] a) {
    for (int i = 0; i <= 2; i++) {
      System.out.println(a[i]);
    }
  }
}
