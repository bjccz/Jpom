package io.jpom.controller.node.monitor;

import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import com.alibaba.fastjson.JSONObject;
import io.jpom.common.BaseServerController;
import io.jpom.common.forward.NodeForward;
import io.jpom.common.forward.NodeUrl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 内存查看
 *
 * @author Administrator
 */
@Controller
@RequestMapping(value = "/node/manage/")
public class InternalController extends BaseServerController {


	/**
	 * @return
	 * @author Hotstrip
	 * get InternalData
	 * 获取内存信息接口
	 */
	@RequestMapping(value = "getInternalData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getInternalData() {
		JSONObject data = NodeForward.requestData(getNode(), NodeUrl.Manage_internal_data, getRequest(), JSONObject.class);
		DefaultSystemLog.getLog().info("data: {}", data == null ? "" : data.toString());
		return JsonMessage.getString(200, "success", data);
	}

	/**
	 * 查询监控线程列表
	 *
	 * @return json
	 */
	@RequestMapping(value = "threadInfos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String threadInfos() {
		return NodeForward.request(getNode(), getRequest(), NodeUrl.Manage_internal_threadInfos).toString();
	}

	/**
	 * 导出堆栈信息
	 */
	@RequestMapping(value = "stack", method = RequestMethod.GET)
	@ResponseBody
	public void stack() {
		NodeForward.requestDownload(getNode(), getRequest(), getResponse(), NodeUrl.Manage_internal_stack);
	}

	/**
	 * 导出内存信息
	 */
	@RequestMapping(value = "ram", method = RequestMethod.GET)
	@ResponseBody
	public void ram() {
		NodeForward.requestDownload(getNode(), getRequest(), getResponse(), NodeUrl.Manage_internal_ram);
	}
}
