package com.thinkbiganalytics.feedmgr.rest.controller;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.cluster.ClusterNodeSummary;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceTester;
import com.thinkbiganalytics.cluster.MessageDeliveryStatus;
import com.thinkbiganalytics.cluster.SimpleClusterMessageTest;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = "Kylo Cluster - Administration", produces = "application/json")
@Path("/v1/admin/cluster")
@SwaggerDefinition(tags = @Tag(name = "Kylo Cluster - Administration", description = "administrator cluster"))
public class ClusterServiceTestController {


    @Inject
    private ClusterServiceTester clusterServiceTester;

    @Inject
    private ClusterService clusterService;

    @GET
    @Path("/simple")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets thet latest message sent to this cluster.")
    public Response getSimpleMessage() {
        SimpleClusterMessageTest simpleClusterMessageTest = clusterServiceTester.getSimpleClusterMessage();
        if (simpleClusterMessageTest == null) {
            simpleClusterMessageTest = new SimpleClusterMessageTest("NULL", "No messages found", "");
        }
        return Response.ok(simpleClusterMessageTest).build();
    }


    @GET
    @Path("/is-clustered")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Is it clustered.")
    public Response isClustered() {

        if (clusterServiceTester.isClustered()) {
            return Response.ok(new RestResponseStatus.ResponseStatusBuilder().buildSuccess()).build();
        } else {
            return Response.ok(new RestResponseStatus.ResponseStatusBuilder().buildError()).build();
        }
    }

    @GET
    @Path("/members")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the members in the cluster.")
    public Response getMembers() {
        List<String> members = clusterServiceTester.getMembers();
        return Response.ok(members).build();
    }

    @POST
    @Path("/simple")
    @ApiOperation("Post a Simple message")
    public Response postSimpleMessage(@Nonnull final String simpleMessage) {

        clusterServiceTester.sendSimpleMessage(simpleMessage);
        return Response.ok(new RestResponseStatus.ResponseStatusBuilder().buildSuccess()).build();
    }


    @GET
    @Path("/awaiting-messages")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets messages awaiting acknowledgement.  This will only return data if the property: kylo.cluster.acknowledge=true  in the application.properties of kylo-services ")
    public List<MessageDeliveryStatus> getMessagesAwaitingAcknowledgement(@QueryParam("longerThan") Long longerThan) {
        if (longerThan != null && longerThan > 0L) {
            return clusterService.getMessagesAwaitingAcknowledgement(longerThan);
        } else {
            return clusterService.getMessagesAwaitingAcknowledgement();
        }

    }


    @GET
    @Path("/cluster-node-summary")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Returns a summary of this cluster node.")
    public ClusterNodeSummary getClusterNodeSummary() {
            return clusterService.getClusterNodeSummary();
    }

}
