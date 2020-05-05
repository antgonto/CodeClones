/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author Fede
 */
public final class ThreadGroup extends java.lang.ThreadGroup {

    /**
     * Singleton class.
     */
    private static ThreadGroup threadGroup;

    /**
     * Method that initializes the thread groupd with a name.
     *
     * @param name The name of the Thread group.
     */
    private ThreadGroup(String name) {
        super(name);
    }

    /**
     * Method that is called by the Java Virtual Machine when a thread in this
     * thread group stops because of an uncaught exception.
     *
     * @see java.lang.ThreadGroup#uncaughtException(java.lang.Thread,
     * java.lang.Throwable)
     */
    public void uncaughtException(Thread thread, Throwable error) {
        //ExceptionNotifier.notify(error);
    }

    /**
     * Method that return the thread groupd.
     *
     * @return The ThreadGroup.
     */
    public static ThreadGroup getInstance() {
        if (threadGroup == null) {
            return new ThreadGroup("ThreadGroupd");
        }
        return threadGroup;
    }
}
