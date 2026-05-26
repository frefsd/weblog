package com.blog;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.LogSearchDTO;
import com.blog.entity.AlertRecord;
import com.blog.entity.AlertRule;
import com.blog.entity.LogRecord;
import com.blog.mapper.AlertRecordMapper;
import com.blog.mapper.AlertRuleMapper;
import com.blog.mapper.MonitorLogMapper;
import com.blog.service.AlertService;
import com.blog.service.MonitorLogService;
import com.blog.service.impl.AlertServiceImpl;
import com.blog.service.impl.MonitorLogServiceImpl;
import com.blog.utils.MailNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeBlogServerApplicationTests {

    // ==================== MailNotifier 测试 ====================

    @Test
    @DisplayName("MailNotifier - 发送告警邮件成功")
    void mailNotifier_sendSuccess() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailNotifier notifier = new MailNotifier(mailSender);
        ReflectionTestUtils.setField(notifier, "from", "test@qq.com");
        ReflectionTestUtils.setField(notifier, "to", "admin@qq.com");

        boolean result = notifier.sendAlert("错误过多", "ERROR", 8, 5, "NullPointerException");

        assertTrue(result);
    }

    @Test
    @DisplayName("MailNotifier - 邮箱未配置返回false")
    void mailNotifier_noConfig() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailNotifier notifier = new MailNotifier(mailSender);
        ReflectionTestUtils.setField(notifier, "to", "");

        boolean result = notifier.sendAlert("错误过多", "ERROR", 8, 5, "error");

        assertFalse(result);
    }

    @Test
    @DisplayName("MailNotifier - 发送异常返回false")
    void mailNotifier_sendException() {
        JavaMailSender mailSender = mock(JavaMailSender.class, withSettings().defaultAnswer(invocation -> {
            if ("send".equals(invocation.getMethod().getName())) {
                throw new RuntimeException("SMTP error");
            }
            return null;
        }));
        MailNotifier notifier = new MailNotifier(mailSender);
        ReflectionTestUtils.setField(notifier, "from", "test@qq.com");
        ReflectionTestUtils.setField(notifier, "to", "admin@qq.com");

        boolean result = notifier.sendAlert("错误过多", "ERROR", 8, 5, "error");

        assertFalse(result);
    }

    // ==================== MonitorLogServiceImpl 测试 ====================

    @Test
    @DisplayName("MonitorLogService - 保存日志成功")
    void saveLog() {
        MonitorLogMapper mockMapper = mock(MonitorLogMapper.class);
        MonitorLogService service = new MonitorLogServiceImpl(mockMapper);
        LogRecord record = new LogRecord();
        record.setLevel("ERROR");
        record.setMethod("TestController.test");
        record.setType("HTTP_REQUEST");

        service.save(record);

        assertNotNull(record.getCreateTime());
        verify(mockMapper, times(1)).insert(any(LogRecord.class));
    }

    @Test
    @DisplayName("MonitorLogService - 分页查询带筛选条件")
    void pageLog_withFilters() {
        MonitorLogMapper mockMapper = mock(MonitorLogMapper.class);
        MonitorLogService service = new MonitorLogServiceImpl(mockMapper);
        Page<LogRecord> mockPage = new Page<>(1, 10);
        LogSearchDTO dto = new LogSearchDTO();
        dto.setLevel("ERROR");
        dto.setType("HTTP_REQUEST");
        dto.setKeyword("异常");
        when(mockMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        Page<LogRecord> result = service.page(dto);

        assertNotNull(result);
        verify(mockMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    // ==================== AlertService 测试 ====================

    @Mock
    private AlertRuleMapper alertRuleMapper;
    @Mock
    private MonitorLogMapper monitorLogMapper;
    @Mock
    private AlertRecordMapper alertRecordMapper;
    @Mock
    private MailNotifier mailNotifier;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(alertRuleMapper, monitorLogMapper, alertRecordMapper, mailNotifier);
    }

    @Test
    @DisplayName("AlertService - 列出已启用规则")
    void listEnabledRules() {
        AlertRule rule = new AlertRule();
        rule.setStatus(1);
        rule.setName("错误过多");
        when(alertRuleMapper.selectList(any())).thenReturn(List.of(rule));

        List<AlertRule> rules = alertService.listEnabledRules();

        assertEquals(1, rules.size());
        assertEquals("错误过多", rules.get(0).getName());
    }

    @Test
    @DisplayName("AlertService - 新增规则")
    void saveNewRule() {
        AlertRule rule = new AlertRule();
        rule.setName("新规则");

        alertService.saveRule(rule);

        assertNotNull(rule.getCreateTime());
        verify(alertRuleMapper, times(1)).insert(any(AlertRule.class));
    }

    @Test
    @DisplayName("AlertService - 删除规则")
    void deleteRule() {
        alertService.deleteRule(1L);
        verify(alertRuleMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("AlertService - 告警评估：未达阈值不触发通知")
    void evaluate_notEnoughErrors() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("错误过多");
        rule.setLogLevel("ERROR");
        rule.setTimeWindow(1);
        rule.setThreshold(5);
        when(monitorLogMapper.selectCount(any(Wrapper.class))).thenReturn(3L);

        alertService.evaluateAndNotify(rule, "ERROR");

        verify(mailNotifier, never()).sendAlert(anyString(), anyString(), anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("AlertService - 告警评估：超阈值触发通知和记录")
    void evaluate_triggerAlert() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("错误过多");
        rule.setLogLevel("ERROR");
        rule.setTimeWindow(1);
        rule.setThreshold(5);
        when(monitorLogMapper.selectCount(any(Wrapper.class))).thenReturn(8L);
        when(alertRecordMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        LogRecord latest = new LogRecord();
        latest.setErrorMessage("NullPointerException");
        when(monitorLogMapper.selectOne(any(Wrapper.class))).thenReturn(latest);
        when(mailNotifier.sendAlert(anyString(), anyString(), anyInt(), anyInt(), anyString())).thenReturn(true);

        alertService.evaluateAndNotify(rule, "ERROR");

        verify(mailNotifier, times(1)).sendAlert(anyString(), anyString(), anyInt(), anyInt(), anyString());
        verify(alertRecordMapper, times(1)).insert(any(AlertRecord.class));
    }

    @Test
    @DisplayName("AlertService - 告警评估：时间窗口内已通知过则跳过")
    void evaluate_dedup() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("错误过多");
        rule.setLogLevel("ERROR");
        rule.setTimeWindow(1);
        rule.setThreshold(5);
        when(monitorLogMapper.selectCount(any(Wrapper.class))).thenReturn(8L);
        when(alertRecordMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        alertService.evaluateAndNotify(rule, "ERROR");

        verify(mailNotifier, never()).sendAlert(anyString(), anyString(), anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("AlertService - 告警记录分页查询")
    void pageAlertRecord() {
        Page<AlertRecord> mockPage = new Page<>(1, 10);
        when(alertRecordMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(mockPage);

        Page<AlertRecord> result = alertService.pageAlertRecord(1, 10);

        assertNotNull(result);
        verify(alertRecordMapper, times(1)).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    @DisplayName("生成 BCrypt 加密密码")
    void generateBcryptPassword() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        String rawPassword = "libaizuishuai@admin";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("===========================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("===========================================");
    }
}
