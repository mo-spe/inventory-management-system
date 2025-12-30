package com.example.inventorybackend.Repository;

import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.projection.SalesSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, String> {
    List<OperationLog> findTop10ByOrderByTimestampDesc();

    // 获取最近10条

    /**
     * 删除某个商品的所有日志
     */
    @Modifying
    @Query("DELETE FROM OperationLog o WHERE o.productId = :productId")
    void deleteByProductId(String productId);

    /**
     * 获取近期出库总量（支持动态时间范围）
     */
    @Query("SELECT o.productId as productId, o.productName as productName, SUM(o.quantity) as quantity " +
            "FROM OperationLog o " +
            "WHERE o.action = '出库' AND o.timestamp >= :start " +
            "GROUP BY o.productId, o.productName")
    List<SalesSummaryProjection> getRecentSales(@Param("start") LocalDateTime start);

    /**
     * 统计某商品在指定时间段内的总出库量
     */
    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM OperationLog o " +
            "WHERE o.productId = :pid AND o.action = '出库' AND o.timestamp >= :start")
    int getTotalSoldForProduct(@Param("pid") String pid, @Param("start") LocalDateTime start);
}
