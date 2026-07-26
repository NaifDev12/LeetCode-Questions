Program : 

import java.util.Comparator;

public class GenericDemo {

    public static <T extends Comparable<T>> T findMax(T[] arr) {
        T max = arr[0];
        for (T element : arr) {
            if (element.compareTo(max) > 0) {
                max = element;
            }
        }
        return max;
    }

    public static void main(String[] args) {

        Box<Integer> intBox = new Box<>();
        intBox.set(100);
        System.out.println("Integer Box Value : " + intBox.get());
        intBox.showType();

        Box<String> strBox = new Box<>();
        strBox.set("Hello Generics");
        System.out.println("String Box Value : " + strBox.get());
        strBox.showType();

        System.out.println();
        System.out.println("---- Key-Value Pairs ----");

        Pair<String, Integer> pair1 = new Pair<>("Rahul", 88);
        pair1.display();

        Pair<Integer, String> pair2 = new Pair<>(101, "CSE");
        pair2.display();

        System.out.println();

        Integer[] intArray = {45, 89, 23, 67, 12};
        System.out.println("Maximum Number : " + findMax(intArray));

        String[] strArray = {"Rahul", "Sneha", "Kiran", "Amit"};
        System.out.println("Maximum (Alphabetical) : " + findMax(strArray));

        Double[] doubleArray = {75.5, 92.3, 88.1, 60.0};
        System.out.println("Maximum Marks : " + findMax(doubleArray));
    }
}

class Box<T> {
    private T item;

    public void set(T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }

    public void showType() {
        System.out.println("Type of stored item : " + item.getClass().getName());
    }
}

class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public void display() {
        System.out.println(key + " = " + value);
    }
}
