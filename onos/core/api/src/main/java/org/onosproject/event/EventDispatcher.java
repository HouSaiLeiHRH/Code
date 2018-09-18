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
package org.onosproject.event;

/**
 * Abstraction of a mechanism capable of accepting and dispatching events to
 * appropriate event sinks. Where the event sinks are obtained is unspecified.
 * Similarly, whether the events are accepted and dispatched synchronously
 * or asynchronously is unspecified as well.
 * 抽象能够接受和派发事件到合适事件接收器的机制。 没有指定事件接收器的地址。类似地，事件是
 * 否被同步或异步接受和分派也没有被指定。
 */
public interface EventDispatcher {

    /**
     * Posts the specified event for dispatching.
     *
     * @param event event to be posted
     */
    void post(Event event);

}