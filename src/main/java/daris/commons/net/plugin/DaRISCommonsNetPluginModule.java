package daris.commons.net.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.DataSinkRegistry;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import daris.commons.net.plugin.sink.FTPSink;

public class DaRISCommonsNetPluginModule implements PluginModule {

    private List<PluginService> _services;

    private FTPSink _ftpSink;

    public DaRISCommonsNetPluginModule() {
        _services = new ArrayList<PluginService>();
    }

    public String description() {
        return "Plugin sinks to access remove FTP server.";
    }

    public void initialize(ConfigurationResolver conf) throws Throwable {
        try {
            if (_ftpSink == null) {
                _ftpSink = new FTPSink();
                DataSinkRegistry.add(this, _ftpSink);
            }
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    public Collection<PluginService> services() {
        return _services;
    }

    public void shutdown(ConfigurationResolver conf) throws Throwable {
        try {
            DataSinkRegistry.removeAll(this);
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    public String vendor() {
        return "Research Platform Services, The University of Melbourne";
    }

    public String version() {
        return "1.0.0";
    }

}
