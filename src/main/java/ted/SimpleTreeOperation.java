package ted;

public class SimpleTreeOperation implements TreeOperation {
    private final TreeOperationType type;
    private final int leftIndex;
    private final int rightIndex;
    private final int cost;

    public SimpleTreeOperation(TreeOperationType type, int leftIndex, int rightIndex, int cost) {
        this.type = type;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.cost = cost;
    }

    @Override
    public TreeOperationType getType() {
        return type;
    }

    @Override
    public int getLeftIndex() {
        return leftIndex;
    }

    @Override
    public int getRightIndex() {
        return rightIndex;
    }

    @Override
    public int getCost() {
        return cost;
    }

    public static SimpleTreeOperation insert(int index, int cost) {
        return new SimpleTreeOperation(TreeOperationType.INSERT, TreeOperation.EMPTY_INDEX, index, cost);
    }

    public static SimpleTreeOperation delete(int index, int cost) {
        return new SimpleTreeOperation(TreeOperationType.DELETE, index, TreeOperation.EMPTY_INDEX, cost);
    }

    public static SimpleTreeOperation replace(int leftIndex, int rightIndex, int cost) {
        return new SimpleTreeOperation(TreeOperationType.REPLACE, leftIndex, rightIndex, cost);
    }

    public static SimpleTreeOperation keep(int leftIndex, int rightIndex, int cost) {
        return new SimpleTreeOperation(TreeOperationType.KEEP, leftIndex, rightIndex, cost);
    }

    @Override
    public String toString() {
        return String.format("%s (%d, %d)@%d", type.name(), leftIndex, rightIndex, cost);
    }
}
