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
package org.onosproject.incubator.net.tunnel;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

import com.google.common.annotations.Beta;
import org.onosproject.core.GroupId;
import org.onosproject.net.AbstractModel;
import org.onosproject.net.Annotations;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.Path;
import org.onosproject.net.provider.ProviderId;

/**
 * The default implementation of an network tunnel. supports for creating a
 * tunnel by connect point ,IP address, MAC address, device and so on.
 */
@Beta
public final class DefaultTunnel extends AbstractModel implements Tunnel {

    private final TunnelEndPoint src; // a source point of tunnel.
    private final TunnelEndPoint dst; // a destination point of tunnel.
    private final State state;
    private final Type type; // tunnel type
    private final GroupId groupId; // represent for a group flow table
    // which a tunnel match up
    // tunnel producer
    private final TunnelId tunnelId; // tunnel identify generated by
                                     // ONOS as primary key
    private final TunnelName tunnelName; // name of a tunnel
    private final Path path;
    private final NetworkResource networkRes; // network resource to carry label stack

    /**
     * Creates an active infrastructure tunnel using the supplied information.
     *
     * @param producerName provider identity
     * @param src tunnel source
     * @param dst tunnel destination
     * @param type tunnel type
     * @param groupId groupId
     * @param tunnelId tunnelId
     * @param tunnelName tunnel name
     * @param path the path of tunnel
     * @param annotations optional key/value annotations
     */
    public DefaultTunnel(ProviderId producerName, TunnelEndPoint src,
                         TunnelEndPoint dst, Type type, GroupId groupId,
                         TunnelId tunnelId, TunnelName tunnelName, Path path,
                         Annotations... annotations) {
        this(producerName, src, dst, type, Tunnel.State.ACTIVE, groupId,
             tunnelId, tunnelName, path, annotations);
    }

    /**
     * Creates an tunnel using the supplied information.
     *
     * @param producerName provider identity
     * @param src tunnel source
     * @param dst tunnel destination
     * @param type tunnel type
     * @param state tunnel state
     * @param groupId groupId
     * @param tunnelId tunnelId
     * @param tunnelName tunnel name
     * @param path the path of tunnel
     * @param annotations optional key/value annotations
     */
    public DefaultTunnel(ProviderId producerName, TunnelEndPoint src,
                         TunnelEndPoint dst, Type type, State state,
                         GroupId groupId, TunnelId tunnelId,
                         TunnelName tunnelName, Path path, Annotations... annotations) {
        super(producerName, annotations);
        this.src = src;
        this.dst = dst;
        this.type = type;
        this.state = state;
        this.groupId = groupId;
        this.tunnelId = tunnelId;
        this.tunnelName = tunnelName;
        this.path = path;
        this.networkRes = null;
    }

    /**
     * Creates an active infrastructure tunnel using the supplied information.
     *
     * @param producerName provider identity
     * @param src tunnel source
     * @param dst tunnel destination
     * @param type tunnel type
     * @param groupId groupId
     * @param tunnelId tunnelId
     * @param tunnelName tunnel name
     * @param path the path of tunnel
     * @param networkRes network resource of tunnel
     * @param annotations optional key/value annotations
     */
    public DefaultTunnel(ProviderId producerName, TunnelEndPoint src,
                         TunnelEndPoint dst, Type type, GroupId groupId,
                         TunnelId tunnelId, TunnelName tunnelName, Path path,
                         NetworkResource networkRes, Annotations... annotations) {
        this(producerName, src, dst, type, Tunnel.State.ACTIVE, groupId,
                tunnelId, tunnelName, path, networkRes, annotations);
    }

    /**
     * Creates an tunnel using the supplied information.
     *
     * @param producerName provider identity
     * @param src tunnel source
     * @param dst tunnel destination
     * @param type tunnel type
     * @param state tunnel state
     * @param groupId groupId
     * @param tunnelId tunnelId
     * @param tunnelName tunnel name
     * @param path the path of tunnel
     * @param networkRes network resource of tunnel
     * @param annotations optional key/value annotations
     */
    public DefaultTunnel(ProviderId producerName, TunnelEndPoint src,
                         TunnelEndPoint dst, Type type, State state,
                         GroupId groupId, TunnelId tunnelId,
                         TunnelName tunnelName, Path path, NetworkResource networkRes,
                         Annotations... annotations) {
        super(producerName, annotations);
        this.src = src;
        this.dst = dst;
        this.type = type;
        this.state = state;
        this.groupId = groupId;
        this.tunnelId = tunnelId;
        this.tunnelName = tunnelName;
        this.path = path;
        this.networkRes = networkRes;
    }

    @Override
    public TunnelEndPoint src() {
        return src;
    }

    @Override
    public TunnelEndPoint dst() {
        return dst;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public NetworkResource resource() {
        return networkRes;
    }

    @Override
    public TunnelId tunnelId() {
        return tunnelId;
    }

    @Override
    public GroupId groupId() {
        return groupId;
    }

    @Override
    public TunnelName tunnelName() {
        return tunnelName;
    }


    @Override
    public Path path() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst, type, groupId, tunnelId, tunnelName,
                            state, path, networkRes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultTunnel) {
            final DefaultTunnel other = (DefaultTunnel) obj;
            return Objects.equals(this.src, other.src)
                    && Objects.equals(this.dst, other.dst)
                    && Objects.equals(this.type, other.type)
                    && Objects.equals(this.groupId, other.groupId)
                    && Objects.equals(this.tunnelId, other.tunnelId)
                    && Objects.equals(this.tunnelName, other.tunnelName)
                    && Objects.equals(this.state, other.state)
                    && Objects.equals(this.path, other.path)
                    && Objects.equals(this.networkRes, other.networkRes);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("src", src).add("dst", dst)
                .add("type", type).add("state", state).add("groupId", groupId)
                .add("producerTunnelId", tunnelId)
                .add("tunnelName", tunnelName)
                .add("path", path)
                .add("networkResource", networkRes).toString();
    }
}
