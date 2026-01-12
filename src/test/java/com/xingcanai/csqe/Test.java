package com.xingcanai.csqe;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Test {
    public static void main(String[] args) {
        System.out.println("========== 数据库时间转换演示 ==========");
        System.out.println();

        // 模拟数据库中存储的时间：2026-01-05 23:42:16+08
        System.out.println("数据库中存储的时间: 2026-01-05 23:42:16+08");
        System.out.println();

        // 创建这个时间的 ZonedDateTime 对象
        ZonedDateTime dbTime = ZonedDateTime.parse("2026-01-05T23:42:16+08:00");

        System.out.println("转成 ZonedDateTime 后：");
        System.out.println("1. toString() 输出: " + dbTime.toString());
        System.out.println("   → 这是原始的东八区时间");
        System.out.println();

        // 转成UTC时区（很多数据库驱动会这样做）
        ZonedDateTime utcTime = dbTime.withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println("2. 如果数据库驱动转成 UTC 时区:");
        System.out.println("   toString() 输出: " + utcTime.toString());
        System.out.println("   → 注意：23:42:16 - 8小时 = 15:42:16");
        System.out.println();

        System.out.println("3. 转成 LocalDateTime:");
        System.out.println("   东八区: " + dbTime.toLocalDateTime());
        System.out.println("   UTC:    " + utcTime.toLocalDateTime());
        System.out.println("   → 看！时间数字不一样了！");
        System.out.println();

        System.out.println("========== 为什么终端输出显示 15:42:16? ==========");
        System.out.println();
        System.out.println("原因：数据库或 JDBC 驱动将时间转换成了 UTC");
        System.out.println("  数据库存储: 2026-01-05 23:42:16+08");
        System.out.println("  JDBC 读取: 2026-01-05T15:42:16Z (UTC)");
        System.out.println("  toString(): 2026-01-05T15:42:16Z");
        System.out.println("  toLocalDateTime(): 2026-01-05T15:42:16 (去掉了Z)");
        System.out.println();

        System.out.println("========== 如何正确显示原始时区的时间? ==========");
        System.out.println();
        System.out.println("方法1: 转回东八区");
        ZonedDateTime backToShanghai = utcTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
        System.out.println("  " + backToShanghai);
        System.out.println();

        System.out.println("方法2: 使用系统默认时区");
        ZonedDateTime systemZone = utcTime.withZoneSameInstant(ZoneId.systemDefault());
        System.out.println("  " + systemZone);
        System.out.println();

        System.out.println("========== 验证这是同一个时刻 ==========");
        System.out.println();
        System.out.println("toEpochSecond() 比较（时间戳）：");
        System.out.println("  东八区 23:42:16: " + dbTime.toEpochSecond());
        System.out.println("  UTC    15:42:16: " + utcTime.toEpochSecond());
        System.out.println("  → 时间戳相同，说明是同一个时刻！");
    }
}
