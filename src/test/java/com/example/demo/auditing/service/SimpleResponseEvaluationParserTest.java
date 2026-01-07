package com.example.demo.auditing.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.auditing.entity.EvaluationDetail;

class SimpleResponseEvaluationParserTest {

    private SimpleResponseEvaluationParser parser;

    @BeforeEach
    void setUp() {
        parser = new SimpleResponseEvaluationParser();
    }

    @Test
    void testParseResponse_所有字段都为是() {
        // 准备测试数据
        String response = """
                完成资料发送：是
                完成到课提醒：是
                完成课后作业发布：是
                下周资料发送：是
                周日螳螂销转链接：是
                风险词触发：是
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "FIRST_WEEK");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getHasMaterialSend());
        assertEquals(1, result.getHasCourseRemind());
        assertEquals(1, result.getHasHomeworkPublish());
        assertEquals(1, result.getHasWeekMaterialSend());
        assertEquals(1, result.getHasSundayLinkSend());
        assertEquals(1, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_所有字段都为否() {
        // 准备测试数据
        String response = """
                完成资料发送：否
                完成到课提醒：否
                完成课后作业发布：否
                下周资料发送：否
                周日螳螂销转链接：否
                风险词触发：否
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "FIRST_WEEK");

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getHasMaterialSend());
        assertEquals(0, result.getHasCourseRemind());
        assertEquals(0, result.getHasHomeworkPublish());
        assertEquals(0, result.getHasWeekMaterialSend());
        assertEquals(0, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_部分字段为是() {
        // 准备测试数据
        String response = """
                完成资料发送：是
                完成到课提醒：否
                完成课后作业发布：是
                下周资料发送：否
                周日螳螂销转链接：是
                风险词触发：否
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "SECOND_WEEK");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getHasMaterialSend());
        assertEquals(0, result.getHasCourseRemind());
        assertEquals(1, result.getHasHomeworkPublish());
        assertEquals(0, result.getHasWeekMaterialSend());
        assertEquals(1, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_缺少某些关键词() {
        // 准备测试数据 - 只包含部分关键词
        String response = """
                完成资料发送：是
                完成到课提醒：是
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "FIRST_WEEK");

        // 验证结果 - 缺少的字段应该为0（默认false）
        assertNotNull(result);
        assertEquals(1, result.getHasMaterialSend());
        assertEquals(1, result.getHasCourseRemind());
        assertEquals(0, result.getHasHomeworkPublish());
        assertEquals(0, result.getHasWeekMaterialSend());
        assertEquals(0, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_空字符串() {
        // 准备测试数据
        String response = "";

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "FIRST_WEEK");

        // 验证结果 - 所有字段应该为0
        assertNotNull(result);
        assertEquals(0, result.getHasMaterialSend());
        assertEquals(0, result.getHasCourseRemind());
        assertEquals(0, result.getHasHomeworkPublish());
        assertEquals(0, result.getHasWeekMaterialSend());
        assertEquals(0, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_包含额外文本() {
        // 准备测试数据 - 包含额外的说明文本
        String response = """
                评估报告：
                完成资料发送：是（已发送课程资料）
                完成到课提醒：是（提前一天提醒）
                完成课后作业发布：否（未发送）
                下周资料发送：是
                周日螳螂销转链接：否
                风险词触发：否
                备注：整体表现良好
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "THIRD_WEEK");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getHasMaterialSend());
        assertEquals(1, result.getHasCourseRemind());
        assertEquals(0, result.getHasHomeworkPublish());
        assertEquals(1, result.getHasWeekMaterialSend());
        assertEquals(0, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_关键词在行首() {
        // 准备测试数据
        String response = """
                完成资料发送是
                完成到课提醒是的
                完成课后作业发布：是的，已完成
                下周资料发送：是
                周日螳螂销转链接：包含是字
                风险词触发：否
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "FIRST_WEEK");

        // 验证结果 - 只要包含"是"字就应该为true
        assertNotNull(result);
        assertEquals(1, result.getHasMaterialSend());
        assertEquals(1, result.getHasCourseRemind());
        assertEquals(1, result.getHasHomeworkPublish());
        assertEquals(1, result.getHasWeekMaterialSend());
        assertEquals(1, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }

    @Test
    void testParseResponse_不同报告类型() {
        // 准备测试数据
        String response = """
                完成资料发送：是
                完成到课提醒：是
                完成课后作业发布：是
                下周资料发送：是
                周日螳螂销转链接：是
                风险词触发：否
                """;

        // 测试不同的报告类型，结果应该相同（reportType参数当前未使用）
        EvaluationDetail result1 = parser.parseResponse(response, "FIRST_WEEK");
        EvaluationDetail result2 = parser.parseResponse(response, "SECOND_WEEK");
        EvaluationDetail result3 = parser.parseResponse(response, "THIRD_WEEK");
        EvaluationDetail result4 = parser.parseResponse(response, "FOURTH_WEEK");

        // 验证所有报告类型的解析结果一致
        assertEquals(result1.getHasMaterialSend(), result2.getHasMaterialSend());
        assertEquals(result1.getHasCourseRemind(), result3.getHasCourseRemind());
        assertEquals(result1.getHasHomeworkPublish(), result4.getHasHomeworkPublish());
    }

    @Test
    void testParseResponse_特殊字符和空格() {
        // 准备测试数据 - 包含多余空格和特殊字符
        String response = """
                  完成资料发送  ：  是
                完成到课提醒    ：是
                完成课后作业发布：  是
                下周资料发送：否
                周日螳螂销转链接：  否
                风险词触发：否
                """;

        // 执行测试
        EvaluationDetail result = parser.parseResponse(response, "FIRST_WEEK");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getHasMaterialSend());
        assertEquals(1, result.getHasCourseRemind());
        assertEquals(1, result.getHasHomeworkPublish());
        assertEquals(0, result.getHasWeekMaterialSend());
        assertEquals(0, result.getHasSundayLinkSend());
        assertEquals(0, result.getHasRiskWordTrigger());
    }
}
