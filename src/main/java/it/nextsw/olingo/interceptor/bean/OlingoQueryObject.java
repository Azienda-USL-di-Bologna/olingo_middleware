package it.nextsw.olingo.interceptor.bean;

import it.nextsw.olingo.interceptor.exception.QueryInterceptorException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContext;
import org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLSelectContextView;
import org.apache.olingo.odata2.jpa.processor.core.jpql.JPQLSelectSingleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Classe che spacchetta e impacchetta le jpql generate da olingo
 *
 * Created by f.longhitano on 07/07/2017.
 */
public class OlingoQueryObject {

    private static final String DEFAULT_OLINGO_ENTITY_NAME = "E1";
    private static final String DEFAULT_OLINGO_FROM = "FROM";
    private static final String DEFAULT_OLINGO_ORDER_BY = "ORDER BY";
    private static final String DEFAULT_OLINGO_WHERE = "WHERE";
    private static final String DEFAULT_OLINGO_SELECT = "SELECT";

    protected StringTokenizer st;

    /**
     * Lista di stringhe contenete i nomi dei campi da mettere in select, se vuoto si intende la select di {@link #getOlingoEntityName()}
     */
    protected List<String> selectValues;
    /**
     * Lista di stringhe contenete i nomi delle tabelle da mettere in from
     */
    protected List<String> fromValues;
    /**
     * La where generata da olingo, può essere presente o meno
     */
    protected String olingoWhere;

    /**
     * La stringa contenente la where con i predicati aggiuntivi da aggiungere in and alle olingo where (se presenti)
     */
    protected String customWhere;

    /**
     * La lista di stringhe contente le order by
     */
    protected List<String> orderByValues;


    protected String olingoEntityAlias;
    protected String olingoEntityName;


    public OlingoQueryObject(JPQLContext jpqlContext) {
        selectValues = new ArrayList<>();
        fromValues = new ArrayList<>();
        orderByValues = new ArrayList<>();

        olingoEntityAlias = jpqlContext.getJPAEntityAlias();
        olingoEntityName = jpqlContext.getJPAEntityName();
        fromValues.add(olingoEntityName + " " + olingoEntityAlias);
        if (JPQLSelectContextView.class.isAssignableFrom(jpqlContext.getClass())) {
            JPQLSelectContextView jpqlSelectContext = (JPQLSelectContextView) jpqlContext;
            olingoWhere = jpqlSelectContext.getWhereExpression();
            if (jpqlSelectContext.getOrderByCollection() != null)
                splitOrderByValues(jpqlSelectContext.getOrderByCollection());
        } else if (JPQLSelectSingleContext.class.isAssignableFrom(jpqlContext.getClass())) {
            JPQLSelectSingleContext jpqlSelectSingleContext = (JPQLSelectSingleContext) jpqlContext;
            for (int i = 0; i < jpqlSelectSingleContext.getKeyPredicates().size(); i++) {
                KeyPredicate keyPredicate = jpqlSelectSingleContext.getKeyPredicates().get(i);
                try {
                    olingoWhere = olingoEntityAlias + "." + keyPredicate.getProperty().getName() + "=" + keyPredicate.getLiteral() + (i < jpqlSelectSingleContext.getKeyPredicates().size() - 1 ? " and " : "");
                } catch (EdmException e) {
                    throw new QueryInterceptorException(" Exception during read keyPredicate jpqlContext",e);
                }

            }

        }
    }

    public String toJpqlString() {
        String query = "select ";
        //add select values
        if (CollectionUtils.isEmpty(selectValues))
            query += olingoEntityAlias;
        else {
            for (String select : selectValues) {
                query += select + ",";
            }
            if (query.endsWith(","))
                query = query.substring(0, query.length() - 1);
        }

        //add from values
        query+=" from ";
        for (String from : fromValues)
            query += from + ",";
        if (query.endsWith(","))
            query = query.substring(0, query.length() - 1);

        if (StringUtils.isNotBlank(olingoWhere) || StringUtils.isNotBlank(customWhere)) {
            query += " where ";
            if (StringUtils.isNotBlank(olingoWhere))
                query += olingoWhere + " " + (StringUtils.isNotBlank(customWhere) ? " and " + customWhere : "");
            else
                query += customWhere + " ";
        }

        if (CollectionUtils.isNotEmpty(orderByValues)) {
            query += " order by ";
            for (String orderBy : orderByValues)
                query += orderBy + ",";
            if (query.endsWith(","))
                query = query.substring(0, query.length() - 1);
        }

        return query;

    }

//    protected void splitFromValues(String fromValuesString) {
//        st = new StringTokenizer(fromValuesString, ",");
//        while ((st.hasMoreTokens()))
//            fromValues.add(st.nextToken());
//    }

