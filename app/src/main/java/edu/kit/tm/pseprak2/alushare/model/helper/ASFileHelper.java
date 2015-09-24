package edu.kit.tm.pseprak2.alushare.model.helper;


import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Data;

/**
 * Defines more method signatures that are needed for inserting/updating/deleting a {@link ASFile}
 * to a data source.
 */
public abstract class ASFileHelper extends Helper<ASFile> {

    /**
     * Gets an ASFile from the data source by a given unique identifier.
     * @param fileID the unique identifier of the asfile.
     * @return the asfile.
     */
    public abstract ASFile getFileByID(long fileID);

    /**
     * Gets an ASFile from the data source by a given unique data identifier.
     * @param dataID unique data identifier.
     * @return the asfile.
     */
    public abstract ASFile getFileByDataID(long dataID);

    /**
     * Gets a list of all ASFiles in the data source.
     * @return list of all ASFiles.
     */
    public abstract List<ASFile> getFiles();

    /**
     * Gets a list of all ASFiles in the data source with a given limit and offset.
     * @param limit Amount of asfiles to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return list of all ASFiles.
     */
    public abstract List<ASFile> getFiles(int limit, int offset);

    /**
     * Searches all ASFiles by a given name and returns them.
     * @param name name to search for.
     * @return list of ASFiles.
     */
    public abstract List<ASFile> getFilesByName(String name);

    /**
     * Searches all ASFiles by a given name and returns them.
     * @param limit Amount of asfiles to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @param name name to search for.
     * @return list of ASFiles.
     */
    public abstract List<ASFile> getFilesByName(int limit, int offset, String name);

    /**
     * Gets all received ASFiles from the data source.
     * @return list of all received ASFiles.
     */
    public abstract List<ASFile> getReceivedFiles();

    /**
     * Gets all received ASFiles from the data source.
     * @param limit Amount of asfiles to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return list of all received ASFiles.
     */
    public abstract List<ASFile> getReceivedFiles(int limit, int offset);

    /**
     * Searches all received ASFiles by a given name and returns them.
     * @param name name to search for.
     * @return list of received ASFiles.
     */
    public abstract List<ASFile> getReceivedFilesByName(String name);

    /**
     * Searches all received ASFiles by a given name and returns them.
     * @param limit Amount of asfiles to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @param name name to search for.
     * @return list of received ASFiles.
     */
    public abstract List<ASFile> getReceivedFilesByName(int limit, int offset, String name);

    /**
     * Gets all sent ASFiles from the data source.
     * @return list of all sent ASFiles.
     */
    public abstract List<ASFile> getSendFiles();

    /**
     * Gets all sent ASFiles from the data source.
     * @param limit Amount of asfiles to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return list of all sent ASFiles.
     */
    public abstract List<ASFile> getSendFiles(int limit, int offset);

    /**
     * Searches all sent ASFiles by a given name and returns them.
     * @param name name to search for.
     * @return list of sent ASFiles.
     */
    public abstract List<ASFile> getSendFilesByName(String name);

    /**
     * Searches all sent ASFiles by a given name and returns them.
     * @param limit Amount of asfiles to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @param name name to search for.
     * @return list of sent ASFiles.
     */
    public abstract List<ASFile> getSendFilesByName(int limit, int offset, String name);
}
