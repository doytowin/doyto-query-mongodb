package win.doyto.query.config;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.core.Dialect;

/**
 * GlobalConfiguration
 *
 * @author f0rb
 */
@Getter
@Setter
public class GlobalConfiguration {

    private boolean mapCamelCaseToUnderscore;
    private Dialect dialect = (sql, limit, offset) -> sql + " LIMIT " + limit + (sql.startsWith("SELECT") ? " OFFSET " + offset : "");

    private GlobalConfiguration() {
    }

    public static GlobalConfiguration instance() {
        return Singleton.instance;
    }

    private static class Singleton {
        private static GlobalConfiguration instance = new GlobalConfiguration();
    }

}
