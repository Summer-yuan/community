package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author syl
 * @date 2022年05月23日 19:51
 */
@Service
public class IpService {

    @Autowired
    private UserMapper userMapper;


    public void saveIp(int user_id, String ip, String address) {
        userMapper.insertIp(user_id,ip,address);
    }

    public String getAddress(int user_id){
        return userMapper.getAddressById(user_id);
    }
}
