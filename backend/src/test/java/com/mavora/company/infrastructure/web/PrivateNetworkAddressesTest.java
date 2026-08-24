package com.mavora.company.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import org.junit.jupiter.api.Test;

class PrivateNetworkAddressesTest {

    @Test
    void blocksLoopbackPrivateLinkLocalAndMetadata() throws Exception {
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("127.0.0.1"))).isTrue();
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("10.1.2.3"))).isTrue();
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("192.168.1.10"))).isTrue();
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("172.16.0.4"))).isTrue();
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("169.254.169.254"))).isTrue();
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("0.0.0.0"))).isTrue();
        assertThat(PrivateNetworkAddresses.isBlocked(InetAddress.getByName("8.8.8.8"))).isFalse();
    }
}
