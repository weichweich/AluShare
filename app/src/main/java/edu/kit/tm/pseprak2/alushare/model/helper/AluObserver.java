package edu.kit.tm.pseprak2.alushare.model.helper;

/**
 * @author Albrecht Weiche
 */
public interface AluObserver<E>  {
    void updated(E data);
    void inserted(E data);
    void removed(E data);
}
