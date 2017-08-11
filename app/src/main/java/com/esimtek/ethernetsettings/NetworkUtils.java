package com.esimtek.ethernetsettings;

import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * Created by tiansen on 2017/8/9 0009.
 */
    public class NetworkUtils {

        private static final String TAG = "NetworkUtils";






        /***
         * Convert a IPv4 address from an integer to an InetAddress.
         * @param hostAddress an int corresponding to the IPv4 address in network byte order
         */
        public static InetAddress intToInetAddress(int hostAddress) {
            byte[] addressBytes = { (byte)(0xff & hostAddress),
                    (byte)(0xff & (hostAddress >> 8)),
                    (byte)(0xff & (hostAddress >> 16)),
                    (byte)(0xff & (hostAddress >> 24)) };

            try {
                return InetAddress.getByAddress(addressBytes);
            } catch (UnknownHostException e) {
                throw new AssertionError();
            }
        }

        /***
         * Convert a IPv4 address from an InetAddress to an integer
         * @param inetAddr is an InetAddress corresponding to the IPv4 address
         * @return the IP address as an integer in network byte order
         */
        public static int inetAddressToInt(InetAddress inetAddr)
                throws IllegalArgumentException {
            byte [] addr = inetAddr.getAddress();
            if (addr.length != 4) {
                throw new IllegalArgumentException("Not an IPv4 address");
            }
            return ((addr[3] & 0xff) << 24) | ((addr[2] & 0xff) << 16) |
                    ((addr[1] & 0xff) << 8) | (addr[0] & 0xff);
        }

        /***
         * Convert a network prefix length to an IPv4 netmask integer
         * @param prefixLength
         * @return the IPv4 netmask as an integer in network byte order
         */
        public static int prefixLengthToNetmaskInt(int prefixLength)
                throws IllegalArgumentException {
            if (prefixLength < 0 || prefixLength > 32) {
                throw new IllegalArgumentException("Invalid prefix length (0 <= prefix <= 32)");
            }
            int value = 0xffffffff << (32 - prefixLength);
            return Integer.reverseBytes(value);
        }

        /***
         * Convert a IPv4 netmask integer to a prefix length
         * @param netmask as an integer in network byte order
         * @return the network prefix length
         */
        public static int netmaskIntToPrefixLength(int netmask) {
            return Integer.bitCount(netmask);
        }

        /***
         * Create an InetAddress from a string where the string must be a standard
         * representation of a V4 or V6 address.  Avoids doing a DNS lookup on failure
         * but it will throw an IllegalArgumentException in that case.
         * @param addrString
         * @return the InetAddress
         * @throws UnknownHostException
         * @hide
         */
        public static InetAddress numericToInetAddress(String addrString)
                throws  UnknownHostException {



            return InetAddress.getByName(addrString);
        }

        /***
         * Get InetAddress masked with prefixLength.  Will never return null.
         * @param address address which will be masked with specified prefixLength
         * @param prefixLength the prefixLength used to mask the IP
         */
        public static InetAddress getNetworkPart(InetAddress address, int prefixLength) {
            if (address == null) {
                throw new RuntimeException("getNetworkPart doesn't accept null address");
            }

            byte[] array = address.getAddress();

            if (prefixLength < 0 || prefixLength > array.length * 8) {
                throw new RuntimeException("getNetworkPart - bad prefixLength");
            }

            int offset = prefixLength / 8;
            int reminder = prefixLength % 8;
            byte mask = (byte)(0xFF << (8 - reminder));

            if (offset < array.length) array[offset] = (byte)(array[offset] & mask);

            offset++;

            for (; offset < array.length; offset++) {
                array[offset] = 0;
            }

            InetAddress netPart = null;
            try {
                netPart = InetAddress.getByAddress(array);
            } catch (UnknownHostException e) {
                throw new RuntimeException("getNetworkPart error - " + e.toString());
            }
            return netPart;
        }

        /***
         * Check if IP address type is consistent between two InetAddress.
         * @return true if both are the same type.  False otherwise.
         */
        public static boolean addressTypeMatches(InetAddress left, InetAddress right) {
            return (((left instanceof Inet4Address) && (right instanceof Inet4Address)) ||
                    ((left instanceof Inet6Address) && (right instanceof Inet6Address)));
        }

        /***
         * Convert a 32 char hex string into a Inet6Address.
         * throws a runtime exception if the string isn't 32 chars, isn't hex or can't be
         * made into an Inet6Address
         * @param addrHexString a 32 character hex string representing an IPv6 addr
         * @return addr an InetAddress representation for the string
         */
        public static InetAddress hexToInet6Address(String addrHexString)
                throws IllegalArgumentException {
            try {
                return numericToInetAddress(String.format("%s:%s:%s:%s:%s:%s:%s:%s",
                        addrHexString.substring(0,4),   addrHexString.substring(4,8),
                        addrHexString.substring(8,12),  addrHexString.substring(12,16),
                        addrHexString.substring(16,20), addrHexString.substring(20,24),
                        addrHexString.substring(24,28), addrHexString.substring(28,32)));
            } catch (Exception e) {
                Log.e("NetworkUtils", "error in hexToInet6Address(" + addrHexString + "): " + e);
                throw new IllegalArgumentException(e);
            }
        }

        /***
         * Create a string array of host addresses from a collection of InetAddresses
         * @param addrs a Collection of InetAddresses
         * @return an array of Strings containing their host addresses
         */
        public static String[] makeStrings(Collection<InetAddress> addrs) {
            String[] result = new String[addrs.size()];
            int i = 0;
            for (InetAddress addr : addrs) {
                result[i++] = addr.getHostAddress();
            }
            return result;
        }


        /**
         * PING remote host
         * @return
         */
        public static boolean ping(String ip,long time) {
            boolean isReach = false;
            try {
                String cmd = "ping -c 1 " + " -w " + time + " " + ip;
                Process p = Runtime.getRuntime().exec(cmd);
                int status = p.waitFor();
                if (status == 0) {
                    isReach = true;
                }
                Log.d(TAG, ">>>>>>>>>cmd:" + cmd + " result:" + status);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isReach;
        }
    }
