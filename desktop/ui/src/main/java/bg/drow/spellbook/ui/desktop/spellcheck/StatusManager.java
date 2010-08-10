package bg.drow.spellbook.ui.desktop.spellcheck;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 */
public class StatusManager {

    private static final StatusManager INSTANCE = new StatusManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusManager.class);
    private final List<StatusObserver> observers = new LinkedList<StatusObserver>();

    private StatusManager() {
    }

    public static StatusManager getInstance() {
        return INSTANCE;
    }

    public void setStatus(String message) {
        if (message == null || message.isEmpty()) {
            LOGGER.error("message is null or empty");
            throw new IllegalArgumentException("message is null or empty");
        }

        List<StatusObserver> currentList = null;

        synchronized (this) {
            if (observers.isEmpty()) {
                return;
            }
            currentList = new ArrayList<StatusObserver>(observers);
        }
        
        for (StatusObserver observer : currentList) {
            observer.setStatus(message);
        }
    }

    public synchronized void addObserver(StatusObserver observer) {
        if (observer == null) {
            LOGGER.error("observer is null");
            throw new NullPointerException("observer is null");
        }

        observers.add(observer);
    }

    public synchronized void removeObserver(StatusObserver observer) {
        if (observer == null) {
            LOGGER.error("observer is null");
            throw new NullPointerException("observer is null");
        }

        observers.remove(observer);
    }

    public static interface StatusObserver {

        void setStatus(String message);
    }
}


