import java.util.HashMap;

public class MyPQ<T> {
    private Node[] contents;
    private int size;
    private HashMap<T, Integer> toIndex;

    public MyPQ() {
        contents = new MyPQ.Node[16];
        contents[0] = null;
        toIndex = new HashMap<T, Integer>();
        size = 0;
    }

    public boolean isEmpty() { return size == 0; }

    private static int leftIndex(int i) {
        return 2 * i;
    }

    private static int rightIndex(int i) {
        return 2 * i + 1;
    }

    private static int parentIndex(int i) {
        return i / 2;
    }

    private Node getNode(int index) {
        if (!inBounds(index)) {
            return null;
        }
        return contents[index];
    }

    private boolean inBounds(int index) {
        if ((index > size) || (index < 1)) {
            return false;
        }
        return true;
    }

    private void swap(int index1, int index2) {
        Node node1 = getNode(index1);
        Node node2 = getNode(index2);
        contents[index1] = node2;
        contents[index2] = node1;
        toIndex.put(node1.item(), index2);
        toIndex.put(node2.item(), index1);
    }

    private int min(int index1, int index2) {
        Node node1 = getNode(index1);
        Node node2 = getNode(index2);
        if (node1 == null) {
            return index2;
        } else if (node2 == null) {
            return index1;
        } else if (node1.myPriority < node2.myPriority) {
            return index1;
        } else {
            return index2;
        }
    }

    private void swim(int index) {
        validateSinkSwimArg(index);
        int parent = parentIndex(index);
        while ((index > 1) && contents[parent].priority() > contents[index].priority()) {
            swap(parent, index);
            index = parent;
            parent = parentIndex(index);
        }
        return;
    }

    private void sink(int index) {
        validateSinkSwimArg(index);
        if (leftIndex(index) > size || index > size || size == 0) {
            return;
        }
        double leftCompare = contents[leftIndex(index)].priority() - contents[index].priority();
        if (rightIndex(index) > size) {
            if (leftIndex(index) == size && leftCompare < 0) {
                swap(index, leftIndex(index));
                return;
            } else {
                return;
            }
        }
        double rightCompare = contents[rightIndex(index)].priority() - contents[index].priority();
        if (leftCompare >= 0 && rightCompare >= 0) {
            return;
        }
        if (leftCompare > rightCompare) {
            swap(index, rightIndex(index));
            sink(rightIndex(index));
        } else {
            swap(index, leftIndex(index));
            sink(leftIndex(index));
        }
    }

    public void insert(T item, double priority) {
        if (size + 1 == contents.length) {
            resize(contents.length * 2);
        }

        size = size + 1;
        contents[size] = new MyPQ.Node(item, priority);
        toIndex.put(item, size);
        if (size > 1) {
            swim(size);
        }
    }

    public T peek() {
        if (size == 0) {
            return null;
        }
        return contents[1].myItem;
    }

    public T removeMin() {
        T root = contents[1].item();
        if (size == 1) {
            contents[1] = null;
            toIndex.remove(root);
            size = 0;
        } else {
            contents[1] = contents[size];
            toIndex.put(contents[size].item(), 1);
            contents[size] = null;
            toIndex.remove(root);
            size = size - 1;
        }
        if (size > 1) {
            sink(1);
        }
        if (size > 16 && size < 0.5 * contents.length) {
            resize(contents.length / 2);
        }
        return root;
    }

    public int size() {
        return size;
    }

    public void changePriority(T item, double priority) {
        double prevprior;
        int ind = toIndex.get(item);
        //System.out.println(ind);
        //System.out.println(size);
        prevprior = contents[ind].priority();
        contents[ind] = new MyPQ.Node(item, priority);
        if (prevprior != priority) {
            if (prevprior > priority) {
                swim(ind);
            } else {
                sink(ind);
            }
        }
        return;
    }

    public String toString() {
        return toStringHelper(1, "");
    }

    private String toStringHelper(int index, String soFar) {
        if (getNode(index) == null) {
            return "";
        } else {
            String toReturn = "";
            int rightChild = rightIndex(index);
            toReturn += toStringHelper(rightChild, "        " + soFar);
            if (getNode(rightChild) != null) {
                toReturn += soFar + "    /";
            }
            toReturn += "\n" + soFar + getNode(index) + "\n";
            int leftChild = leftIndex(index);
            if (getNode(leftChild) != null) {
                toReturn += soFar + "    \\";
            }
            toReturn += toStringHelper(leftChild, "        " + soFar);
            return toReturn;
        }
    }

    private void validateSinkSwimArg(int index) {
        if (index < 1) {
            throw new IllegalArgumentException("Cannot sink or swim nodes with index 0 or less");
        }
        if (index > size) {
            throw new IllegalArgumentException("Cannot sink or swim nodes with index greater than current size.");
        }
        if (contents[index] == null) {
            throw new IllegalArgumentException("Cannot sink or swim a null node.");
        }
    }

    private class Node {
        private T myItem;
        private double myPriority;

        private Node(T item, double priority) {
            myItem = item;
            myPriority = priority;
        }

        public T item() {
            return myItem;
        }

        public double priority() {
            return myPriority;
        }

        public String toString() {
            return myItem.toString() + ", " + myPriority;
        }
    }

    private void resize(int capacity) {
        Node[] temp = new MyPQ.Node[capacity];
        for (int i = 1; i <= size; i++) {
            temp[i] = contents[i];
        }
        contents = temp;
    }
}