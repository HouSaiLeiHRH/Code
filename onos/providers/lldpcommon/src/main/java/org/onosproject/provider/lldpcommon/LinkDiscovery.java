/*
 * Copyright 2016-present Open Networking Foundation
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
package org.onosproject.provider.lldpcommon;

import com.google.common.collect.Sets;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onlab.packet.ONOSLLDP;
import org.onlab.util.Timer;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link.Type;
import org.onosproject.net.LinkKey;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.link.ProbedLinkProvider;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.onosproject.net.PortNumber.portNumber;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Run discovery process from a physical switch. Ports are initially labeled as
 * slow ports. When an LLDP is successfully received, label the remote port as
 * fast. Every probeRate milliseconds, loop over all fast ports and send an
 * LLDP, send an LLDP for a single slow port. Based on FlowVisor topology
 * discovery implementation.
 * 从物理交换机运行发现过程。 端口最初标记为慢端口。 成功接收到LLDP时，请将远程端口标记为快速。
 * 每个probeRate毫秒，遍历所有快速端口并发送LLDP，为单个低速端口发送LLDP。
 * 基于FlowVisor拓扑发现实现。
 */
public class LinkDiscovery implements TimerTask {

    private final Logger log = getLogger(getClass());

    private final Device device;
    private final LinkDiscoveryContext context;

    private final Ethernet ethPacket;
    private final Ethernet bddpEth;

    private Timeout timeout;
    private volatile boolean isStopped;
    // Set of ports to be probed
    private final Set<Long> ports = Sets.newConcurrentHashSet();

    /**
     * Instantiates discovery manager for the given physical switch. Creates a
     * generic LLDP packet that will be customized for the port it is sent out on.
     * Starts the the timer for the discovery process.
     *
     * @param device  the physical switch
     * @param context discovery context
     */
    public LinkDiscovery(Device device, LinkDiscoveryContext context) {
        this.device = device;
        this.context = context;

        ethPacket = new Ethernet();
        ethPacket.setEtherType(Ethernet.TYPE_LLDP);
        ethPacket.setDestinationMACAddress(MacAddress.ONOS_LLDP);
        ethPacket.setPad(true);

        bddpEth = new Ethernet();
        bddpEth.setEtherType(Ethernet.TYPE_BSN);
        bddpEth.setDestinationMACAddress(MacAddress.BROADCAST);
        bddpEth.setPad(true);

        isStopped = true;
        start();
        log.debug("Started discovery manager for switch {}", device.id());

    }

    public synchronized void stop() {
        if (!isStopped) {
            isStopped = true;
            timeout.cancel();
        } else {
            log.warn("LinkDiscovery stopped multiple times?");
        }
    }

    public synchronized void start() {
        if (isStopped) {
            isStopped = false;
            timeout = Timer.newTimeout(this, 0, MILLISECONDS);
        } else {
            log.warn("LinkDiscovery started multiple times?");
        }
    }

    public synchronized boolean isStopped() {
        return isStopped || timeout.isCancelled();
    }

    /**
     * Add physical port to discovery process.
     * Send out initial LLDP and label it as slow port.
     * 根据发现处理添加物理端口,发送初始化的LLDP数据包并标记为慢端口
     *
     * @param port the port
     */
    public void addPort(Port port) {
        boolean newPort = ports.add(port.number().toLong());
        boolean isMaster = context.mastershipService().isLocalMaster(device.id());
        if (newPort && isMaster) {
            log.debug("Sending initial probe to port {}@{}", port.number().toLong(), device.id());
            sendProbes(port.number().toLong());
        }
    }

    /**
     * removed physical port from discovery process.
     * @param port the port number
     */
    public void removePort(PortNumber port) {
        ports.remove(port.toLong());
    }

