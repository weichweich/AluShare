package edu.kit.tm.pseprak2.alushare.model;

/**
 * Created by dominik on 02.07.15.
 */

import android.content.Context;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Holds all necessary information about a ASFile that is currently
 * generated or got loaded from a data source.
 */
public class ASFile extends File {
    private long id = -1;
    private long dataId = -1;
    private String asName;
    private Boolean received = false;

    /**
     * Constructor that's only used by the {@link edu.kit.tm.pseprak2.alushare.model.helper.Helper}
     * classes to create an instance out of the information that's stored in a data source
     * (e.g SQLite Database).
     * @param id Unique identifier that was set by the data source.
     * @param dataID Unique identifier of a {@link Data} object.
     * @param path Path to an existing file in the filesystem.
     * @param asName Name of the file thats shown in the {@link edu.kit.tm.pseprak2.alushare.presenter.FileTabPresenter}.
     * @param received True if the file was received otherwise false.
     */
    public ASFile(long id, long dataID, String path, String asName, Boolean received) {
        super(path);
        setId(id);
        setDataId(dataID);
        setASName(asName);
        setReceived(received);
    }

    /**
     * Constructor that creates an instance by given context, name and a {@link Data} identifier.
     * @param context Context for creating a file path.
     * @param asName Name of the file thats shown in the {@link edu.kit.tm.pseprak2.alushare.presenter.FileTabPresenter}.
     * @param dataID Unique identifier of a {@link Data} object.
     */
    public ASFile(Context context, String asName, long dataID) {
        this(context, asName);
        setDataId(dataID);
    }

    /**
     * Constructor that creates an instance by given context and name.
     * @param context Context for creating a file path.
     * @param asName Name of the file thats shown in the {@link edu.kit.tm.pseprak2.alushare.presenter.FileTabPresenter}.
     */
    public ASFile(Context context, String asName) {
        super(context.getFilesDir(), generateHashedName() + "_" + asName);
        setASName(asName);
    }

    /**
     * Returns boolean received value.
     * @return true if file was received otherwise false.
     */
    public Boolean getReceived() {
        return this.received;
    }

    /**
     * Sets boolean received value with given parameter.
     * @param received Should be true if file was received otherwise false.
     */
    public void setReceived(Boolean received) {
        this.received = received;
    }

    /**
     * Returns the unique identifier of the ASFile.
     * @return long value that's the unique identifier of ASFile.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets the unique identifier of the ASFile.
     * @param id the unique identifier of the ASFile.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the name of the ASFile.
     * @return string value that's the name of the ASFile.
     */
    public String getASName() {
        return this.asName;
    }

    /**
     * Sets the name of the ASFile.
     * @param asName the name of the ASFile.
     */
    public void setASName(String asName) {
        this.asName = asName;
    }

    /**
     * Returns the unique identifier of the data object that is linked to this ASFile.
     * @return long value that's the unique identifier of the data object.
     */
    public long getDataId() {
        return dataId;
    }

    /**
     * Sets the unique identifier of the data object that is linked to this ASFile.
     * @param dataId the unique identifier of the data object,
     */
    public void setDataId(long dataId) {
        this.dataId = dataId;
    }

    //TODO: Check if name already exists.
    private static String generateHashedName() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

}