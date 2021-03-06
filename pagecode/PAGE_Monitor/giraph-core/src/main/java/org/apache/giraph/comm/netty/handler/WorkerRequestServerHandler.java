/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.comm.netty.handler;

import org.apache.giraph.bsp.*;
import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.comm.ServerData;
import org.apache.giraph.comm.requests.SendWorkerMessagesRequest;
import org.apache.giraph.comm.requests.WorkerRequest;
import org.apache.giraph.graph.TaskInfo;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

/**
 * Handler for requests on worker
 *
 * @param <I> Vertex id
 * @param <V> Vertex data
 * @param <E> Edge data
 * @param <M> Message data
 */
public class WorkerRequestServerHandler<I extends WritableComparable,
    V extends Writable, E extends Writable, M extends Writable> extends
    RequestServerHandler<WorkerRequest<I, V, E, M>> {
  /** Data that can be accessed for handling requests */
  private final ServerData<I, V, E, M> serverData;
  private final CentralizedServiceWorker<I, V, E, M> service;

  /**
   * Constructor with external server data
   *
   * @param serverData               Data held by the server
   * @param workerRequestReservedMap Worker request reservation map
   * @param conf                     Configuration
   * @param myTaskInfo               Current task info
   */
  public WorkerRequestServerHandler(ServerData<I, V, E, M> serverData,
		  CentralizedServiceWorker<I, V, E, M> service,
		  WorkerRequestReservedMap workerRequestReservedMap,
      ImmutableClassesGiraphConfiguration conf,
      TaskInfo myTaskInfo) {
    super(workerRequestReservedMap, conf, myTaskInfo);
    this.serverData = serverData;
    this.service = service;
  }

  @Override
  public void processRequest(WorkerRequest<I, V, E, M> request) {
	long start_time = System.currentTimeMillis();
    request.doRequest(serverData);
    if (request instanceof SendWorkerMessagesRequest) {
    	long end_time = System.currentTimeMillis();
    	service.addRemoteMessage(end_time - start_time);
    	service.setRemoteFinishTime(end_time);
    	//assume the start time is the same as the start time for the superstep.
//    	if(service.getRemoteStartTime() > start_time){
//    		service.setRemoteStartTime(start_time);
//    	}
    }
  }

  /** Factory for {@link WorkerRequestServerHandler} */
  public static class Factory<I extends WritableComparable,
      V extends Writable, E extends Writable, M extends Writable> implements
      RequestServerHandler.Factory {
    /** Data that can be accessed for handling requests */
    private final ServerData<I, V, E, M> serverData;
    private final CentralizedServiceWorker<I, V, E, M> service;

    /**
     * Constructor
     *
     * @param serverData Data held by the server
     */
    public Factory(ServerData<I, V, E, M> serverData,
    		CentralizedServiceWorker<I, V, E, M> service) {
      this.serverData = serverData;
      this.service = service;
    }

    @Override
    public RequestServerHandler newHandler(
        WorkerRequestReservedMap workerRequestReservedMap,
        ImmutableClassesGiraphConfiguration conf,
        TaskInfo myTaskInfo) {
      return new WorkerRequestServerHandler<I, V, E,
          M>(serverData, service, workerRequestReservedMap, conf, myTaskInfo);
    }
  }
}
