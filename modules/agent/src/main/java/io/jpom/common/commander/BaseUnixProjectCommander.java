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
package io.jpom.common.commander;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.StrUtil;
import io.jpom.model.data.NodeProjectInfoModel;
import io.jpom.model.system.NetstatModel;
import io.jpom.util.CommandUtil;
import io.jpom.util.JvmUtil;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * unix
 *
 * @author bwcx_jzy
 * @since 2021/12/17
 */
public abstract class BaseUnixProjectCommander extends AbstractProjectCommander {


	@Override
	public String buildCommand(NodeProjectInfoModel nodeProjectInfoModel, NodeProjectInfoModel.JavaCopyItem javaCopyItem) {
		String path = NodeProjectInfoModel.getClassPathLib(nodeProjectInfoModel);
		if (StrUtil.isBlank(path)) {
			return null;
		}
		String tag = javaCopyItem == null ? nodeProjectInfoModel.getId() : javaCopyItem.getTagId();
		return String.format("nohup %s %s %s" +
						" %s  %s  %s >> %s 2>&1 &",
				getRunJavaPath(nodeProjectInfoModel, false),
				javaCopyItem == null ? nodeProjectInfoModel.getJvm() : javaCopyItem.getJvm(),
				JvmUtil.getJpomPidTag(tag, nodeProjectInfoModel.allLib()),
				path,
				nodeProjectInfoModel.getMainClass(),
				javaCopyItem == null ? nodeProjectInfoModel.getArgs() : javaCopyItem.getArgs(),
				nodeProjectInfoModel.getAbsoluteLog(javaCopyItem));
	}

	@Override
	public String stop(NodeProjectInfoModel nodeProjectInfoModel, NodeProjectInfoModel.JavaCopyItem javaCopyItem) throws Exception {
		Tuple tuple = super.stopBefore(nodeProjectInfoModel, javaCopyItem);
		String result = tuple.get(1);
		String webHook = tuple.get(0);
		int pid = parsePid(result);
		if (pid > 0) {
			File file = FileUtil.file(nodeProjectInfoModel.allLib());
			String kill = AbstractSystemCommander.getInstance().kill(file, pid);
			if (this.loopCheckRun(nodeProjectInfoModel.getId(), false)) {
				// 强制杀进程
				String cmd = String.format("kill -9 %s", pid);
				CommandUtil.asyncExeLocalCommand(file, cmd);
			}
			String tag = javaCopyItem == null ? nodeProjectInfoModel.getId() : javaCopyItem.getTagId();
			result = status(tag) + StrUtil.SPACE + kill;
		}
		return StrUtil.format("{}  {}", result, webHook);
	}

	protected List<NetstatModel> listNetstat(String cmd) {
		String result = CommandUtil.execSystemCommand(cmd);
		List<String> netList = StrSplitter.splitTrim(result, StrUtil.LF, true);
		if (CollUtil.isEmpty(netList)) {
			return null;
		}
		return netList.stream().map(str -> {
			List<String> list = StrSplitter.splitTrim(str, " ", true);
			if (list.size() < 5) {
				return null;
			}
			NetstatModel netstatModel = new NetstatModel();
			netstatModel.setProtocol(list.get(0));
			netstatModel.setReceive(list.get(1));
			netstatModel.setSend(list.get(2));
			netstatModel.setLocal(list.get(3));
			netstatModel.setForeign(list.get(4));
			if ("tcp".equalsIgnoreCase(netstatModel.getProtocol())) {
				netstatModel.setStatus(CollUtil.get(list, 5));
				netstatModel.setName(CollUtil.get(list, 6));
			} else {
				netstatModel.setStatus(StrUtil.DASHED);
				netstatModel.setName(CollUtil.get(list, 5));
			}

			return netstatModel;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}
}
