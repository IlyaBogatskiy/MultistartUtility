package com.ibdev.desktop.application.model;

import java.io.File;

/**
 * @author VPVXNC
 */
public class FileItem {

    private final File file;
    private boolean selected;

    public FileItem(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
