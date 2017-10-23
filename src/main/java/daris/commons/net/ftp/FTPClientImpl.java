package daris.commons.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.net.ftp.FTP;

import io.github.xtman.util.PathUtils;

public class FTPClientImpl implements FTPClient {

    private org.apache.commons.net.ftp.FTPClient _client;
    private ReentrantLock _lock;

    public FTPClientImpl(String serverHost, int serverPort, boolean passiveMode, String username, String password)
            throws Throwable {
        _lock = new ReentrantLock();

        _client = new org.apache.commons.net.ftp.FTPClient();
        _client.setUseEPSVwithIPv4(true);

        // connect
        _client.connect(serverHost, serverPort);

        // login
        if (!_client.login(username, password)) {
            throw new Exception("Failed to login.");
        }

        if (passiveMode) {
            _client.enterLocalPassiveMode();
        } else {
            _client.enterLocalActiveMode();
        }

    }

    @Override
    public void close() throws IOException {
        if (_client.isConnected()) {
            try {
                _client.logout();
            } finally {
                _client.disconnect();
            }
        }
    }

    private boolean directoryExists(String remoteDirPath) throws Throwable {
        try {
            _lock.lock();
            if (remoteDirPath != null) {
                String cwd = _client.printWorkingDirectory();
                if (_client.changeWorkingDirectory(remoteDirPath)) {
                    if (cwd != null) {
                        if (!_client.changeWorkingDirectory(cwd)) {
                            throw new Exception("Failed to change working directory to: " + cwd);
                        }
                    }
                    return true;
                }
            }
            return false;
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public void mkdirs(String remoteDirPath) throws Throwable {
        try {
            _lock.lock();
            if (directoryExists(remoteDirPath)) {
                return;
            }
            String parentDirPath = PathUtils.getParent(remoteDirPath);
            if (parentDirPath != null) {
                mkdirs(parentDirPath);
            }
            if (!_client.makeDirectory(remoteDirPath)) {
                throw new Exception("FTP: Failed to make directory: " + remoteDirPath);
            }
        } finally {
            _lock.unlock();
        }
    }

    @Override
    public void put(String remoteFilePath, InputStream in) throws Throwable {
        try {
            _lock.lock();
            String parentPath = PathUtils.getParent(remoteFilePath);
            if (parentPath != null) {
                mkdirs(parentPath);
            }
            _client.setFileType(FTP.BINARY_FILE_TYPE);
            if (!_client.storeFile(remoteFilePath, in)) {
                throw new Exception(_client.getReplyString());
            }
        } finally {
            _lock.unlock();
        }
    }

}
