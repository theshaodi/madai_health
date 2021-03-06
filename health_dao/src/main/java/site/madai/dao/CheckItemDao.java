package site.madai.dao;

import com.github.pagehelper.Page;
import site.madai.pojo.CheckItem;

import java.util.List;

/**
 * @Project: site.madai.dao
 * @Author: ShaoDi Wang
 * @Date: Created in 2019-06-24 21:53
 * @Description:
 * @Version: 1.0
 */
public interface CheckItemDao {

    Page<CheckItem> findByCondition(String queryString);

    void add(CheckItem checkItem);

    CheckItem findById(Integer id);

    void update(CheckItem checkItem);

    void delById(Integer id);

    List<CheckItem> findAll();

    long getCheckItemIdCountFrom_t_checkgroup_checkitemByCheckitemId(Integer id);
}
