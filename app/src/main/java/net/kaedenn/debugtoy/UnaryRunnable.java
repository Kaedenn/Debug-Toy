package net.kaedenn.debugtoy;

/** Class implementing the Runnable interface, but providing access to a parameter. */
public abstract class UnaryRunnable implements Runnable {
    private Object arg;
    public UnaryRunnable() {
        arg = null;
    }
    public UnaryRunnable(Object argument) {
        arg = argument;
    }
    public void bind(Object argument) {
        arg = argument;
    }
    public void run() {
        runWith(arg);
    }
    public abstract void runWith(Object argument);
}
