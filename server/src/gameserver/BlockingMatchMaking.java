package gameserver;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class BlockingMatchMaking<T> {
    private boolean empty = true;
    private boolean full = false;
    private final ArrayDeque<T> queue;


    public BlockingMatchMaking(int size) {
        this.queue = new ArrayDeque<T>(size);
    }

    public synchronized T poll() {
        //wait until ticket is available
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //toggle status
        if (full)
            full = false;
        else
            empty = true;
        T item = queue.poll();
        notifyAll();
        return item;
    }

    public synchronized boolean remove(T obj) {
        //wait until ticket is available
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //toggle status
        if (full)
            full = false;
        else
            empty = true;
        boolean item = queue.remove(obj);
        notifyAll();
        return item;
    }

    public synchronized ArrayList<T> clear() {
        //wait until ticket is available
        ArrayList<T> list = new ArrayList<>();
        while (!full) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //toggle status
        empty = true;
        full = false;
        list.add(queue.poll());
        list.add(queue.poll());
        notifyAll();
        return list;
    }

    public synchronized void fill(T newTicket) {
        //wait untill both tickets have been retrieved
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //toggle status
        empty = false;
        full = true;
        queue.add(newTicket);
        queue.add(newTicket);
        notifyAll();
    }

    public synchronized void put(T newTicket) {
        //wait untill both tickets have been retrieved
        while (full) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!empty)
            full = true;
        else
            empty = false;
        queue.add(newTicket);
        notifyAll();
    }

}

