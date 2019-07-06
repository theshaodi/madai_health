package site.madai.dao;

import org.apache.ibatis.annotations.Param;
import site.madai.pojo.Permission;

import java.util.List;

/**
 * @Project: site.madai.dao
 * @Author: ShaoDi Wang
 * @Date: Created in 2019-07-05 21:21
 * @Description:
 * @Version: 1.0
 */
public interface PermissionDao {
    List<Permission> findPermissionListByRoleId(@Param("roleId") Integer roleId);
}