    /**
     * Handles an incoming LLDP packet. Creates link in topology and adds the
     * link for staleness tracking.
     * 处理传入的LLDP数据包。 在拓扑结构中创建链接并添加链接以进行陈旧度跟踪。
     * @param packetContext packet context
     * @return true if handled
     */
    public boolean handleLldp(PacketContext packetContext) {
        Ethernet eth = packetContext.inPacket().parsed();
        if (eth == null) {
            return false;
        }

        ONOSLLDP onoslldp = ONOSLLDP.parseONOSLLDP(eth);
        if (onoslldp != null) {
            Type lt;
            //判断是否是集群自己的探测
            if (notMy(eth.getSourceMAC().toString())) {
                lt = Type.EDGE;
            } else {
                //链接类型
                lt = eth.getEtherType() == Ethernet.TYPE_LLDP ?
                        Type.DIRECT : Type.INDIRECT;
            }

            //从数据包中解析出源MAC端口
            PortNumber srcPort = portNumber(onoslldp.getPort());
            PortNumber dstPort = packetContext.inPacket().receivedFrom().port();

            String idString = onoslldp.getDeviceString();
            if (!isNullOrEmpty(idString)) {
                DeviceId srcDeviceId = DeviceId.deviceId(idString);
                DeviceId dstDeviceId = packetContext.inPacket().receivedFrom().deviceId();

                ConnectPoint src = new ConnectPoint(srcDeviceId, srcPort);
                ConnectPoint dst = new ConnectPoint(dstDeviceId, dstPort);

                LinkDescription ld = new DefaultLinkDescription(src, dst, lt);

                //输出收到的ONOS_LLDP数据包
/*                log.info("************************Link Discovery*****************************");
                log.info(onoslldp.toString());
                log.info("*****************************************************");*/
                try {
                    //调用LinkProviderService添加链接到core,context.providerService()
                    //返回的其实是LldpLinkProvider中的LinkProviderService
                    context.providerService().linkDetected(ld);
                    //将链接信息添加到LldpLinkProvider的linkTimes map中
                    context.touchLink(LinkKey.linkKey(src, dst));
                } catch (IllegalStateException e) {
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    // true if *NOT* this cluster's own probe.
    ////判断是否是集群自己的探测
    private boolean notMy(String mac) {
        // if we are using DEFAULT_MAC, clustering hadn't initialized, so conservative 'yes'
        String ourMac = context.fingerprint();
        if (ProbedLinkProvider.defaultMac().equalsIgnoreCase(ourMac)) {
            return true;
        }
        return !mac.equalsIgnoreCase(ourMac);
    }

    /**
     * Execute this method every t milliseconds. Loops over all ports
     * labeled as fast and sends out an LLDP. Send out an LLDP on a single slow
     * port.
     *
     * 每t毫秒执行一次该方法。 在所有标记为快速的端口上循环并发送LLDP。
     * 在单个慢速端口上发送LLDP。
     *
     * @param t timeout
     */
    @Override
    public void run(Timeout t) {
        if (isStopped()) {
            return;
        }

        if (context.mastershipService().isLocalMaster(device.id())) {
            log.trace("Sending probes from {}", device.id());
            ports.forEach(this::sendProbes);
        }

        if (!isStopped()) {
            timeout = t.timer().newTimeout(this, context.probeRate(), MILLISECONDS);
        }
    }

    /**
     * Creates packet_out LLDP for specified output port.
     * 为特定的端口创建LLDP packet_out数据包
     *
     * @param port the port
     * @return Packet_out message with LLDP data
     */
    private OutboundPacket createOutBoundLldp(Long port) {
        if (port == null) {
            return null;
        }
        ONOSLLDP lldp = getLinkProbe(port);
        ethPacket.setSourceMACAddress(context.fingerprint()).setPayload(lldp);
        return new DefaultOutboundPacket(device.id(),
                                         builder().setOutput(portNumber(port)).build(),
                                         ByteBuffer.wrap(ethPacket.serialize()));
    }

    /**
     * Creates packet_out BDDP for specified output port.
     *
     * @param port the port
     * @return Packet_out message with LLDP data
     */
    private OutboundPacket createOutBoundBddp(Long port) {
        if (port == null) {
            return null;
        }
        ONOSLLDP lldp = getLinkProbe(port);
        bddpEth.setSourceMACAddress(context.fingerprint()).setPayload(lldp);
        return new DefaultOutboundPacket(device.id(),
                                         builder().setOutput(portNumber(port)).build(),
                                         ByteBuffer.wrap(bddpEth.serialize()));
    }

    private ONOSLLDP getLinkProbe(Long port) {
        return ONOSLLDP.onosLLDP(device.id().toString(), device.chassisId(), port.intValue());
    }

    //向端口发送LLDP数据包进行探测
    private void sendProbes(Long portNumber) {
        if (context.packetService() == null) {
            return;
        }
        log.trace("Sending probes out of {}@{}", portNumber, device.id());
        OutboundPacket pkt = createOutBoundLldp(portNumber);
        context.packetService().emit(pkt);
        if (context.useBddp()) {
            OutboundPacket bpkt = createOutBoundBddp(portNumber);
            context.packetService().emit(bpkt);
        }
    }

    public boolean containsPort(long portNumber) {
        return ports.contains(portNumber);
    }
}
