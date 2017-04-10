package daris.commons.net.ftp.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import daris.commons.net.ftp.FTPClient;
import daris.commons.net.ftp.FTPClientFactory;

public class FTPClientTest {

    private FTPClient client;
    private String filePath;
    private String destFilePath;

    @Before
    public void prepare() throws Throwable {
        Map<String, String> settings = TestUtils.getTestSettings();
        String serverHost = settings.get("server.host");
        int serverPort = settings.get("server.port") == null ? 21 : Integer.parseInt(settings.get("server.port"));
        String username = settings.get("username");
        String password = settings.get("password");
        client = FTPClientFactory.create(serverHost, serverPort, username, password);

        System.out.print("Creating temporary file...");
        File file = TestUtils.generateTempFile(1024);
        System.out.println("done.");
        filePath = file.getCanonicalPath();
        destFilePath = "daris-commons-net-plugin-test3/a/b/c/d/test-" + System.currentTimeMillis() + ".dat";
    }

    @Test
    public void test() {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        long duration = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            final int j = i + 1;
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        String dstPath = destFilePath + "." + (j % 4);
                        System.out.println(Thread.currentThread().getName() + ": PUT: " + dstPath);
                        InputStream in = new BufferedInputStream(new FileInputStream(filePath));
                        try {
                            client.put(dstPath, in);
                        } finally {
                            in.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        duration = System.currentTimeMillis() - duration;
        System.out.println("Duration: " + (((double) duration) / 1000.0) + " seconds.");
    }

    @After
    public void cleanup() throws IOException {
        if (client != null) {
            try {
                client.close();
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
        System.out.print("Deleting temporary file...");
        Files.delete(Paths.get(filePath));
        System.out.println("done.");
    }
}
