package com.myhero.frame.openapi;

import com.myhero.frame.core.Executor;
import com.myhero.frame.core.entity.Configuration;

import java.util.List;

/***
 * <p>Description: </p>
 *
 *
 * @return
 * @author chenhan
 * @date 2022/11/27 22:00
 * @version 1.0.0
 *
 */
public class SqlSessionImpl implements SqlSession{

    //每次Sql会话连接，必须要有数据库配置信息
    private Configuration configuration;

    public SqlSessionImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 查询所有用户
     * T 代表泛型类型，T(type缩写)
     *
     * @param statement
     */
    @Override
    public <T> List<T> selectList(String statement) throws Exception {
        Executor executor = new Executor(configuration);

        return (List<T>) executor.executeQuery(statement);
    }
}
