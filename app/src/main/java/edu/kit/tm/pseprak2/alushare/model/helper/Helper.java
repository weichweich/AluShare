package edu.kit.tm.pseprak2.alushare.model.helper;

/**
 * Class defines essential methods that a helper needs to have for loading, saving and deleting objects
 * to any data source.
 * @param <E> object type to save/load to/from a data source.
 */
public abstract class Helper<E> extends AluObservable<E> {
    /**
     * Inserts a given object into a data source.
     * @param obj the object to insert.
     */
    public abstract void insert(E obj);

    /**
     * Updates a given object that is already in a data source.
     * @param obj the object to update.
     */
    public abstract void update(E obj);

    /**
     * Deletes a given object from the data source.
     * @param obj the object to delete.
     */
    public abstract void delete(E obj);

    /**
     * Checks if a given object already exists in the data source.
     * @param obj object to check for.
     * @return true if object exits already in the data source, otherwise false.
     */
    public abstract boolean exist(E obj);

}
