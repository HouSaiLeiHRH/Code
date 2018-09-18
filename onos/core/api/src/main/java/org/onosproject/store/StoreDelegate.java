/*
 * Copyright 2014-present Open Networking Foundation
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
package org.onosproject.store;

import org.onosproject.event.Event;

/**
 * Entity associated with a store and capable of receiving notifications of
 * events within the store.
 * 与存储关联的实体，能够接收存储内的事件通知。
 */
public interface StoreDelegate<E extends Event> {

    /**
     * Notifies the delegate via the specified event.
     *
     * @param event store generated event
     */
    void notify(E event);

}