    protected void splitOrderByValues(String orderByValuesString) {
        st = new StringTokenizer(orderByValuesString,",");
        while ((st.hasMoreTokens()))
            orderByValues.add(st.nextToken());
    }

    public String getOlingoEntityAlias() {
        return olingoEntityAlias;
    }

    public String getOlingoEntityName() {
        return olingoEntityName;
    }

    public List<String> getSelectValues() {
        return selectValues;
    }

    public void setSelectValues(List<String> selectValues) {
        this.selectValues = selectValues;
    }

    public List<String> getFromValues() {
        return fromValues;
    }

    public void setFromValues(List<String> fromValues) {
        this.fromValues = fromValues;
    }

    public String getCustomWhere() {
        return customWhere;
    }

    public void setCustomWhere(String customWhere) {
        this.customWhere = customWhere;
    }


//    public OlingoQueryObject(String olingoJpqlQueryString) {
//        selectValues=new ArrayList<>();
//        fromValues=new ArrayList<>();
//        orderByValues=new ArrayList<>();
//        this.splitOlingoQueryString(olingoJpqlQueryString);
//    }
//
//
//    protected void splitOlingoQueryString(String olingoJpqlQueryString){
//        String[] split;
//        boolean wherePresent=olingoJpqlQueryString.contains(getDefaultOlingoWhere());
//        boolean orderByPresent=olingoJpqlQueryString.contains(getOlingoOrderBy());
//        boolean selectPresent=olingoJpqlQueryString.contains(getOlingoSelect());
//
//        //select
//        if(selectPresent) {
//            olingoJpqlQueryString = olingoJpqlQueryString.split(getOlingoSelect())[1];
//            split=olingoJpqlQueryString.split(getOlingoFrom());
//            st=new StringTokenizer(split[0]);
//            String select;
//            while(st.hasMoreTokens()){
//                select=st.nextToken().trim();
//                if(!select.equals(getOlingoEntityName()))
//                    selectValues.add(select);
//            }
//        }
//
//
//
//        //from
//        olingoJpqlQueryString=olingoJpqlQueryString.split(getOlingoFrom())[1];
//
//        if(wherePresent) {
//            split=olingoJpqlQueryString.split(getDefaultOlingoWhere());
//            // fromvalues
//            splitFromValues(split[0]);
//
//            olingoJpqlQueryString=split[1];
//            if(orderByPresent) {
//                split = olingoJpqlQueryString.split(getOlingoOrderBy());
//                olingoWhere=split[0];
//                splitOrderByValues(split[1]);
//            } else
//                olingoWhere=olingoJpqlQueryString;
//        } else {
//            if (orderByPresent){
//                split=olingoJpqlQueryString.split(getOlingoOrderBy());
//                splitFromValues(split[0]);
//                splitOrderByValues(split[1]);
//            } else
//                splitFromValues(olingoJpqlQueryString);
//        }
//    }
//

//
//
//    // ===========================================================================
//
//
//
//    protected static String getOlingoEntityName() {
//        return DEFAULT_OLINGO_ENTITY_NAME;
//    }
//
//    protected static String getOlingoFrom() {
//        return DEFAULT_OLINGO_FROM;
//    }
//
//    protected static String getOlingoOrderBy() {
//        return DEFAULT_OLINGO_ORDER_BY;
//    }
//
//    protected static String getOlingoSelect() {
//        return DEFAULT_OLINGO_SELECT;
//    }
//
//    public static String getDefaultOlingoWhere() {
//        return DEFAULT_OLINGO_WHERE;
//    }
}
