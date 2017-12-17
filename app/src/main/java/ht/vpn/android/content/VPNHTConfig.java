package ht.vpn.android.content;

import android.content.SharedPreferences;

import java.util.ArrayList;

import ht.vpn.android.Preferences;
import ht.vpn.android.network.responses.Server;

public class VPNHTConfig {

    public static String generate(SharedPreferences preferences, Server server, Boolean firewall) {
        ArrayList<String> stringList = new ArrayList<>();

        //# 申明我们是一个client，配置从server端pull过来，如IP地址，路由信息之类“Server使用push指令push过来的”
        stringList.add("client");
        //#指定接口的类型，严格和Server端一致:dev tap或dev tun
        stringList.add("dev tun");

        //# 使用的协议，与Server严格一致
        if(firewall) {
            stringList.add("proto tcp");
        } else {
            stringList.add("proto udp");
        }

        //# 始终重新解析Server的IP地址（如果remote后面跟的是域名），
        //# 保证Server IP地址是动态的使用DDNS动态更新DNS后，Client在自动重新连接时重新解析Server的IP地址
        //# 这样无需人为重新启动，即可重新接入VPN
        stringList.add("resolv-retry infinite");

        stringList.add("nobind");
        stringList.add("key-direction 1");// Useful when using inline files
        stringList.add("reneg-sec 360000");// client和server只有一端设置为0，另一端不用设置
//        stringList.add("tun-mtu 1500");
//        stringList.add("tun-mtu-extra 32");
//        stringList.add("mssfix 1450");

        //# 和Server配置上的功能一样如果使用了chroot或者su功能，最好打开下面2个选项，防止重新启动后找不到keys文件，或者nobody用户没有权限启动tun设备
        stringList.add("persist-key");
//        if(preferences.getBoolean(Preferences.KILLSWITCH, true)) {
            stringList.add("persist-tun");
//        }

//        stringList.add("ping 15");
//        stringList.add("ping-restart 45");// 默认120s，不能与ping-exit同时使用
//        stringList.add("ping-timer-rem");// 配合ping-restart或ping-exit使用
        stringList.add("ns-cert-type server");
        stringList.add("mute 10");
        stringList.add("comp-lzo");
        stringList.add("verb 4");
//        stringList.add("pull");
//        stringList.add("fast-io");
//        stringList.add("auth-nocache");

        if(!preferences.getBoolean(Preferences.SMARTDNS, true)) {
            stringList.add("route-nopull");
            stringList.add("redirect-gateway def1");
            stringList.add("dhcp-option DNS 10.11.0.1");
        }

//        if(firewall) {
//            stringList.add(String.format("cipher %s", "AES-128-CBC"));
//        } else {
//            stringList.add(String.format("cipher %s", preferences.getString(Preferences.ENC_TYPE, "AES-128-CBC")));
//            stringList.add("explicit-exit-notify");
//        }


        // 连接确定的端口
//        stringList.add("remote 101.200.209.93 1194");
        //# 随机选择一个Server连接，否则按照顺序从上到下依次连接
        stringList.add("remote-random");

        // 服务器必须指定--auth-user-pass-verify
        stringList.add("<auth-user-pass>");
        stringList.add(preferences.getString(Preferences.USERNAME, ""));
        stringList.add(preferences.getString(Preferences.PASSWORD, ""));
        stringList.add("</auth-user-pass>");


        // 证书
        stringList.add("<ca>");
        stringList.add("-----BEGIN CERTIFICATE-----");
        stringList.add("MIIE0DCCA7igAwIBAgIJAPTHMrVYYmv3MA0GCSqGSIb3DQEBCwUAMIGgMQswCQYD");
        stringList.add("VQQGEwJDTjELMAkGA1UECBMCQkoxEDAOBgNVBAcTB0JFSUpJTkcxDjAMBgNVBAoT");
        stringList.add("BUNUU0lHMR0wGwYDVQQLExRNeU9yZ2FuaXphdGlvbmFsVW5pdDERMA8GA1UEAxMI");
        stringList.add("Q1RTSUcgQ0ExEDAOBgNVBCkTB0Vhc3lSU0ExHjAcBgkqhkiG9w0BCQEWD2FkbWlu");
        stringList.add("QGN0c2lnLmNvLDAeFw0xNjA0MjgwMjQ2MDlaFw0yNjA0MjYwMjQ2MDlaMIGgMQsw");
        stringList.add("CQYDVQQGEwJDTjELMAkGA1UECBMCQkoxEDAOBgNVBAcTB0JFSUpJTkcxDjAMBgNV");
        stringList.add("BAoTBUNUU0lHMR0wGwYDVQQLExRNeU9yZ2FuaXphdGlvbmFsVW5pdDERMA8GA1UE");
        stringList.add("AxMIQ1RTSUcgQ0ExEDAOBgNVBCkTB0Vhc3lSU0ExHjAcBgkqhkiG9w0BCQEWD2Fk");
        stringList.add("bWluQGN0c2lnLmNvLDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALGe");
        stringList.add("FQ7a6kq/RS8ZKEuYQBXorP/tDyVLQrAPE0DpeTKVJkTgaM/6tPHM0u7EpQm4f+ZR");
        stringList.add("HpUDwXwZGxyl0pnLHTUXRL20rbU2Roirb7SXNgrdMtws63ErMK8IGyGZ8GQRhsN1");
        stringList.add("B77BTcVTuAPo+2noyF9siv6/rpAuz3Q+JllZHzxMKgSM4QZwReB8X0z8ESJDitka");
        stringList.add("DddTSZGcE83dpt77ZxqFJZbZuRUuODLtwinqJKrb1lIE0E/LiVY2pSPZ7LQFBkMV");
        stringList.add("Q/khaaiBBtsIXjCTGv7TADzsSLhT3Nz4u9V59PkCOxTWP9aZnCnK2ZcJsIQazUip");
        stringList.add("eJCKiFS44981qS+5gjECAwEAAaOCAQkwggEFMB0GA1UdDgQWBBTjuuA/yuXC1MmT");
        stringList.add("e+mTgrNO+XHc9zCB1QYDVR0jBIHNMIHKgBTjuuA/yuXC1MmTe+mTgrNO+XHc96GB");
        stringList.add("pqSBozCBoDELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAkJKMRAwDgYDVQQHEwdCRUlK");
        stringList.add("SU5HMQ4wDAYDVQQKEwVDVFNJRzEdMBsGA1UECxMUTXlPcmdhbml6YXRpb25hbFVu");
        stringList.add("aXQxETAPBgNVBAMTCENUU0lHIENBMRAwDgYDVQQpEwdFYXN5UlNBMR4wHAYJKoZI");
        stringList.add("hvcNAQkBFg9hZG1pbkBjdHNpZy5jbyyCCQD0xzK1WGJr9zAMBgNVHRMEBTADAQH/");
        stringList.add("MA0GCSqGSIb3DQEBCwUAA4IBAQCFj2+IwSZpsbAE3axCvlLSpEpbkwAR9dUuJYvR");
        stringList.add("WX+tcnvZdOkyoOilBXsOrVSyUZqRs1gRufEgdnU0XdimLg4YHxYmNXDf14Y7lVQj");
        stringList.add("NjvGTS1wpGAUwJv7vj33voVvoJThH4YOMcirJVX8khjCCFoWRf6PClD4qp8zq4UN");
        stringList.add("xS92keDr+Qcn1seamAZAO7PqWIqbUzXaMtJa44Ayid9LCxHwDipBD5bErmt+OHqL");
        stringList.add("I/XR64ic4j+65Mi1UNGFz/ujcYVFCcWPTEat6ZzxkKCLFkF9wpTy+7aSKz9+IxV5");
        stringList.add("LEruzlaXS3aKMFgPkUMIFWtN3adcXr48BD7K2oKJnjJy+Cmt");




//        stringList.add("MIIEmzCCA4OgAwIBAgIJAIsPF0BTVr9FMA0GCSqGSIb3DQEBCwUAMIGPMQswCQYD");
//        stringList.add("VQQGEwJVUzELMAkGA1UECBMCREUxEzARBgNVBAcTCldpbG1pbmd0b24xDjAMBgNV");
//        stringList.add("BAoTBVZwbkhUMQ4wDAYDVQQLEwVWUE5IVDEPMA0GA1UEAxMGdnBuLmh0MQ4wDAYD");
//        stringList.add("VQQpEwVWUE5IVDEdMBsGCSqGSIb3DQEJARYOc3VwcG9ydEB2cG4uaHQwHhcNMTQx");
//        stringList.add("MTI4MTM1NDE5WhcNMjQxMTI1MTM1NDE5WjCBjzELMAkGA1UEBhMCVVMxCzAJBgNV");
//        stringList.add("BAgTAkRFMRMwEQYDVQQHEwpXaWxtaW5ndG9uMQ4wDAYDVQQKEwVWcG5IVDEOMAwG");
//        stringList.add("A1UECxMFVlBOSFQxDzANBgNVBAMTBnZwbi5odDEOMAwGA1UEKRMFVlBOSFQxHTAb");
//        stringList.add("BgkqhkiG9w0BCQEWDnN1cHBvcnRAdnBuLmh0MIIBIjANBgkqhkiG9w0BAQEFAAOC");
//        stringList.add("AQ8AMIIBCgKCAQEA3Vz35G5+cChwgyy2L96U6hVCkT2TVfXE4EA+UoVzSV2DQIYH");
//        stringList.add("4cdz+t+jmvyfPZCLdqRIZ3QgV+ftPFuCPlrESdccIOhe1KM5GDDv4LhsQC6jbAsH");
//        stringList.add("pmRrilIxLyZBVTfe2opAJ1A1e03CYjORgLBz7vx+krQ+cG6p7mR/aoKSJulgOhkR");
//        stringList.add("PffXKhnq7dGQkFR5tSG5xESsQVbRqP82xyR9eCOC8GyN6yKt85vCmA/e+6f3fGrJ");
//        stringList.add("uyXnmexxrV8GvRNbdScY/TcjaPsMwePOaOGfa97Svt/M7loTKgUI544p+nEH3QeK");
//        stringList.add("BxwiryBUOHmFetOBh+2nabn5982t4k+MVdy6kQIDAQABo4H3MIH0MB0GA1UdDgQW");
//        stringList.add("BBRvE5Y9ivf8/XYJosCQJeaOIhnYBTCBxAYDVR0jBIG8MIG5gBRvE5Y9ivf8/XYJ");
//        stringList.add("osCQJeaOIhnYBaGBlaSBkjCBjzELMAkGA1UEBhMCVVMxCzAJBgNVBAgTAkRFMRMw");
//        stringList.add("EQYDVQQHEwpXaWxtaW5ndG9uMQ4wDAYDVQQKEwVWcG5IVDEOMAwGA1UECxMFVlBO");
//        stringList.add("SFQxDzANBgNVBAMTBnZwbi5odDEOMAwGA1UEKRMFVlBOSFQxHTAbBgkqhkiG9w0B");
//        stringList.add("CQEWDnN1cHBvcnRAdnBuLmh0ggkAiw8XQFNWv0UwDAYDVR0TBAUwAwEB/zANBgkq");
//        stringList.add("hkiG9w0BAQsFAAOCAQEAOyV3OXQOyJk4U4kkLtvy/Kw0p2V3kaAwRZ9t8sQU1vm4");
//        stringList.add("g/5DIE3lbfCKT4vyb1ckzoV6bP6lG/9NhePJyGR6kub1M9KmwbdR68uTXH69S8/N");
//        stringList.add("ENdjI66gcPLmZGB7FrlMV7wQUy7X5g3cbLJ6spVKqM7lnYmxSqfwTG8qq546gdgk");
//        stringList.add("0OcROxPVtRDyKr+xQRg+WJSFa1ugcVz/x2FiYyTXFwgTS9RAXymTOiDIZcTlrmik");
//        stringList.add("32XQSJBk1cbUDCFsZo9LbuUB3Oe6Kv36wUJAXlsxgEtdgEcsr7BezqLcSPp6PyqC");
//        stringList.add("5GZ97ULagirc82d4BfDVp1GtUJlJMLJVMAmaoNn3Sw==");
        stringList.add("-----END CERTIFICATE-----");
        stringList.add("</ca>");

        // 预共享密钥
        stringList.add("<tls-auth>");
        stringList.add("-----BEGIN OpenVPN Static key V1-----");
        stringList.add("7b496ac820a48a40f0480a4d68d3919a");
        stringList.add("bdacde80b7ec642ef7d145e7201650cf");
        stringList.add("f33ac9a615b359c80383cac89fc0b86e");
        stringList.add("126a62a4ade2c82a863bd6fa2c387799");
        stringList.add("b839c261992b96a292fb06ed995d5cc8");
        stringList.add("9929e4a376b92a83ac4f9471689848e2");
        stringList.add("80d1b7b9afb01cf35e86f27a47deb561");
        stringList.add("fcbb960c1cc47e9fe8bdbb086f8267db");
        stringList.add("b3b0c41d230255b00e47a74d19cdbd39");
        stringList.add("befb2a84713caf64c013c3193f2899c4");
        stringList.add("510fb2d1c173af0e0e244d818bff3d17");
        stringList.add("524cacb3b7430b3cadcd4ebf78710a83");
        stringList.add("5447955d1dad5e2fc9ff1f082633afdf");
        stringList.add("87e9efd1e05a8d8edaed5a92558ec7bb");
        stringList.add("cb736284f6f5654c92a2f9de62793463");
        stringList.add("2b11a9a51669b8fdaf9dc9f7f28997b8");

//        stringList.add("74fa428696037279b617bb92efc1d2df");
//        stringList.add("edf3e030b0e24b848e1389490411e2b6");
//        stringList.add("ebbc521669285d17b9aeea190066502a");
//        stringList.add("c3ad09b0b272a81ed737760451fe6071");
//        stringList.add("a2003356a5f8e0f8f4555290f539bcfb");
//        stringList.add("371282cec7f6de53ffce1665f304f774");
//        stringList.add("6d4aaad012afa02a4faa9d4db325e104");
//        stringList.add("e1c957b056e1d6130daf4210531488e0");
//        stringList.add("978ba4ddaac3986e31c23f6589d21f62");
//        stringList.add("e36354931f0723771376c117b6ef3a17");
//        stringList.add("260e1f582475b8e1438147a82d716b37");
//        stringList.add("f8d451f0191586040950721bc5657657");
//        stringList.add("ecd7574731c06d390af2977c2eb15176");
//        stringList.add("b604121698394edf94e1ea091f008b83");
//        stringList.add("ad7921e7beba7b175956b9261d0cd686");
//        stringList.add("692b07de56806b72e46e5a7a69f9bb9a");
        stringList.add("-----END OpenVPN Static key V1-----");
        stringList.add("</tls-auth>");


        stringList.add("<cert>");
        stringList.add("-----BEGIN CERTIFICATE-----");
        stringList.add("MIIFFTCCA/2gAwIBAgIBAjANBgkqhkiG9w0BAQsFADCBoDELMAkGA1UEBhMCQ04x");
        stringList.add("CzAJBgNVBAgTAkJKMRAwDgYDVQQHEwdCRUlKSU5HMQ4wDAYDVQQKEwVDVFNJRzEd");
        stringList.add("MBsGA1UECxMUTXlPcmdhbml6YXRpb25hbFVuaXQxETAPBgNVBAMTCENUU0lHIENB");
        stringList.add("MRAwDgYDVQQpEwdFYXN5UlNBMR4wHAYJKoZIhvcNAQkBFg9hZG1pbkBjdHNpZy5j");
        stringList.add("bywwHhcNMTYwNDI4MDI0NjU4WhcNMjYwNDI2MDI0NjU4WjCBnzELMAkGA1UEBhMC");
        stringList.add("Q04xCzAJBgNVBAgTAkJKMRAwDgYDVQQHEwdCRUlKSU5HMQ4wDAYDVQQKEwVDVFNJ");
        stringList.add("RzEdMBsGA1UECxMUTXlPcmdhbml6YXRpb25hbFVuaXQxEDAOBgNVBAMTB2NsaWVu");
        stringList.add("dDExEDAOBgNVBCkTB0Vhc3lSU0ExHjAcBgkqhkiG9w0BCQEWD2FkbWluQGN0c2ln");
        stringList.add("LmNvLDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMYJDZnmyzsvk4Z9");
        stringList.add("DA2OCGMV7U4eVHKJu44MGxfrIu+ddagabGfaoSSooN8LjzFavW9MW+gvRW4adLd+");
        stringList.add("43Wn11Wk5jAE5nYxhCAwJ2GBCYxmos0UzQ1BRfzraR7+19QkDBOw1X8aVX71LlGJ");
        stringList.add("YbuzLEly1wgMEtaksli/ZuWYVxMXCnPm+B6vsjs7qW2xmllMwpDKnlVP7sY+EKXm");
        stringList.add("pDAE7i8xp8HxSXdpHg3Qx0tci/BAWxUYxUcqMCrueU6CNIvrRl/pTuaGe7MvzydW");
        stringList.add("G3JX54D1l3ysllxpfsQjO8ZtA3yinyLSuD+hw4Svn2WwZx5qjI5WsXO/G+X4/vgB");
        stringList.add("UQclRUUCAwEAAaOCAVcwggFTMAkGA1UdEwQCMAAwLQYJYIZIAYb4QgENBCAWHkVh");
        stringList.add("c3ktUlNBIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU29UuH65mqS7W");
        stringList.add("PjMks4My0Gy7l5gwgdUGA1UdIwSBzTCByoAU47rgP8rlwtTJk3vpk4KzTvlx3Peh");
        stringList.add("gaakgaMwgaAxCzAJBgNVBAYTAkNOMQswCQYDVQQIEwJCSjEQMA4GA1UEBxMHQkVJ");
        stringList.add("SklORzEOMAwGA1UEChMFQ1RTSUcxHTAbBgNVBAsTFE15T3JnYW5pemF0aW9uYWxV");
        stringList.add("bml0MREwDwYDVQQDEwhDVFNJRyBDQTEQMA4GA1UEKRMHRWFzeVJTQTEeMBwGCSqG");
        stringList.add("SIb3DQEJARYPYWRtaW5AY3RzaWcuY28sggkA9McytVhia/cwEwYDVR0lBAwwCgYI");
        stringList.add("KwYBBQUHAwIwCwYDVR0PBAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQCWrEE+9+D8");
        stringList.add("KDW/yVToCJ7w0Wp46ENxjT6EQEKI1X/1MsjMy0XXVXUK6J+4dzXJQd/EzHBoDrWU");
        stringList.add("/saznmTaitkphQ6F/yt0uo4xSDeK6n520fFxCnbdBDi2wNrsC38oqNbttHN+ejGT");
        stringList.add("08wkCHGO93M5XGU1v043B03DWfaBSCWm0F1r63+MggnUFMqhJ95FNkXJ3aShCoRz");
        stringList.add("I1p1P5xtsVmbc7yl8uLwC4LavrmMcKEAWmniDwHUxFhJ0fKc1tU9I75/lKoM+GeS");
        stringList.add("xUihHjmslbImV+mwD7YkP2uShebQ5zpNJifcjTMf98UDb5gKoMIaCzUVy/xZW+p1");
        stringList.add("6i0U2cf8er/+");
        stringList.add("-----END CERTIFICATE-----");
        stringList.add("</cert>");

        stringList.add("<key>");
        stringList.add("-----BEGIN PRIVATE KEY-----");
        stringList.add("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDGCQ2Z5ss7L5OG");
        stringList.add("fQwNjghjFe1OHlRyibuODBsX6yLvnXWoGmxn2qEkqKDfC48xWr1vTFvoL0VuGnS3");
        stringList.add("fuN1p9dVpOYwBOZ2MYQgMCdhgQmMZqLNFM0NQUX862ke/tfUJAwTsNV/GlV+9S5R");
        stringList.add("iWG7syxJctcIDBLWpLJYv2blmFcTFwpz5vger7I7O6ltsZpZTMKQyp5VT+7GPhCl");
        stringList.add("5qQwBO4vMafB8Ul3aR4N0MdLXIvwQFsVGMVHKjAq7nlOgjSL60Zf6U7mhnuzL88n");
        stringList.add("VhtyV+eA9Zd8rJZcaX7EIzvGbQN8op8i0rg/ocOEr59lsGceaoyOVrFzvxvl+P74");
        stringList.add("AVEHJUVFAgMBAAECggEAfFK8piyc06g3jku8oFnVbBcc5ljmHQ3YoAF7lNV6FkGc");
        stringList.add("1o7YFY7McU4nyD7ig1J68H9Yh3f9t3SGZ5gSRg+7gLnIBMdewz6suuRgJLc0fl8E");
        stringList.add("v4jafgLHZFOJp/rHFhgXONlf/q5SlCRqEqgTohmGqQmecx/ZR8UXbd2jRGoR6BoJ");
        stringList.add("J8IuT31JKiZSkDrtHsrGkAJZk3AlsZC+haHNIIdzOZ1d2NG6lvrFlbrSf0Fgg9i8");
        stringList.add("9v3T+ReByNNR8AcAokkpsT6f/ys2o5mvLx3JP127Gv0+gJJ8lS//WDfrRC78YcCG");
        stringList.add("02Ucc40yNXtfRty/2FH4v7eQF2q2cYcKe4GfWfEHdQKBgQDk7p7xui5LrNFuV30w");
        stringList.add("AlH2CWbII3E5Y+LiSX5PgcPXqUdqkC/cUasVn+s8UEandhJnqfwboUp/RBcQ4Wsp");
        stringList.add("2Ah9LwJoE5yM6NEj4hoRf8JSkMa0p4o2quNzjipdo28b4aFVcZV26EyED80vukKa");
        stringList.add("YIeaLwOuAT0QZs64kmySh7TjlwKBgQDdcz24d8JsvQwdlw6oKlE/fEC3B3R2sOvS");
        stringList.add("Y2Y3e+tVy9e2cfpATLQXsgiPXfHS5e2COc78Jgq7Eq0ayiGFqGUN1PEsSjEsBKOB");
        stringList.add("rrg86aY8VfhO3qkYjptzeZMgIPMJh9KS/x96C9D76qYDkEzJXm7Qr/MZdJGZ7Xm7");
        stringList.add("gF3wm5+JgwKBgAP/BldB4L0Bb0Z10IGeCMA6uciUlc1iqPs6PTI2Ga0SD2UUz0ER");
        stringList.add("S+M8v2Z2IEWcEgYtOXFiB2LhVWW+CZjZZIyK3Yfjj8AF8GCn+byEdmfqjw1oP0ll");
        stringList.add("AkNqH+MyjyX1clODgBBrTaqge/xxS+hV9wgB+hZ3fxFiOgmOxMZqCAZRAoGAckgP");
        stringList.add("mQwMxQsGnpqfEL/N8CJST2JEgyAogwmS7Z1AxKUOrHPDfr1Wz0esTThhHMJVwLfO");
        stringList.add("jqefMA2iUcnwZMaTaAD1eTLMj3fRXZJqN90oUcjX/PPBdg/aP0yFVbZLVibGz99j");
        stringList.add("Qhuo/OiKRhqI1f6q132FyiQzI6m/AblsQUg+m7sCgYEA2woGqiWDQHf2F5ZQTpQF");
        stringList.add("DJlZBnAZwN9VLwaM2IGnAXCuw8mDtctLmEmc6De2tBtJ+Q58jXxOXeD34l8hHuPS");
        stringList.add("Bhdyyn3WhtTF/s2hnIkHsMJ8jeZnXX4hT/plq91jTR7wh/YgdvfmzQrk3dE+6DZl");
        stringList.add("KlMx1Kn003VvTI8Oy1Xgz30=");
        stringList.add("-----END PRIVATE KEY-----");
        stringList.add("</key>");

        if(firewall) {
            stringList.add(String.format("remote %s %s", server.hostname, "1194"));
        } else {
            for (String port : getPortsByProtocol(preferences.getString(Preferences.ENC_TYPE, "AES-128-CBC"))) {
                stringList.add(String.format("remote %s %s", server.hostname, port));
            }
        }
        // Test server:
        // stringList.add(String.format("remote %s %s", "188.226.230.75", "1194"));

        StringBuilder builder = new StringBuilder();
        for(String s : stringList) {
            builder.append(s);
            builder.append('\n');
        }
        return builder.toString();
    }

    private static String[] getPortsByProtocol(String protocol) {
        ArrayList<String> stringList = new ArrayList<>();
        switch(protocol) {
            case "AES-256-CBC":
                for(int i = 1300; i < 1305; i++) {
                    stringList.add(Integer.toString(i));
                }
                break;
            case "AES-128-CBC":
                for(int i = 1194; i < 1201; i++) {
                    stringList.add(Integer.toString(i));
                }
                break;
            case "BF-CBC":
                for(int i = 1202; i < 1206; i++) {
                    stringList.add(Integer.toString(i));
                }
                break;
        }
        return stringList.toArray(new String[stringList.size()]);
    }

}
