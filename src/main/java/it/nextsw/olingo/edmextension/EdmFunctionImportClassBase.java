package it.nextsw.olingo.edmextension;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Classe di base con utilities che deve essere estesa dalle classi che
 * espongono l'annotation {@link EdmFunctionImportClass}
 */
@EdmFunctionImportClass
public abstract class EdmFunctionImportClassBase {

    /**
     * Classe che a partire da una queryDSL crea un {@link JPAQueryInfo} con una
     * countQuery {@link JPAQueryInfo#getCountQuery()} clonata dal parametro
     * queryDSL e che ha come select il parametro selectCountExpression
     *
     * @param queryDSL
     * @param selectCountExpression
     * @param em
     * @return
     */
    protected JPAQueryInfo createQueryInfo(JPAQuery queryDSL, Expression selectCountExpression, EntityManager em) {
        JPAQueryInfo jpaQueryInfo = new JPAQueryInfo();

        Query query = queryDSL.createQuery();
        queryDSL.getMetadata().clearOrderBy();
        Query countQuery = queryDSL.clone(em).select(selectCountExpression).createQuery();
        jpaQueryInfo.setQuery(query);
        jpaQueryInfo.setCountQuery(countQuery);
        return jpaQueryInfo;
    }
}
