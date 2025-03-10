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
package io.jpom.controller.openapi;

import cn.jiangzeyin.common.JsonMessage;
import cn.jiangzeyin.controller.base.AbstractController;
import io.jpom.common.ServerOpenApi;
import io.jpom.service.node.NodeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 节点管理
 *
 * @author bwcx_jzy
 * @date 2019/8/5
 */
@RestController
public class NodeInfoController extends AbstractController {


	private final NodeService nodeService;

	public NodeInfoController(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * 添加或者更新节点信息
	 *
	 * @return json
	 */
	@RequestMapping(value = ServerOpenApi.UPDATE_NODE_INFO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String update() {
		nodeService.update(getRequest(), true);
		return JsonMessage.getString(200, "操作成功");
	}
}
