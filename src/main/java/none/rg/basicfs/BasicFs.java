package none.rg.basicfs;

import none.rg.basicfs.operations.TreeOperations;
import none.rg.basicfs.operations.Reading;
import none.rg.basicfs.operations.Traversing;
import none.rg.basicfs.operations.Writing;
import none.rg.basicfs.storage.FileStorage;
import none.rg.basicfs.storage.PhysicalStorage;

import java.io.InputStream;

public class BasicFs {

    private BlockStorage blocks;

    private Traversing traversing;
    private TreeOperations treeOperations;
    private Writing writing;
    private Reading reading;

    public BasicFs(String fileName) {
        FileStorage storage = new FileStorage();
        storage.init(fileName, Block.SIZE);
        init(storage);
    }

    public BasicFs(PhysicalStorage storage) {
        init(storage);
    }

    private void init(PhysicalStorage storage) {
        blocks = new BlockStorage(storage);
        traversing = new Traversing(blocks);
        treeOperations = new TreeOperations(blocks, traversing);
        writing = new Writing(blocks);
        reading = new Reading(blocks);
        if (blocks.size() == 0) {
            treeOperations.createRootDirectory();
        }
    }

    public void close() {
        blocks.close();
    }

    public void makeDirectory(String path, String name) {
        createDirOrFile(path, name, HeaderBlock.Type.DIRECTORY);
    }

    public void createFile(String path, String name, InputStream input) {
        HeaderBlock fileHead = createDirOrFile(path, name, HeaderBlock.Type.FILE);
        writing.appendFile(fileHead, input);
    }

    public void appendFile(String path, InputStream input) {
        HeaderBlock fileHead = traversing.findBlockOrError(path);
        writing.appendFile(fileHead, input);
    }

    private HeaderBlock createDirOrFile(String path, String name, HeaderBlock.Type type) {
        HeaderBlock dir = traversing.findBlockOrError(path);
        return treeOperations.createDirectoryEntry(dir, name, type);
    }

    public int fileSize(String path) {
        return traversing.findBlockOrError(path).getSize();
    }

    public ReadingHandle startReading(String path) {
        HeaderBlock file = traversing.findBlockOrError(path);
        return new ReadingHandle(file.getContentLink(), file.getSize());
    }
    
    public void rename(String path, String name) {
        HeaderBlock block = traversing.findBlockOrError(path);
        treeOperations.rename(block, name);
    }
    
    public void move(String path, String newPath) {
        HeaderBlock block = traversing.findBlockOrError(path);
        HeaderBlock dir = traversing.findBlockOrError(newPath);
        treeOperations.move(block, dir);
    }

    public class ReadingHandle {

        private Reading.Cursor cursor;

        public ReadingHandle(int address, int size) {
            cursor = new Reading.Cursor(address, size);
        }

        public int read(byte[] buffer) {
            return read(buffer, 0, buffer.length);
        }

        public int read(byte[] buffer, int start, int length) {
            return reading.readMore(cursor, buffer, start, length);
        }


    }

}
