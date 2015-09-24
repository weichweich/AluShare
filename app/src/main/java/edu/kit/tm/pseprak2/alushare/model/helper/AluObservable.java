package edu.kit.tm.pseprak2.alushare.model.helper;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Albrecht Weiche
 */
public class AluObservable<E> {
    List<WeakReference<AluObserver<E>>> observerList = new CopyOnWriteArrayList<>();

    public void notifyUpdated(E data) {
        int index = 0;
        while (index < observerList.size()) {
            AluObserver<E> observer = observerList.get(index).get();
            if (observer != null) {
                observer.updated(data);
                index++;
            } else {
                observerList.remove(index);
            }
        }
    }

    public void notifyInserted(E data) {
        int index = 0;
        while (index < observerList.size()) {
            AluObserver<E> observer = observerList.get(index).get();
            if (observer != null) {
                observer.inserted(data);
                index++;
            } else {
                observerList.remove(index);
            }
        }
    }

    public void notifyRemoved(E data) {
        int index = 0;
        while (index < observerList.size()) {
            AluObserver<E> observer = observerList.get(index).get();
            if (observer != null) {
                observer.removed(data);
                index++;
            } else {
                observerList.remove(index);
            }
        }
    }


    public void addObserver(AluObserver<E> aluObserver) {
        observerList.add(new WeakReference<AluObserver<E>>(aluObserver));
    }
}
