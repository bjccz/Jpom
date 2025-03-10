-- @author bwcx_jzy

ALTER TABLE BUILD_INFO
	ADD IF NOT EXISTS webhook VARCHAR(255) comment 'webhook';

ALTER TABLE BUILD_INFO
	ADD IF NOT EXISTS extraData CLOB COMMENT '额外信息，JSON 字符串格式';

-- @author jzy 字段类型修改为 json
-- @author Hotstrip 字段类型修改为 CLOB
ALTER TABLE BUILD_INFO
	ALTER COLUMN extraData CLOB COMMENT '额外信息，JSON 字符串格式';

-- @author jzy 增加构建产物字段长度
ALTER TABLE BUILD_INFO
	ADD IF NOT EXISTS resultDirFile VARCHAR(200) comment '构建产物目录';
ALTER TABLE BUILD_INFO
	ALTER COLUMN resultDirFile VARCHAR(200) comment '构建产物目录';
ALTER TABLE BUILD_INFO
	ADD IF NOT EXISTS buildId int comment '构建 id';

ALTER TABLE REPOSITORY
	ADD IF NOT EXISTS gitUrl varchar(255) comment '仓库地址';

ALTER TABLE BUILD_INFO
	ADD IF NOT EXISTS autoBuildCron VARCHAR(100) comment '自动构建表达式';
