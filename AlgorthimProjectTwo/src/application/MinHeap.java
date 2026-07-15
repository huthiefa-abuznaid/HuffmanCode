package application;

public class MinHeap{
	private Node[] heap = new Node[512];
    private int size = 0; 

    public MinHeap() {
    }

    private int getParentIndex(int i) { return (i - 1) / 2; }
    private int getLeftChildIndex(int i) { return 2 * i + 1; }
    private int getRightChildIndex(int i) { return 2 * i + 2; }

    public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	public boolean isEmpty() {
        return size == 0;
    }
	public void insert(Node value) {
        if (size >= heap.length) {
            System.out.println("Heap is full!");
            return;
        }
        heap[size] = value;
        size++;
        heapifyUp(size - 1);
    }

    public Node removeMin() {
        if (size == 0) return null;
        
        Node min = heap[0]; 
       
        heap[0] = heap[size - 1];
        size--;
        
        heapifyDown(0);
        
        return min;
    }

    private void heapifyUp(int index) {
        while (index > 0 && heap[index].compareTo(heap[getParentIndex(index)]) < 0) {
            swap(index, getParentIndex(index));
            index = getParentIndex(index);
        }
    }

    private void heapifyDown(int index) {
        int smallest = index;
        int left = getLeftChildIndex(index);
        int right = getRightChildIndex(index);

        if (left < size && heap[left].compareTo(heap[smallest]) < 0) {
            smallest = left;}
        if (right < size && heap[right].compareTo(heap[smallest]) < 0) {
            smallest = right;
        }
        if (smallest != index) {
            swap(index, smallest);
            heapifyDown(smallest);
        }
    }

    private void swap(int i, int j) {
        Node temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    public void printHeap() {
        System.out.print("[");
        for (int i = 0; i < size; i++) {
            System.out.print(heap[i] + (i == size - 1 ? "" : ", "));
        }
        System.out.println("]");
    }
}