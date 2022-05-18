public class Entry<T> extends Node {
    final T entry;

    public Entry(float[] coords, float[] dimensions, T entry) {
        super(coords, dimensions, true);
        this.entry = entry;
    }

    public String toString() {
        return "Entry: " + entry;
    }
}