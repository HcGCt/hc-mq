package com.hc.mq.dashboard.dao;

import com.hc.mq.dashboard.entity.Registry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author hc
 */
@Mapper
public interface IRegistryDao {

    List<Registry> list();

    Registry selectById(@Param("id") int id);

    Registry selectByHash(@Param("hashCode") int hashCode);

    int insert(@Param("registry") Registry registry);

    int update(@Param("registry") Registry registry);

    int delete(@Param("id") int id);

    List<Registry> selectByKey(@Param("key") String key);


    int deleteByHash(@Param("hashCode") int hashCode);

    int deleteByInfo(@Param("key") String key, @Param("info") String info);

    int deleteByKey(@Param("key") String key);
}
