package com.xingcanai.csqe.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface WxChatMessageRepository extends JpaRepository<WxChatMessage, String> {

    /**
     * 根据时间范围查询消息
     */
    List<WxChatMessage> findByMsgTimeBetweenOrderByMsgTimeAsc(ZonedDateTime startTime, ZonedDateTime endTime);

    /**
     * 获取最新的一条消息
     */
    WxChatMessage findTopByOrderByMsgTimeDesc();

    /**
     * 查询员工在指定时间范围内的所有聊天客户
     */
    @Query("SELECT DISTINCT " +
           "CASE WHEN m.fromId = :employeeId THEN m.acceptId ELSE m.fromId END " +
           "FROM WxChatMessage m " +
           "WHERE (m.fromId = :employeeId OR m.acceptId = :employeeId) " +
           "AND m.msgTime BETWEEN :startTime AND :endTime " +
           "AND m.acceptType = 1")
    List<String> findCustomersByEmployeeAndTimeRange(@Param("employeeId") String employeeId,
                                                     @Param("startTime") ZonedDateTime startTime,
                                                     @Param("endTime") ZonedDateTime endTime);

    /**
     * 查询员工与指定客户在时间范围内的聊天记录
     */
    @Query("SELECT m FROM WxChatMessage m " +
           "WHERE ((m.fromId = :employeeId AND m.acceptId = :customerId) " +
           "OR (m.fromId = :customerId AND m.acceptId = :employeeId)) " +
           "AND m.msgTime BETWEEN :startTime AND :endTime " +
           "ORDER BY m.msgTime ASC")
    List<WxChatMessage> findChatBetweenEmployeeAndCustomer(@Param("employeeId") String employeeId,
                                                         @Param("customerId") String customerId,
                                                         @Param("startTime") ZonedDateTime startTime,
                                                         @Param("endTime") ZonedDateTime endTime);


    /**
     * 查询所有参与聊天的员工ID
     */
    @Query("SELECT DISTINCT m.fromId FROM WxChatMessage m " +
           "WHERE m.msgTime BETWEEN :startTime AND :endTime " +
           "AND m.acceptType = 1")
    List<String> findAllEmployeeIds(@Param("startTime") ZonedDateTime startTime,
                                   @Param("endTime") ZonedDateTime endTime);

    /**
     * 根据data_seq查询最大值，用于增量同步
     */
    @Query("SELECT MAX(m.dataSeq) FROM WxChatMessage m")
    Long findMaxDataSeq();

    /**
     * 查询员工与指定客户之间最早的一条聊天记录
     */
    @Query("SELECT m FROM WxChatMessage m " +
           "WHERE ((m.fromId = :employeeId AND m.acceptId = :customerId) " +
           "OR (m.fromId = :customerId AND m.acceptId = :employeeId)) " +
           "ORDER BY m.msgTime ASC LIMIT 1")
    WxChatMessage findFirstChatBetweenEmployeeAndCustomer(@Param("employeeId") String employeeId,
                                                          @Param("customerId") String customerId);

}
