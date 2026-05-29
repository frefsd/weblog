-- 管理员角色
INSERT INTO `weblog`.`user_role` (`id`, `username`, `role`, `create_time`) VALUES (1, 'admin', 'ROLE_ADMIN', '2026-07-07 01:21:15');
-- 游客角色
INSERT INTO `weblog`.`user_role` (`id`, `username`, `role`, `create_time`) VALUES (2, 'vistor', 'ROLE_VISITOR', '2026-07-07 01:23:33');

-- 默认告警规则：1分钟内超过5条ERROR日志触发钉钉通知
INSERT INTO `weblog`.`monitor_alert_rule` (`id`, `name`, `log_level`, `time_window`, `threshold`, `status`) VALUES (1, '错误日志过多', 'ERROR', 1, 5, 1);
