-- 修复现有 Quartz 表的脚本
-- 针对已存在的小写表名进行修复

USE zhglxt;

-- 检查并修复现有表的问题
-- 确保所有必需的锁记录存在

-- 清理可能存在的错误锁记录
DELETE FROM qrtz_locks WHERE SCHED_NAME != 'ZhglxtScheduler';

-- 插入必需的锁记录（如果不存在）
INSERT IGNORE INTO qrtz_locks (SCHED_NAME, LOCK_NAME) VALUES ('ZhglxtScheduler', 'STATE_ACCESS');
INSERT IGNORE INTO qrtz_locks (SCHED_NAME, LOCK_NAME) VALUES ('ZhglxtScheduler', 'TRIGGER_ACCESS');

-- 验证锁记录
SELECT 'Current lock records:' as message;
SELECT * FROM qrtz_locks WHERE SCHED_NAME = 'ZhglxtScheduler';

-- 检查表结构是否正确
SELECT 'Existing Quartz tables:' as message;
SELECT table_name, table_comment 
FROM information_schema.tables 
WHERE table_schema = 'zhglxt' 
  AND table_name LIKE 'qrtz_%' 
ORDER BY table_name;

-- 清理可能存在的旧任务数据（可选，如果需要全新开始）
-- TRUNCATE TABLE qrtz_fired_triggers;
-- TRUNCATE TABLE qrtz_simple_triggers;
-- TRUNCATE TABLE qrtz_cron_triggers;
-- TRUNCATE TABLE qrtz_blob_triggers;
-- TRUNCATE TABLE qrtz_triggers;
-- TRUNCATE TABLE qrtz_job_details;
-- TRUNCATE TABLE qrtz_paused_trigger_grps;

SELECT 'Quartz tables fix completed!' as result;