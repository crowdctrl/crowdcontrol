package unarmed.web;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import holon.Holon;
import holon.api.Application;
import holon.api.config.Config;
import holon.api.config.Setting;
import holon.contrib.caching.HttpCache;
import unarmed.domain.Counties;
import unarmed.domain.Reports;
import unarmed.domain.subscriptions.Subscriptions;
import unarmed.domain.auth.Users;

import java.nio.file.Path;
import java.util.Collection;

import static holon.api.config.Setting.defaultValue;
import static holon.api.config.Setting.setting;
import static holon.api.config.SettingConverters.path;
import static holon.api.config.SettingConverters.string;
import static java.util.Arrays.asList;

public class Unarmed implements Application
{
    private ComboPooledDataSource ds;
    private HttpCache httpCache;

    public static class Configuration
    {
        public static Setting<Path> report_path = setting( "unarmed.report_path", path(), defaultValue("/tmp/upload") );
        public static Setting<String> dbUrl = setting( "unarmed.db", string(), defaultValue("jdbc:neo4j://localhost:7474") );
        public static Setting<Path> cache_path = setting( "unarmed.cache_path", path(), defaultValue("/tmp/.unarmed_cache") );
    }

    public static void main(String ... args) throws Exception
    {
        Holon.run( Unarmed.class );
    }

    @Override
    public Collection<Object> startup( Config config ) throws Exception
    {
        ds = new ComboPooledDataSource();
        ds.setDriverClass( "org.neo4j.jdbc.Driver" );
        ds.setJdbcUrl( config.get( Configuration.dbUrl ) );

        Users users = new Users(ds);
        users.ensureAdminExists( "admin", "admin" );

        httpCache = new HttpCache( config.get( Configuration.cache_path ) );
        return asList(
            new Reports(config.get( Configuration.report_path )),
            new Counties(),
            new Subscriptions(),
            httpCache,
            users,
            ds );
    }

    @Override
    public void shutdown()
    {
        ds.close();
        httpCache.stop();
    }
}