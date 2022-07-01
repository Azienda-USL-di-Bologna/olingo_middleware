package it.nextsw.olingo.querybuilder;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**todo a seguito del fork questa classe è da eliminare
 *
 * Created by f.longhitano on 28/06/2017.
 */
public class JPAQueryInfo {
    private Query query = null;
    private boolean isTombstoneQuery = false;

    /**
     * Devo chiamare i metodi con la reflection in quanto {@link org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryBuilder.JPAQueryInfo} non accessibile fuori dal package olingo
     *
     * @param jpaQueryInfo object istanza di JPAQueryBuilder.JPAQueryInfo
     */
    public JPAQueryInfo(Object jpaQueryInfo) {
        try {
            Method method=jpaQueryInfo.getClass().getDeclaredMethod("getQuery");
            method.setAccessible(true);
            this.query= (Query) method.invoke(jpaQueryInfo);
            method=jpaQueryInfo.getClass().getDeclaredMethod("isTombstoneQuery");
            method.setAccessible(true);
            this.isTombstoneQuery= (boolean) method.invoke(jpaQueryInfo);

        } catch (Exception e) {
            throw new ClassCastException("The object "+jpaQueryInfo+" is not of class JPAQueryBuilder.JPAQueryInfo");
        }
        this.query = query;
        this.isTombstoneQuery = isTombstoneQuery;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public boolean isTombstoneQuery() {
        return isTombstoneQuery;
    }

    public void setTombstoneQuery(boolean isTombstoneQuery) {
        this.isTombstoneQuery = isTombstoneQuery;
    }
}
