package cabbageroll.notrisdefect.core.tables;

public class ScoreTable {
    /*old
        public static ScoreType SINGLE = new ScoreType(100, "Single");
        public static ScoreType DOUBLE = new ScoreType(300, "Double");
        public static ScoreType TRIPLE = new ScoreType(500, "Triple");
        public static ScoreType LONG = new ScoreType(800, "Quad");
        public static ScoreType SPIN_MINI_SINGLE = new ScoreType(200, "Spin Mini Single");
        public static ScoreType SPIN_SINGLE = new ScoreType(800, "Spin Single");
        public static ScoreType SPIN_MINI_DOUBLE = new ScoreType(400, "Spin Mini Double");
        public static ScoreType SPIN_DOUBLE = new ScoreType(1200, "Spin Double");
        public static ScoreType SPIN_TRIPLE = new ScoreType(1600, "Spin Triple");
        public static ScoreType SPIN_LONG = new ScoreType(2600, "Spin Quad");
        public static ScoreType SPIN_MINI = new ScoreType(100, "Spin Mini");
        public static ScoreType SPIN = new ScoreType(400, "Spin");
        public static ScoreType COMBO = new ScoreType(50, "Combo");
        public static ScoreType ALL_CLEAR = new ScoreType(3500, "All Clear");
        public static ScoreType SD = new ScoreType(1, "Soft Drop");
        public static ScoreType HD = new ScoreType(2, "Hard Drop");
        public static ScoreType WTF = new ScoreType(69420, "Unknown");
     */

    public static final ScoreTable normal = new ScoreTable(new int[][]{
        {0, 0, 0},
        {100, 200, 800},
        {300, 400, 1200},
        {500, 0, 1600},
        {800, 0, 2600}
    });

    private final int[][] values;

    public ScoreTable(int[][] values) {
        this.values = values;
    }

    public int get(int lines, int spin) {
        return values[lines][spin];
    }
}
