package com.thinkbiganalytics.metadata.modeshape.support;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import org.modeshape.jcr.api.JcrTools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * Created by sr186054 on 6/13/16.
 */
public class JcrQueryUtil {


    public static <T extends Object> List<T> find(Session session, String query, Class<T> type) {
        return find(session, query, null, type);
    }

    public static <T extends Object> List<T> find(Session session, String query, Map<String, String> bindParams, Class<T> type) {
        JcrTools tools = new JcrTools();
        try {
            QueryResult result = query(session, query, bindParams);
            return queryResultToList(result, type);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findAll for query : " + query, e);
        }
    }

    public static <T extends Object> List<T> queryResultToList(QueryResult result, Class<T> type) {
        return queryResultToList(result, type, null);
    }

    public static <T extends Object> List<T> queryResultToList(QueryResult result, Class<T> type, Integer fetchSize) {
        List<T> entities = new ArrayList<>();

        if (result != null) {
            try {
                NodeIterator nodeIterator = result.getNodes();
                int cntr = 0;
                while (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    T entity = JcrUtil.constructNodeObject(node, type, null);
                    entities.add(entity);
                    cntr++;
                    if (fetchSize != null && cntr == fetchSize) {
                        break;
                    }

                }
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Unable to parse QueryResult to List for type  : " + type, e);

            }
        }
        return entities;
    }

    public static <T extends Object> T findFirst(Session session, String query, Class<T> type) {
        return findFirst(session, query, null, type);
    }

    public static <T extends Object> T findFirst(Session session, String query, Map<String, String> bindParams, Class<T> type) {

        JcrTools tools = new JcrTools();
        try {
            QueryResult result = query(session, query, bindParams);
            List<T> list = queryResultToList(result, type, 1);
            if (list != null && list.size() > 0) {
                return list.get(0);
            } else {
                return null;
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to findFirst for query : " + query, e);
        }
    }


    public static QueryResult query(Session session, String queryExpression) throws RepositoryException {
        return query(session, queryExpression, null);
    }

    public static QueryResult query(Session session, String queryExpression, Map<String, String> bindParams) throws RepositoryException {

        QueryResult results = null;

        Query query = session.getWorkspace().getQueryManager().createQuery(queryExpression, "JCR-SQL2");
        if (bindParams != null && !bindParams.isEmpty()) {
            Iterator e = bindParams.entrySet().iterator();

            while (e.hasNext()) {
                Map.Entry entry = (Map.Entry) e.next();
                String key = (String) entry.getKey();
                Value value = session.getValueFactory().createValue((String) entry.getValue());
                query.bindValue(key, value);
            }
        }

        results = query.execute();

        return results;
    }

}
