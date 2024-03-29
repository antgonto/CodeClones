package ted;

public interface TreeOperation {
    int EMPTY_INDEX = -1;

    TreeOperationType getType();
    int getLeftIndex();
    int getRightIndex();
    int getCost();
}
