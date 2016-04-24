package none.rg.basicfs;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * For now it is going to be used for manual-testing purposes
 * Later we shall make it entry point for utility tool
 */
public class MainTest {

    BasicFs fs;

    public static void main(String... args) {
        new MainTest().run();
    }

    private void run() {
        File f = new File("sample.fs");
        if (f.exists()) {
            f.delete();
        }
        fs = new BasicFs(f.getName());
        fs.createFile("/", "executable.exe", new ByteArrayInputStream(new byte[] {(byte) 0xCD, 0x19}));
        fs.makeDirectory("/", "somedir");
        fs.createFile("/somedir", "file1.txt", new ByteArrayInputStream("Hi, People!\nIt Works!\n".getBytes()));
        fs.createFile("/", "file22.bak", new ByteArrayInputStream(new byte[0]));
        System.out.println(new String(readFile("/somedir/file1.txt")));
        fs.close();
    }

    private byte[] readFile(String path) {
        byte[] buffer = new byte[fs.fileSize(path)];
        fs.startReading(path).read(buffer);
        return buffer;
    }

}
