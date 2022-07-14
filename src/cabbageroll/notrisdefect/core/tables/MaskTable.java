package cabbageroll.notrisdefect.core.tables;

import cabbageroll.notrisdefect.core.Point;

public class MaskTable {
    public static final MaskTable SRS = new MaskTable(new int[][][][]{
        {
            {{2, 0}, {0, 1}, {0, 2}, {1, 2}, {2, 2}},
            {{2, 2}, {1, 0}, {0, 0}, {0, 1}, {0, 2}},
            {{0, 2}, {2, 1}, {2, 0}, {1, 0}, {0, 0}},
            {{0, 0}, {1, 2}, {2, 2}, {2, 1}, {2, 0}}
        },
        {
            {{0, 0}, {1, 0}, {0, 2}, {1, 2}, {2, 2}},
            {{2, 0}, {2, 1}, {0, 0}, {0, 1}, {0, 2}},
            {{2, 2}, {1, 2}, {2, 0}, {1, 0}, {0, 0}},
            {{0, 2}, {0, 1}, {2, 2}, {2, 1}, {2, 0}}
        },
        {
            {}
        },
        {
            {{0, 0}, {2, 1}, {0, 2}, {1, 2}, {2, 2}},
            {{2, 0}, {1, 2}, {0, 0}, {0, 1}, {0, 2}},
            {{2, 2}, {0, 1}, {2, 0}, {1, 0}, {0, 0}},
            {{0, 2}, {1, 0}, {2, 2}, {2, 1}, {2, 0}}
        },
        {
            {},
            {},
            {},
            {}
        },
        {
            {{1, 0}, {2, 0}, {0, 2}, {1, 2}, {2, 2}},
            {{2, 1}, {2, 2}, {0, 0}, {0, 1}, {0, 2}},
            {{2, 1}, {0, 2}, {2, 0}, {1, 0}, {0, 0}},
            {{0, 1}, {0, 0}, {2, 2}, {2, 1}, {2, 0}}
        },
        {
            {{0, 0}, {2, 0}, {0, 2}, {1, 2}, {2, 2}},
            {{2, 0}, {2, 2}, {0, 0}, {0, 1}, {0, 2}},
            {{2, 2}, {0, 2}, {2, 0}, {1, 0}, {0, 0}},
            {{0, 2}, {0, 0}, {2, 2}, {2, 1}, {2, 0}}
        }
    }, new int[][]{
        {5, 5, 3, 0, 3},
        {5, 5, 3, 0, 3},
        {},
        {5, 5, 3, 0, 3},
        {},
        {5, 5, 3, 0, 3},
        {5, 5, 3, 0, 3}

    });
    Point[][][] masks;
    int[][] scores;

    public MaskTable(int[][][][] masks, int[][] scores) {
        this.masks = new Point[masks.length][][];
        for (int i = 0; i < masks.length; i++) {
            int[][][] piece = masks[i];
            this.masks[i] = new Point[piece.length][];
            for (int j = 0; j < piece.length; j++) {
                int[][] rotation = piece[j];
                this.masks[i][j] = new Point[rotation.length];
                for (int k = 0; k < rotation.length; k++) {
                    int[] offset = rotation[k];
                    this.masks[i][j][k] = new Point(offset[0], offset[1]);
                }
            }
        }
        this.scores = scores;
    }

    public Point[] getPoints(int piece, int rotation) {
        return masks[piece][rotation];
    }

    public int[] getScores(int piece) {
        return scores[piece];
    }
}
