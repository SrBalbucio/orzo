package io.github.martinschneider.kommpeiler.examples;

public class K004_IntegerExpressions {
  public static void main(String[] args) {
    int a = 1 + 2 * 3 + 4;
    int b = (1 + 2) * (3 + 4);
    System.out.println(a);
    System.out.println(b);
    System.out.println((1 + b) / a);
  }
}