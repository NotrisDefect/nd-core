package cabbageroll.notrisdefect.core.tables;

import java.awt.Point;

public class MaskTable {
    public static final MaskTable SRS = new MaskTable(new Point[][][]{
        {
            {new Point(2, 0), new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(2, 2)},
            {new Point(2, 2), new Point(1, 0), new Point(0, 0), new Point(0, 1), new Point(0, 2)},
            {new Point(0, 2), new Point(2, 1), new Point(2, 0), new Point(1, 0), new Point(0, 0)},
            {new Point(0, 0), new Point(1, 2), new Point(2, 2), new Point(2, 1), new Point(2, 0)}
        },
        {
            {new Point(0, 0), new Point(1, 0), new Point(0, 2), new Point(1, 2), new Point(2, 2)},
            {new Point(2, 0), new Point(2, 1), new Point(0, 0), new Point(0, 1), new Point(0, 2)},
            {new Point(2, 2), new Point(1, 2), new Point(2, 0), new Point(1, 0), new Point(0, 0)},
            {new Point(0, 2), new Point(0, 1), new Point(2, 2), new Point(2, 1), new Point(2, 0)}
        },
        {
            {},
            {},
            {},
            {}
        },
        {
            {new Point(0, 0), new Point(2, 1), new Point(0, 2), new Point(1, 2), new Point(2, 2)},
            {new Point(2, 0), new Point(1, 2), new Point(0, 0), new Point(0, 1), new Point(0, 2)},
            {new Point(2, 2), new Point(0, 1), new Point(2, 0), new Point(1, 0), new Point(0, 0)},
            {new Point(0, 2), new Point(1, 0), new Point(2, 2), new Point(2, 1), new Point(2, 0)}
        },
        {
            {},
            {},
            {},
            {}
        },
        {
            {new Point(1, 0), new Point(2, 0), new Point(0, 2), new Point(1, 2), new Point(2, 2)},
            {new Point(2, 1), new Point(2, 2), new Point(0, 0), new Point(0, 1), new Point(0, 2)},
            {new Point(2, 1), new Point(0, 2), new Point(2, 0), new Point(1, 0), new Point(0, 0)},
            {new Point(0, 1), new Point(0, 0), new Point(2, 2), new Point(2, 1), new Point(2, 0)}
        },
        {
            {new Point(0, 0), new Point(2, 0), new Point(0, 2), new Point(1, 2), new Point(2, 2)},
            {new Point(2, 0), new Point(2, 2), new Point(0, 0), new Point(0, 1), new Point(0, 2)},
            {new Point(2, 2), new Point(0, 2), new Point(2, 0), new Point(1, 0), new Point(0, 0)},
            {new Point(0, 2), new Point(0, 0), new Point(2, 2), new Point(2, 1), new Point(2, 0)}
        }
    }, new int[][]{
        {2, 2, -1, -1, 0},
        {2, 2, -1, -1, 0},
        {},
        {2, 2, -1, -1, 0},
        {},
        {2, 2, -1, -1, 0},
        {5, 5, 3, 0, 3}

    });
    Point[][][] masks;
    int[][] scores;

    public MaskTable(Point[][][] masks, int[][] scores) {
        this.masks = masks;
        this.scores = scores;
    }

    public Point[] getPoints(int piece, int rotation) {
        return masks[piece-1][rotation];
    }

    public int[] getScores(int piece) {
        return scores[piece-1];
    }
}
