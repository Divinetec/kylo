package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.catalog.DataSet;

/*-
 * #%L
 * thinkbig-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.catalog.dataset.JcrDataSet;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasource;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDatasourceProvider;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public abstract class JcrFeedConnection extends JcrObject implements FeedConnection {

    public static final String DATASOURCE = "tba:datasource";  // Legacy Datasource support
    public static final String DATA_SET = "tba:dataSet";

    public JcrFeedConnection(Node node) {
        super(node);
    }

    public JcrFeedConnection(Node node, JcrDatasource datasource) {
        this(node);
        this.setProperty(DATASOURCE, datasource);
    }
    
    public JcrFeedConnection(Node node, JcrDataSet dataSet) {
        this(node);
        this.setProperty(DATA_SET, dataSet);
    }

    public Optional<Datasource> getDatasource() {
        if (JcrPropertyUtil.hasProperty(getNode(), DATASOURCE)) {
            return Optional.of(JcrUtil.getReferencedObject(getNode(), DATASOURCE, JcrDatasourceProvider.TYPE_RESOLVER));
        } else {
            return Optional.empty();
        }
    }
    
    public Optional<DataSet> getDataSet() {
        if (JcrPropertyUtil.hasProperty(getNode(), DATA_SET)) {
            return Optional.of(JcrUtil.getReferencedObject(getNode(), DATA_SET, JcrDataSet.class));
        } else {
            return Optional.empty();
        }
    }


    @Override
    public Feed getFeed() {
        try {
            //this.getParent == tba:details.
            //this.getParent.getParent == tba:summary
            //this.getParent.getParent.getParent == tba:feed
            return JcrUtil.createJcrObject(getNode().getParent().getParent().getParent(), JcrFeed.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access feed", e);
        }
    }
}
