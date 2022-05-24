package com.nowcoder.community.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * @author syl
 * @date 2022年05月19日 20:38
 */
@Component
public class IpUtils {

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.indexOf(",") != -1) {
            ip = ip.substring(ip.lastIndexOf(",") + 1).trim();
        }
        return ip;
    }

    private static DatabaseReader reader;

    private static void init() {
        try {
            // 创建 GeoLite2 数据库 Reader
            // 这里可以放在本地磁盘，也可以随项目放在resource目录下
            File database = new File("E:\\java\\GeoLite2-City.mmdb");
            // 读取数据库内容
            reader = new DatabaseReader.Builder(database).build();
        } catch (Exception ex) {
        }
    }

    public static HashMap<String,String> getCityByIP(String ip) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        if (null == reader) {
            init();
        }
        InetAddress ipAddress = InetAddress.getByName(ip);
        // 获取查询结果
        CityResponse response = reader.city(ipAddress);

        // 获取国家信息
        Country state = response.getCountry();
        String country = JSON.toJSONString(state);
        JSONObject object1 = (JSONObject) JSONObject.parse(country);
        map.put("国家",(String)object1.getJSONObject("names").get("zh-CN"));

        // 获取省份
        Subdivision subdivision = response.getMostSpecificSubdivision();
        String province = JSON.toJSONString(subdivision);
        JSONObject object2 = (JSONObject) JSONObject.parse(province);
        map.put("省份",(String)object2.getJSONObject("names").get("zh-CN"));

        //城市
        City town = response.getCity();
        String city = JSON.toJSONString(town);
        JSONObject object3 = (JSONObject) JSONObject.parse(province);
        map.put("城市",(String)object3.getJSONObject("names").get("zh-CN"));

        return map;
    }
}
