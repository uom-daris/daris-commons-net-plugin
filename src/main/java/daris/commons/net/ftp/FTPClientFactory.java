package daris.commons.net.ftp;

public class FTPClientFactory {

    public static FTPClient create(String serverHost, int serverPort, boolean passiveMode, String username,
            String password) throws Throwable {
        return new FTPClientImpl(serverHost, serverPort, passiveMode, username, password);
    }

    public static FTPClient create(String serverHost, int serverPort, String username, String password)
            throws Throwable {
        return create(serverHost, serverPort, true, username, password);
    }

    public static FTPClient create(String serverHost, String username, String password) throws Throwable {
        return create(serverHost, 21, true, username, password);
    }

}
