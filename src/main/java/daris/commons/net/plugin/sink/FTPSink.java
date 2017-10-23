package daris.commons.net.plugin.sink;

import java.util.Map;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.PasswordType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.sink.ParameterDefinition;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.xml.XmlDoc.Element;
import daris.commons.net.ftp.FTPClient;
import daris.commons.net.ftp.FTPClientFactory;
import daris.plugin.sink.AbstractDataSink;
import daris.plugin.sink.util.OutputPath;
import io.github.xtman.util.PathUtils;

public class FTPSink extends AbstractDataSink {

    public static final String TYPE_NAME = "daris-ftp";

    public static final String PARAM_HOST = "host";
    public static final String PARAM_PORT = "port";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_DIRECTORY = "directory";
    public static final String PARAM_UNARCHIVE = "unarchive";

    public static final int DEFAULT_FTP_PORT = 21;

    public FTPSink() throws Throwable {
        super(TYPE_NAME);
    }

    public String[] acceptedTypes() throws Throwable {
        return null;
    }

    @Override
    public Object beginMultiple(Map<String, String> params) throws Throwable {
        validateParams(params);
        return getClient(params);
    }

    public int compressionLevelRequired() {
        // don't care
        return -1;
    }

    public void consume(Object multiTransferContext, String path, Map<String, String> params, Element userMeta,
            Element assetMeta, LongInputStream in, String appMimeType, String streamMimeType, long length)
            throws Throwable {
        if (multiTransferContext == null) {
            // if it is in multi transfer context, params were already validated
            // in beginMultiple() method.
            validateParams(params);
        }
        String directory = params.get(PARAM_DIRECTORY);
        String assetSpecificOutputPath = multiTransferContext != null ? null : getAssetSpecificOutput(params);
        boolean unarchive = Boolean.parseBoolean(params.getOrDefault(PARAM_UNARCHIVE, "false"));
        String mimeType = streamMimeType;
        if (mimeType == null && assetMeta != null) {
            mimeType = assetMeta.value("content/type");
        }
        if (!ArchiveRegistry.isAnArchive(mimeType) && unarchive) {
            unarchive = false;
        }
        /*
         * 
         */
        FTPClient client = null;
        try {
            client = getClient(multiTransferContext, params);
            if (unarchive) {
                String dirPath = OutputPath.getOutputPath(directory, assetSpecificOutputPath, path, assetMeta, true);
                ArchiveInput ai = ArchiveRegistry.createInput(in, new NamedMimeType(mimeType));
                ArchiveInput.Entry entry;
                try {
                    while ((entry = ai.next()) != null) {
                        try {
                            if (entry.isDirectory()) {
                                client.mkdirs(PathUtils.join(dirPath, entry.name()));
                            } else {
                                client.put(PathUtils.join(dirPath, entry.name()), entry.stream());
                            }
                        } finally {
                            ai.closeEntry();
                        }
                    }
                } finally {
                    ai.close();
                }
            } else {
                String remoteFilePath = OutputPath.getOutputPath(directory, assetSpecificOutputPath, path, assetMeta,
                        false);
                client.put(remoteFilePath, in);
            }
        } finally {
            if (multiTransferContext == null && client != null) {
                client.close();
            }
        }
    }

    @Override
    public void endMultiple(Object multiTransferContext) throws Throwable {
        if (multiTransferContext != null) {
            FTPClient client = (FTPClient) multiTransferContext;
            client.close();
        }
    }

    @Override
    public void shutdown() throws Throwable {

    }

    protected void validateParams(Map<String, String> params) {
        if (!params.containsKey(PARAM_HOST)) {
            throw new IllegalArgumentException("Missing host argument.");
        }
        if (!params.containsKey(PARAM_USERNAME)) {
            throw new IllegalArgumentException("Missing username argument.");
        }
        if (!params.containsKey(PARAM_PASSWORD)) {
            throw new IllegalArgumentException("Missing password argument.");
        }
    }

    protected FTPClient getClient(Map<String, String> params) throws Throwable {
        String host = params.get(PARAM_HOST);
        int port = Integer.parseInt(params.getOrDefault(PARAM_PORT, Integer.toString(DEFAULT_FTP_PORT)));
        String username = params.get(PARAM_USERNAME);
        String password = params.get(PARAM_PASSWORD);
        return FTPClientFactory.create(host, port, username, password);
    }

    private FTPClient getClient(Object multiTransferContext, Map<String, String> params) throws Throwable {
        if (multiTransferContext != null) {
            return (FTPClient) multiTransferContext;
        } else {
            return getClient(params);
        }
    }

    @Override
    public String description() throws Throwable {
        return "FTP sink.";
    }

    @Override
    protected void addParameterDefinitions(Map<String, ParameterDefinition> paramDefns) throws Throwable {
        /*
         * init param definitions
         */
        // @formatter:off
        
        // {{                 --- start
        // }}                 --- end
        // default=DEFAULT    --- default value
        // admin              --- for admin only, should not be presented to end user
        // text               --- multiple lines text
        // optional           --- optional
        // xor=PARAM1|PARAM2  --- 
        // mutable            --- 
        // pattern=PATTERN    --- regex pattern to validate string value
        // enum=VALUE1|VALUE2 --- enumerated values
        
        // @formatter:on
        addParameterDefinition(paramDefns, PARAM_HOST, StringType.DEFAULT, "FTP server host.", false);
        addParameterDefinition(paramDefns, PARAM_PORT, new IntegerType(1, 65535),
                "FTP server port. Defaults to 21.{{default=21}}", false);
        addParameterDefinition(paramDefns, PARAM_USERNAME, StringType.DEFAULT, "FTP username.", false);
        addParameterDefinition(paramDefns, PARAM_PASSWORD, PasswordType.DEFAULT, "FTP password.", false);
        addParameterDefinition(paramDefns, PARAM_DIRECTORY, StringType.DEFAULT,
                "The default/base directory on the FTP server. If not specified, defaults to user's home directory.{{optional,mutable}}",
                false);
        addParameterDefinition(paramDefns, PARAM_UNARCHIVE, BooleanType.DEFAULT,
                "Extract archive contents. Defaults to false.{{optional,mutable,default=false}}", false);

    }

}
