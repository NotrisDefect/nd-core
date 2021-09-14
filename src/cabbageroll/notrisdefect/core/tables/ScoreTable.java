package cabbageroll.notrisdefect.core.tables;

public class ScoreTable {

    public static final ScoreTable NORMAL = new ScoreTable(new int[][]{
        {0, 100, 400},
        {100, 200, 800},
        {300, 400, 1200},
        {500, 0, 1600},
        {800, 0, 2600}
    }, 3500, 50, 1, 2, 1.5);

    private final int[][] values;
    private final int allClear;
    private final int combo;
    private final int softDrop;
    private final int hardDrop;
    private final double b2bMulti;

    public ScoreTable(int[][] values, int allClear, int combo, int softDrop, int hardDrop, double b2bMulti) {
        this.values = values;
        this.allClear = allClear;
        this.combo = combo;
        this.softDrop = softDrop;
        this.hardDrop = hardDrop;
        this.b2bMulti = b2bMulti;
    }

    public int get(int lines, int spin) {
        return values[lines][spin];
    }

    public int getAllClear() {
        return allClear;
    }

    public double getB2bMulti() {
        return b2bMulti;
    }

    public int getCombo() {
        return combo;
    }

    public int getHardDrop() {
        return hardDrop;
    }

    public int getSoftDrop() {
        return softDrop;
    }
}
