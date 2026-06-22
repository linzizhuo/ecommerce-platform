package com.cloudmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM t_user WHERE phone = #{phone}")
    User selectByPhone(String phone);
}
