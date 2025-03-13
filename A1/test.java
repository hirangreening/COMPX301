public class test {
    public static void main(String[] args) {
        String[] testData = {"apple", "orange", "banana", "grape", "kiwi"};
        Heap heap = new Heap(testData.length);
        heap.heapify(testData);
        heap.heapsort();

        // Use the getter to access sorted data
        for (String s : heap.getHeapArray()) {
            System.out.println(s);
        }
    }
}
