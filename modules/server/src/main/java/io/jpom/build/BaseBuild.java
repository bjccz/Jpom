/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Code Technology Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.jpom.build;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.spring.SpringUtil;
import io.jpom.model.data.BuildInfoModel;
import io.jpom.model.enums.BuildStatus;
import io.jpom.service.dblog.BuildInfoService;

import java.io.File;
import java.io.PrintWriter;

/**
 * 构建的基础类
 *
 * @author bwcx_jzy
 * @date 2019/7/19
 */
public abstract class BaseBuild {

	/**
	 * 日志文件
	 */
	protected final File logFile;
	/**
	 * 构建ID
	 */
	protected final String buildModelId;

	BaseBuild(File logFile, String buildModelId) {
		this.logFile = logFile;
		this.buildModelId = buildModelId;
	}

	protected void log(String title, Throwable throwable, BuildStatus status) {
		DefaultSystemLog.getLog().error(title, throwable);
		FileUtil.appendLines(CollectionUtil.toList(title), this.logFile, CharsetUtil.CHARSET_UTF_8);
		String s = ExceptionUtil.stacktraceToString(throwable);
		FileUtil.appendLines(CollectionUtil.toList(s), this.logFile, CharsetUtil.CHARSET_UTF_8);
		this.updateStatus(status);
	}

	protected void log(String info) {
		FileUtil.appendLines(CollectionUtil.toList(info), this.logFile, CharsetUtil.CHARSET_UTF_8);
	}

	protected PrintWriter getPrintWriter() {
		return FileWriter.create(this.logFile, CharsetUtil.CHARSET_UTF_8).getPrintWriter(true);
	}

	protected boolean updateStatus(BuildStatus status) {
		BuildInfoService buildService = SpringUtil.getBean(BuildInfoService.class);
		BuildInfoModel item = buildService.getByKey(this.buildModelId);
		item.setStatus(status.getCode());
		buildService.update(item);
		return true;
	}
}
