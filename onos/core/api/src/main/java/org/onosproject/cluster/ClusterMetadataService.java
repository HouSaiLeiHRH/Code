/*
 * Copyright 2015-present Open Networking Foundation
 *
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
 */
package org.onosproject.cluster;

import org.onosproject.event.ListenerService;

/**
 * Service for accessing {@link ClusterMetadata cluster metadata}.
 * 获取 ClusterMetadata的元数据的服务
 */
public interface ClusterMetadataService
    extends ListenerService<ClusterMetadataEvent, ClusterMetadataEventListener> {

    /**
     * Returns the current cluster metadata.
     * @return cluster metadata
     */
    ClusterMetadata getClusterMetadata();

    /**
     * Returns the {@link ControllerNode controller node} representing this instance.
     * @return local controller node
     */
    ControllerNode getLocalNode();
}
