package gameserver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class BlockingMatchMaking<T> {
    private boolean empty = true;
    private boolean full = false;
    private final ArrayDeque<T> ticketsQueue;


    public BlockingMatchMaking(int size) {
        this.ticketsQueue = new ArrayDeque<T>(size);
    }

    public synchronized T take() {
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
        notifyAll();
        return ticketsQueue.poll();
    }

    public synchronized void fill(T newTicket) {
        //wait untill both tickets have been retrived
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
        ticketsQueue.add(newTicket);
        ticketsQueue.add(newTicket);
        notifyAll();
    }

}

