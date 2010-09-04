package bg.drow.spellbook.core.service;

import bg.drow.spellbook.core.SpellbookConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Base service class. Provides access to the application's database.
 * It also wraps @Transactional methods in transactions automatically.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class AbstractPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPersistenceService.class);

    //accessible in subclasses directly
    protected static EntityManager EM;

    protected AbstractPersistenceService() {
        // there can be only one entity manager ;-)
        if (EM == null) {
            initEntityManager(SpellbookConstants.SPELLBOOK_DB_PATH);
        } else {
            LOGGER.info("Entity manager is already initialized");
        }
    }

    private static void initEntityManager(String dbFile) {
        LOGGER.info("dictionary database: " + dbFile.replace(".h2.db", ""));

        String url = "jdbc:h2:" + dbFile.replace(".h2.db", "");

        // we need to override the db url from persistence.xml
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("hibernate.connection.url", url);

        EM = Persistence.createEntityManagerFactory("Spellbook", properties).createEntityManager();
    }

    protected static class TransactionInvocationHandler<T> implements InvocationHandler {
        private final T underlying;

        public TransactionInvocationHandler(T underlying) {
            this.underlying = underlying;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getAnnotation(Transactional.class) != null) {
                LOGGER.debug("BEGIN TRANSACTION");
                EM.getTransaction().begin();
            }

            StringBuffer sb = new StringBuffer();
            sb.append(method.getName());
            sb.append("(");
            for (int i = 0; args != null && i < args.length; i++) {
                if (i != 0)
                    sb.append(", ");
                sb.append(args[i]);
            }
            sb.append(")");
            Object ret = method.invoke(underlying, args);
            if (ret != null) {
                sb.append(" -> ");
                sb.append(ret);
            }
            LOGGER.debug(sb.toString());

            if (method.getAnnotation(Transactional.class) != null) {
                LOGGER.debug("COMMIT TRANSACTION");
                EM.getTransaction().commit();
            }
            return ret;
        }
    }
}
