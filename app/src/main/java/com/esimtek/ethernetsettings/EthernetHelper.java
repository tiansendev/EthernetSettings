package com.esimtek.ethernetsettings;

import android.content.Context;
import android.net.LinkAddress;
import android.net.ProxyInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by tiansen on 2017/8/8 0008.
 */

public class EthernetHelper {

    private static final String TAG = "EthernetHelper";

    private  InetAddress inetAddress;
    private  Field mService;
    private  Object mServiceObject;
    private  Class<?> iEthernetManagerClass;

    // STATIC DCHP UNASSIGNED
    private  Object mIpAssignment;

    //     Describe static Ip configuration
    private  Object mStaticIpConfiguration;
    private  Context mContext;
    private  Object ethernetManager;
    private  Object ipConfiguration;
    private  Class<?> ipConfigurationClass;
    private  Class<?> ethernetManagerClass;

    private EthernetHelper() {
    }


    public static EthernetHelper mEthernetHelper;

    public static EthernetHelper getInstance() {
        if (mEthernetHelper == null) {
            return new EthernetHelper();
        }
        return mEthernetHelper;
    }


    public void init(Context context) {
        mContext = context;

        try {

            //######################### Instances of EthernetManager and IpConfiguration ################################

            String ETHERNET_SERVICE = (String) Context.class.getField("ETHERNET_SERVICE").get(null);

            ethernetManagerClass = Class.forName("android.net.EthernetManager");

            ipConfigurationClass = Class.forName("android.net.IpConfiguration");

            //获取ethernetManager服务对象
            ethernetManager = context.getSystemService(ETHERNET_SERVICE);

            ipConfiguration = ethernetManagerClass.getDeclaredMethod("getConfiguration").invoke(ethernetManager);




            //######################### Instances of IpAssignment and StaticIpConfiguration ################################

            Field[] filds = ipConfiguration.getClass().getDeclaredFields();

            // 获取IpConfiguration
            for (Field f : filds) {
                if (f.getName().equals("ipAssignment")) { // ip assignment

                    Log.d(TAG, "ipAssignment--->" + f.get(ipConfiguration));
                    mIpAssignment = f.get(ipConfiguration);
                }

                if (f.getName().equals("staticIpConfiguration")) {
                    // static ip config

                    mStaticIpConfiguration = f.get(ipConfiguration);
                }
            }


            //################################# IEthernetManager ######################################


            //获取在EthernetManager中的抽象类mService成员变量
            mService = ethernetManagerClass.getDeclaredField("mService");

            //修改private权限
            mService.setAccessible(true);

            //获取抽象类的实例化对象
            mServiceObject = mService.get(ethernetManager);


            iEthernetManagerClass = Class.forName("android.net.IEthernetManager");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * ip assignment
     *
     * @return
     */
    public String getIpAssignment() {
        return mIpAssignment.toString();
    }

    /**
     * ip configuration
     *
     * @return
     */
    public String getStaticIpConfiguration() {
        if (mStaticIpConfiguration == null) return "No static IP configuration!";
        return mStaticIpConfiguration.toString();
    }


    public void setStaticEnable(boolean isEnable) {
        try {
            Method[] methods = iEthernetManagerClass.getDeclaredMethods();

            for (Method ms : methods) {

                Log.d(TAG, "methods--->" + ms.toString());

                if (ms.getName().equals("setEthernetEnabled")) {

                    ms.invoke(mServiceObject, isEnable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public String setStaticConfig(boolean hasIpSettings, String ip, String prefixLength, String gateway, String... dns) {



        //获取LinkAddress里面只有一个String类型的构造方法
        Constructor<?> linkAddressConstructor = null;
        try {

            // 获取静态ip配置
            Class<?> staticIpConfig = Class.forName("android.net.StaticIpConfiguration");


            Constructor<?> staticIpConfigConstructor = staticIpConfig.getDeclaredConstructor(staticIpConfig);

            Object staticIpConfigInstance = staticIpConfig.newInstance();

            //获取staticIpConfig中所有的成员变量
            Field[] declaredFields = staticIpConfigInstance.getClass().getDeclaredFields();

            Log.d(TAG, Arrays.asList(declaredFields).toString());


            //存放ipASSignment枚举类参数的集合
            HashMap ipAssignmentMap = new HashMap();

            //存放proxySettings枚举类参数的集合
            HashMap proxySettingsMap=new HashMap();

            Class<?>[] enumClass = ipConfigurationClass.getDeclaredClasses();

            for (Class enumC : enumClass) {

                //获取枚举数组
                Object[] enumConstants = enumC.getEnumConstants();

                        if (enumC.getSimpleName().equals("ProxySettings")) {

                            for (Object enu : enumConstants) {

                                //设置代理设置集合 STATIC DHCP UNASSIGNED PAC
                                proxySettingsMap.put(enu.toString(), enu);

                            }

                        } else if (enumC.getSimpleName().equals("IpAssignment")) {

                    for (Object enu : enumConstants) {

                        //设置以太网连接模式设置集合 STATIC DHCP UNASSIGNED
                        ipAssignmentMap.put(enu.toString(), enu);

                    }

                }

            }

            Method setIpAssignment = ipConfigurationClass.getDeclaredMethod("setIpAssignment", mIpAssignment.getClass());
            setIpAssignment.setAccessible(true);
            setIpAssignment.invoke(ipConfiguration, hasIpSettings ? ipAssignmentMap.get("STATIC") : ipAssignmentMap.get("DCHP"));



            if (hasIpSettings) {
                for (Field f : declaredFields) {
                    Log.d(TAG, "zhixingle.............." + f.getName());
                    //设置成员变量的值
                    if (f.getName().equals("ipAddress")) {
//
//                        //设置IP地址和子网掩码
//                        Inet4Address inetAddr = null;
//                        try {
//                            inetAddr = (Inet4Address) NetworkUtils.numericToInetAddress(ip);
//                        } catch (IllegalArgumentException | ClassCastException e) {
//                            // exception
////                            return "请填写正确的ip地址！";
//                            e.printStackTrace();
//                        }
//
//                        int networkPrefixLength = -1;
//                        try {
//                            networkPrefixLength = Integer.parseInt(prefixLength);
//                            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
//                                // exception
//                                return "前缀长度范围为0-32";
//                            }
//
//
//
//                        } catch (NumberFormatException e) {
//                        }

                        Log.d(TAG, "ip####:" + ip);
                        //实例化 构造方法
                        linkAddressConstructor = LinkAddress.class.getDeclaredConstructor(String.class);
                        LinkAddress linkAddress = (LinkAddress) linkAddressConstructor.newInstance("192.168.1.1/24");//192.168.1.1/24--子网掩码长度,24相当于255.255.255.0
                        f.set(staticIpConfigInstance, linkAddress);


                    } else if (f.getName().equals("gateway")) {

                        //设置默认网关

                        if (!TextUtils.isEmpty(gateway)) {
                            try {
                                f.set(staticIpConfigInstance, (Inet4Address) NetworkUtils.numericToInetAddress(gateway));
                            } catch (IllegalArgumentException | ClassCastException e) {
//                                return "请填写正确的网关地址！";
                                e.printStackTrace();
                            }
                        }

                    } else if (f.getName().equals("dnsServers")) {

                        //设置DNS
//                        ArrayList<InetAddress> dnsServers = new ArrayList<>();

                        ArrayList dnsServers = (ArrayList) f.get(staticIpConfigInstance);

                        String dns1 = dns[0];
                        if (!TextUtils.isEmpty(dns1)) {
                            try {
                                dnsServers.add(NetworkUtils.numericToInetAddress(dns1));
                            } catch (IllegalArgumentException | ClassCastException e) {
//                                return "请填写正确的DNS地址！";
                                e.printStackTrace();
                            }
                        }

                        String dns2 = dns[1];
                        if (!TextUtils.isEmpty(dns2)) {
                            try {
                                dnsServers.add(NetworkUtils.numericToInetAddress(dns2));
                            } catch (IllegalArgumentException | ClassCastException e) {
//                                return "请填写正确的DNS地址！";
                                e.printStackTrace();
                            }
                        }




                    }
                }

                // 获取ipConfiguration类中带有StaticIpConfiguration参数类型的名叫setStaticIpConfiguration的方法
                Method setStaticIpConfiguration = ipConfigurationClass.getDeclaredMethod("setStaticIpConfiguration", staticIpConfig);
                setStaticIpConfiguration.setAccessible(true);
                //在ipConfiguration对象中使用setStaticIpConfiguration方法,并传入参数
                setStaticIpConfiguration.invoke(ipConfiguration, staticIpConfigInstance);
                Method setConfig = ethernetManagerClass.getDeclaredMethod("setConfiguration", ipConfigurationClass);

                Log.d(TAG, "After Static settings-->" + ipConfiguration); // ok

                // TODO: 2017/8/9 0009 Unknown problem......
                setConfig.invoke(ethernetManager, ipConfiguration);

                return "Static Ip setting ok!";


//                        f.set(staticIpConfigInstance, dnsServers);

                // set configuration
                // 获取ipConfiguration类中带有StaticIpConfiguration参数类型的名叫setStaticIpConfiguration的方法
//                Method setStaticIpConfiguration = ipConfigurationClass.getDeclaredMethod("setStaticIpConfiguration", staticIpConfig);
//
//
//                setStaticIpConfiguration.setAccessible(true);
//
//
//                //在ipConfiguration对象中使用setStaticIpConfiguration方法,并传入参数
//                setStaticIpConfiguration.invoke(ipConfiguration, staticIpConfigInstance);
//
//
//
//
//
//
//
//
//                Object ipConfigurationInstance = null;
//
//                //获取ipConfiguration类的构造方法
//                Constructor<?>[] ipConfigConstructors = ipConfigurationClass.getDeclaredConstructors();
//
//                for (Constructor constru : ipConfigConstructors) {
//
//                    //获取ipConfiguration类的4个参数的构造方法
//                    if (constru.getParameterTypes().length==4){//设置以上四种类型
//
//                        //初始化ipConfiguration对象,设置参数
//                        ipConfigurationInstance = constru.newInstance(ipAssignmentMap.get("STATIC"), proxySettingsMap.get("NONE"), staticIpConfigInstance, null);
//
//                    }
//
//                }

//
//
//                Method setConfig = ethernetManagerClass.getDeclaredMethod("setConfiguration", ipConfigurationClass);
//                Log.d(TAG, "After static Ip settings-->" + staticIpConfigInstance);
//
//
//                setStaticEnable(true);
//
//                Object ethernetManagerInstance = ethernetManagerClass.getDeclaredConstructor(Context.class, iEthernetManagerClass).newInstance(mContext, mServiceObject);
//
//
////                setConfig.invoke(ethernetManagerInstance, ipConfigurationInstance);
//                ethernetManagerClass.getDeclaredMethod("setConfiguration",ipConfigurationClass).invoke(ethernetManagerInstance,ipConfigurationInstance);
//
////                ethernetManagerClass.getDeclaredMethod("setConfiguration",ipConfigurationClass).invoke(ethernetManagerInstance,ipConfigurationInstance);

            } else {
                // 获取ipConfiguration类中带有StaticIpConfiguration参数类型的名叫setStaticIpConfiguration的方法
                Method setStaticIpConfiguration = ipConfigurationClass.getDeclaredMethod("setStaticIpConfiguration", staticIpConfig);
                setStaticIpConfiguration.setAccessible(true);
                //在ipConfiguration对象中使用setStaticIpConfiguration方法,并传入参数
                setStaticIpConfiguration.invoke(ipConfiguration, null);
                Method setConfig = ethernetManagerClass.getDeclaredMethod("setConfiguration", ipConfigurationClass);
                Log.d(TAG, "After DCHP settings-->" + ipConfiguration);

                setConfig.invoke(ethernetManager, ipConfiguration);
                return "DHCP setting ok!";
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        return "";
    }

//
//    public String getStaticIp() {
//        return "";
//    }
//
//    public void setStaticIp(String ip) {
//
//    }
//
//    public String getStaticMask() {
//        return "";
//    }
//
//    public void setStaticMask() {
//
//    }
//
//    public String getStaticGateWay() {
//        return "";
//    }
//
//    public void setStaticGateWay() {
//    }
//
//    public String getStaticDns() {
//        return "";
//    }
//
//    public void setStaticDns() {
//    }
//
//    public void setStaticEtherConfig() {
//
//    }


}
