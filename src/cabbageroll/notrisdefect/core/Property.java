package cabbageroll.notrisdefect.core;

public class Property {
    private final double base;
    private final long delay;
    private final double delta;
    private final boolean mode;
    private final double limit;

    private double workingValue;

    public Property(double base, int delay, double delta, double limit) {
        this.base = base;
        this.delay = delay;
        this.delta = delta;
        mode = delta > 0;
        this.limit = limit;
    }

    public int getRealValue() {
        return (int) workingValue;
    }

    public double getWorkingValue() {
        return workingValue;
    }

    public void tick(long n) {
        if (n > delay) {
            if (mode ? (workingValue < limit) : (limit < workingValue)) {
                workingValue += delta;
            } else {
                workingValue = limit;
            }
        }
    }

    public void reset() {
        workingValue = base;
    }

}
