package daris.commons.net.ftp;

import java.io.Closeable;
import java.io.InputStream;

public interface FTPClient extends Closeable {

    void mkdirs(String remoteDirPath) throws Throwable;

    void put(String remoteFilePath, InputStream in) throws Throwable;

}
