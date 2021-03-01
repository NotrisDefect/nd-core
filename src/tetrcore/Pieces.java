package tetrcore;

import java.awt.Point;

public class Pieces {
    public static final Point[][][] pieces = {
        //Rotations: 0,R,2,L
        //Z
        {
            {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(2, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
            {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2)}
        },
        //L
        {
            {new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2)},
            {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2)}
        },
        //O
        {
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(2, 1)},
        },
        //S
        {

            {new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)},
            {new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(2, 2)},
            {new Point(1, 1), new Point(2, 1), new Point(0, 2), new Point(1, 2)},
            {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}
        },
        //I
        {
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1)},
            {new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(2, 3)},
            {new Point(0, 2), new Point(1, 2), new Point(2, 2), new Point(3, 2)},
            {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3)}
        },
        //J
        {
            {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(1, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2)},
            {new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}
        },
        //T
        {
            {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)},
            {new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
            {new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2)},
            {new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}
        }
    };
}
