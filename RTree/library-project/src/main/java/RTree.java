import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class RTree<T> {
    private final int maxEntries;
    private final int minEntries;
    private final int numDims;
    private Node root;
    private int size;

    public RTree(int maxEntries, int minEntries, int numDims) {
        assert (minEntries <= (maxEntries / 2));
        this.numDims = numDims;
        this.maxEntries = maxEntries;
        this.minEntries = minEntries;
        root = buildRoot(true);
    }

    private Node buildRoot(boolean asLeaf) {
        float[] initCoords = new float[numDims];
        float[] initDimensions = new float[numDims];
        for (int i = 0; i < this.numDims; i++) {
            initCoords[i] = (float) Math.sqrt(Float.MAX_VALUE);
            initDimensions[i] = -2.0f * (float) Math.sqrt(Float.MAX_VALUE);
        }
        return new Node(initCoords, initDimensions, asLeaf);
    }

    public RTree() {
        this(3, 1, 2);
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public int getMinEntries() {
        return minEntries;
    }

    public int getNumDims() {
        return numDims;
    }

    public int getSize() {
        return size;
    }

    public List<T> search(float[] coords, float[] dimensions) {
        assert (coords.length == numDims);
        assert (dimensions.length == numDims);
        LinkedList<T> results = new LinkedList<T>();
        search(coords, dimensions, root, results);
        return results;
    }

    private void search(float[] coords, float[] dimensions, Node n,
                        LinkedList<T> results) {
        if (n.leaf) {
            for (Node e : n.children) {
                if (isOverlaping(coords, dimensions, e.coords, e.dimensions)) {
                    results.add((T) ((Entry) e).entry);
                }
            }
        } else {
            for (Node c : n.children) {
                if (isOverlaping(coords, dimensions, c.coords, c.dimensions)) {
                    search(coords, dimensions, c, results);
                }
            }
        }
    }

    public boolean delete(float[] coords, float[] dimensions, T entry) {
        assert (coords.length == numDims);
        assert (dimensions.length == numDims);
        Node l = findLeaf(root, coords, dimensions, entry);
        if (l == null) {
            findLeaf(root, coords, dimensions, entry);
        }
        assert l != null;
        ListIterator<Node> li = l.children.listIterator();
        T removed = null;
        while (li.hasNext()) {
            Entry e = (Entry) li.next();
            if (e.entry.equals(entry)) {
                removed = (T) e.entry;
                li.remove();
                break;
            }
        }
        if (removed != null) {
            condenseTree(l);
            size--;
        }
        if (size == 0) {
            root = buildRoot(true);
        }
        return (removed != null);
    }

    private Node findLeaf(Node n, float[] coords, float[] dimensions, T entry) {
        if (n.leaf) {
            for (Node c : n.children) {
                if (((Entry) c).entry.equals(entry)) {
                    return n;
                }
            }
        } else {
            for (Node c : n.children) {
                if (isOverlaping(c.coords, c.dimensions, coords, dimensions)) {
                    Node result = findLeaf(c, coords, dimensions, entry);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private void condenseTree(Node n) {
        Set<Node> q = new HashSet<Node>();
        while (n != root) {
            if (n.leaf && (n.children.size() < minEntries)) {
                q.addAll(n.children);
                n.parent.children.remove(n);
            } else if (!n.leaf && (n.children.size() < minEntries)) {
                LinkedList<Node> toVisit = new LinkedList<Node>(n.children);
                while (!toVisit.isEmpty()) {
                    Node c = toVisit.pop();
                    if (c.leaf) {
                        q.addAll(c.children);
                    } else {
                        toVisit.addAll(c.children);
                    }
                }
                n.parent.children.remove(n);
            } else {
                tighten(n);
            }
            n = n.parent;
        }
        if (root.children.size() == 0) {
            root = buildRoot(true);
        } else if ((root.children.size() == 1) && (!root.leaf)) {
            root = root.children.get(0);
            root.parent = null;
        } else {
            tighten(root);
        }
        for (Node ne : q) {
            Entry e = (Entry) ne;
            insert(e.coords, e.dimensions, (T) e.entry);
        }
        size -= q.size();
    }

    public void clear() {
        root = buildRoot(true);
    }

    public void insert(float[] coords, float[] dimensions, T entry) {
        assert (coords.length == numDims);
        assert (dimensions.length == numDims);
        Entry<T> e = new Entry<>(coords, dimensions, entry);
        Node l = chooseLeaf(root, e);
        l.children.add(e);
        size++;
        e.parent = l;
        if (l.children.size() > maxEntries) {
            Node[] splits = splitNode(l);
            adjustTree(splits[0], splits[1]);
        } else {
            adjustTree(l, null);
        }
    }

    private void adjustTree(Node n, Node nn) {
        if (n == root) {
            if (nn != null) {
                root = buildRoot(false);
                root.children.add(n);
                n.parent = root;
                root.children.add(nn);
                nn.parent = root;
            }
            tighten(root);
            return;
        }
        tighten(n);
        if (nn != null) {
            tighten(nn);
            if (n.parent.children.size() > maxEntries) {
                Node[] splits = splitNode(n.parent);
                adjustTree(splits[0], splits[1]);
            }
        }
        if (n.parent != null) {
            adjustTree(n.parent, null);
        }
    }

    private Node[] splitNode(Node n) {
        Node[] nn = new Node[]
                {n, new Node(n.coords, n.dimensions, n.leaf)};
        nn[1].parent = n.parent;
        if (nn[1].parent != null) {
            nn[1].parent.children.add(nn[1]);
        }
        LinkedList<Node> cc = new LinkedList<Node>(n.children);
        n.children.clear();
        Node[] ss = pickSeeds(cc);
        nn[0].children.add(ss[0]);
        nn[1].children.add(ss[1]);
        tighten(nn);
        while (!cc.isEmpty()) {
            if ((nn[0].children.size() >= minEntries)
                    && (nn[1].children.size() + cc.size() == minEntries)) {
                nn[1].children.addAll(cc);
                cc.clear();
                tighten(nn);
                return nn;
            } else if ((nn[1].children.size() >= minEntries)
                    && (nn[0].children.size() + cc.size() == minEntries)) {
                nn[0].children.addAll(cc);
                cc.clear();
                tighten(nn);
                return nn;
            }
            Node c = PickNext(cc);
            Node preferred;
            float e0 = getRequiredExpansion(nn[0].coords, nn[0].dimensions, c);
            float e1 = getRequiredExpansion(nn[1].coords, nn[1].dimensions, c);
            if (e0 < e1) {
                preferred = nn[0];
            } else if (e0 > e1) {
                preferred = nn[1];
            } else {
                float a0 = getArea(nn[0].dimensions);
                float a1 = getArea(nn[1].dimensions);
                if (a0 < a1) {
                    preferred = nn[0];
                } else if (e0 > a1) {
                    preferred = nn[1];
                } else {
                    if (nn[0].children.size() < nn[1].children.size()) {
                        preferred = nn[0];
                    } else if (nn[0].children.size() > nn[1].children.size()) {
                        preferred = nn[1];
                    } else {
                        preferred = nn[(int) Math.round(Math.random())];
                    }
                }
            }
            preferred.children.add(c);
            tighten(preferred);
        }
        return nn;
    }

    private Node[] pickSeeds(LinkedList<Node> nn) {
        Node[] bestPair = new Node[2];
        boolean foundBestPair = false;
        float bestSep = 0.0f;
        for (int i = 0; i < numDims; i++) {
            float dimLb = Float.MAX_VALUE, dimMinUb = Float.MAX_VALUE;
            float dimUb = -1.0f * Float.MAX_VALUE, dimMaxLb = -1.0f * Float.MAX_VALUE;
            Node nMaxLb = null, nMinUb = null;
            for (Node n : nn) {
                if (n.coords[i] < dimLb) {
                    dimLb = n.coords[i];
                }
                if (n.dimensions[i] + n.coords[i] > dimUb) {
                    dimUb = n.dimensions[i] + n.coords[i];
                }
                if (n.coords[i] > dimMaxLb) {
                    dimMaxLb = n.coords[i];
                    nMaxLb = n;
                }
                if (n.dimensions[i] + n.coords[i] < dimMinUb) {
                    dimMinUb = n.dimensions[i] + n.coords[i];
                    nMinUb = n;
                }
            }
            float sep = (nMaxLb == nMinUb) ? -1.0f :
                    Math.abs((dimMinUb - dimMaxLb) / (dimUb - dimLb));
            if (sep >= bestSep) {
                bestPair[0] = nMaxLb;
                bestPair[1] = nMinUb;
                bestSep = sep;
                foundBestPair = true;
            }
        }
        if (!foundBestPair) {
            bestPair = new Node[]{nn.get(0), nn.get(1)};
        }
        nn.remove(bestPair[0]);
        nn.remove(bestPair[1]);
        return bestPair;
    }

    private Node PickNext(LinkedList<Node> cc) {
        return cc.pop();
    }

    private void tighten(Node... nodes) {
        assert (nodes.length >= 1);
        for (Node n : nodes) {
            assert (n.children.size() > 0);
            float[] minCoords = new float[numDims];
            float[] maxCoords = new float[numDims];
            for (int i = 0; i < numDims; i++) {
                minCoords[i] = Float.MAX_VALUE;
                maxCoords[i] = -1.0f * Float.MAX_VALUE;

                for (Node c : n.children) {
                    c.parent = n;
                    if (c.coords[i] < minCoords[i]) {
                        minCoords[i] = c.coords[i];
                    }
                    if ((c.coords[i] + c.dimensions[i]) > maxCoords[i]) {
                        maxCoords[i] = (c.coords[i] + c.dimensions[i]);
                    }
                }
            }
            for (int i = 0; i < numDims; i++) {
                maxCoords[i] -= minCoords[i];
            }
            System.arraycopy(minCoords, 0, n.coords, 0, numDims);
            System.arraycopy(maxCoords, 0, n.dimensions, 0, numDims);
        }
    }

    private Node chooseLeaf(Node n, Entry<T> e) {
        if (n.leaf) {
            return n;
        }
        float minInc = Float.MAX_VALUE;
        Node next = null;
        for (Node c : n.children) {
            float inc = getRequiredExpansion(c.coords, c.dimensions, e);
            if (inc < minInc) {
                minInc = inc;
                next = c;
            } else if (inc == minInc) {
                float curArea = 1.0f;
                float thisArea = 1.0f;
                for (int i = 0; i < c.dimensions.length; i++) {
                    assert next != null;
                    curArea *= next.dimensions[i];
                    thisArea *= c.dimensions[i];
                }
                if (thisArea < curArea) {
                    next = c;
                }
            }
        }
        assert next != null;
        return chooseLeaf(next, e);
    }

    private float getArea(float[] dimensions) {
        float area = 1.0f;
        for (float dimension : dimensions) {
            area *= dimension;
        }
        return area;
    }

    private boolean isOverlaping(float[] scoords, float[] sdimensions,
                                 float[] coords, float[] dimensions) {
        final float FUDGE_FACTOR = 1.001f;
        for (int i = 0; i < scoords.length; i++) {
            boolean overlapInThisDimension = false;
            if (scoords[i] == coords[i]) {
                overlapInThisDimension = true;
            } else if (scoords[i] < coords[i]) {
                if (scoords[i] + FUDGE_FACTOR * sdimensions[i] >= coords[i]) {
                    overlapInThisDimension = true;
                }
            } else if (scoords[i] > coords[i]) {
                if (coords[i] + FUDGE_FACTOR * dimensions[i] >= scoords[i]) {
                    overlapInThisDimension = true;
                }
            }
            if (!overlapInThisDimension) {
                return false;
            }
        }
        return true;
    }

    private float getRequiredExpansion(float[] coords, float[] dimensions, Node e) {
        float area = getArea(dimensions);
        float[] deltas = new float[dimensions.length];
        for (int i = 0; i < deltas.length; i++) {
            if (coords[i] + dimensions[i] < e.coords[i] + e.dimensions[i]) {
                deltas[i] = e.coords[i] + e.dimensions[i] - coords[i] - dimensions[i];
            } else if (coords[i] + dimensions[i] > e.coords[i] + e.dimensions[i]) {
                deltas[i] = coords[i] - e.coords[i];
            }
        }
        float expanded = 1.0f;
        for (int i = 0; i < dimensions.length; i++) {
            expanded *= dimensions[i] + deltas[i];
        }
        return (expanded - area);
    }
}