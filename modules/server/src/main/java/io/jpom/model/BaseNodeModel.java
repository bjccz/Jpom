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
package io.jpom.model;

import cn.hutool.crypto.SecureUtil;
import io.jpom.model.data.ProjectInfoModel;
import org.springframework.util.Assert;

/**
 * 节点 数据
 *
 * @author bwcx_jzy
 * @since 2021/12/05
 */
public abstract class BaseNodeModel extends BaseWorkspaceModel {

	/**
	 * 节点Id
	 *
	 * @see io.jpom.model.data.NodeModel
	 */
	private String nodeId;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String fullId() {
		String workspaceId = this.getWorkspaceId();

		String nodeId = this.getNodeId();

		String dataId = this.dataId();

		return ProjectInfoModel.fullId(workspaceId, nodeId, dataId);
	}

	public static String fullId(String workspaceId, String nodeId, String dataId) {

		Assert.hasText(workspaceId, "workspaceId");

		Assert.hasText(workspaceId, "nodeId");

		Assert.hasText(workspaceId, "dataId");
		return SecureUtil.sha1(workspaceId + nodeId + dataId);
	}

	/**
	 * 获取数据ID
	 *
	 * @return 数据ID
	 */
	public abstract String dataId();

	/**
	 * 设置数据ID
	 *
	 * @param id 数据ID
	 */
	public abstract void dataId(String id);
}
