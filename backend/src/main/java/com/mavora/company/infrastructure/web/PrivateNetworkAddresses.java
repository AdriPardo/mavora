package com.mavora.company.infrastructure.web;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public final class PrivateNetworkAddresses {

    private PrivateNetworkAddresses() {
    }

    public static boolean isBlocked(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
        if (address instanceof Inet4Address ipv4) {
            byte[] bytes = ipv4.getAddress();
            int b0 = bytes[0] & 0xff;
            int b1 = bytes[1] & 0xff;
            if (b0 == 0 || b0 == 127 || b0 == 10 || b0 == 169 && b1 == 254) {
                return true;
            }
            if (b0 == 172 && b1 >= 16 && b1 <= 31) {
                return true;
            }
            if (b0 == 192 && b1 == 168) {
                return true;
            }
            if (b0 == 100 && b1 >= 64 && b1 <= 127) {
                return true; // CGNAT
            }
        }
        if (address instanceof Inet6Address ipv6) {
            byte[] bytes = ipv6.getAddress();
            if ((bytes[0] & 0xfe) == 0xfc) {
                return true; // unique local
            }
            InetAddress ipv4 = ipv6.getAddress().length == 16 && isIpv4Mapped(bytes)
                    ? ipv4FromMapped(bytes)
                    : null;
            if (ipv4 != null) {
                return isBlocked(ipv4);
            }
        }
        return false;
    }

    private static boolean isIpv4Mapped(byte[] bytes) {
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
    }

    private static InetAddress ipv4FromMapped(byte[] bytes) {
        try {
            return InetAddress.getByAddress(new byte[] {bytes[12], bytes[13], bytes[14], bytes[15]});
        } catch (java.net.UnknownHostException exception) {
            return null;
        }
    }
}
