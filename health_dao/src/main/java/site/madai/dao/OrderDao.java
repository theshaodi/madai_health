package site.madai.dao;

import org.apache.ibatis.annotations.Param;
import site.madai.pojo.Order;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Project: site.madai.dao
 * @Author: ShaoDi Wang
 * @Date: Created in 2019-06-30 21:53
 * @Description:
 * @Version: 1.0
 */
public interface OrderDao {
    List<Order> findByCondition(Order order);

    void add(Order order);

    Map findById4Detail(Integer id);

    Integer findOrderCountByDate(@Param("orderDate") String today);

    Integer findOrderCountAfterDate(@Param("orderDate") String thisWeekMonday);

    Integer findVisitsCountByDate(@Param("orderDate") String today);

    Integer findVisitsCountAfterDate(@Param("orderDate") String thisWeekMonday);

    List<Map> findHotSetmeal();

    void delOrderById(Integer id);
}
