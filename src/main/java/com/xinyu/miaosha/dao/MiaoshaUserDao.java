package com.xinyu.miaosha.dao;

import com.xinyu.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MiaoshaUserDao {

    @Select("select * from miaosha_user where id = #{id}")
    MiaoshaUser getById(@Param("id") long id);

    @Insert("insert into miaosha_user(id, login_count, nickname, register_date, salt, password)values(" +
            "#{id}, #{loginCount}, #{nickname}, #{registerDate}, #{salt}, #{password})")
    int insertMiaoshaUser(MiaoshaUser user);
}
