import org.junit.jupiter.api.Test;

import java.util.List;

public class RTreeTest {
    @Test
    public void checkRTree(){
        RTree<Integer> tree = new RTree<>();
        tree.insert(new float[]{0, 0}, new float[]{4, 4}, 1);
        tree.insert(new float[]{3, 3}, new float[]{1, 1}, 2);
        tree.insert(new float[]{10, 10}, new float[]{4, 4}, 3);
        tree.insert(new float[]{11, 11}, new float[]{1, 1}, 4);
        tree.insert(new float[]{5, 6}, new float[]{2, 1}, 5);
        tree.insert(new float[]{0, 0}, new float[]{2, 2}, 6);
        tree.insert(new float[]{3, 3}, new float[]{3, 3}, 7);

        int max = tree.getMaxEntries();
        int min = tree.getMinEntries();
        int dims = tree.getNumDims();
        int size = tree.getSize();

        List<Integer> list = tree.search(new float[]{0, 0}, new float[]{14, 14});
        tree.delete(new float[]{5, 6}, new float[]{4, 4}, 5);
    }
}
