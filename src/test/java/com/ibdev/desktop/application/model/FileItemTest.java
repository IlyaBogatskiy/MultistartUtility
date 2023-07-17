package com.ibdev.desktop.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * @author VPVXNC
 */
class FileItemTest {

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link FileItem#FileItem(File)}
     *   <li>{@link FileItem#setSelected(boolean)}
     *   <li>{@link FileItem#toString()}
     *   <li>{@link FileItem#getFile()}
     *   <li>{@link FileItem#isSelected()}
     * </ul>
     */
    @Test
    void testConstructor() {
        File file = Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toFile();
        FileItem actualFileItem = new FileItem(file);
        actualFileItem.setSelected(true);
        String actualToStringResult = actualFileItem.toString();
        assertSame(file, actualFileItem.getFile());
        assertTrue(actualFileItem.isSelected());
        assertEquals("test.txt", actualToStringResult);
    }
}

