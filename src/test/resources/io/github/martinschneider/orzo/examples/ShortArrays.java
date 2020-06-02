package io.github.martinschneider.orzo.examples;

public class ShortArrays {
  public static void main(String[] args) {
    short[] a = new short[] {1, 2, 3};
    short[] b = new short[] {4, 5, 6};
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

  public static void printArray(short[] a) {
    for (int i = 0; i <= 2; i++) {
      System.out.println(a[i]);
    }
  }
}
