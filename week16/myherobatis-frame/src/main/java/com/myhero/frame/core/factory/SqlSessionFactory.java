package com.myhero.frame.core.factory;

import com.myhero.frame.core.entity.Configuration;
import com.myhero.frame.openapi.SqlSession;
import com.myhero.frame.openapi.SqlSessionImpl;

/**
 * SqlSession工厂类，负责创建SqlSession接口实现类
 */
public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 创建SqlSession会话
     */
    public SqlSession openSession(){
        return new SqlSessionImpl(configuration);
    }
}
