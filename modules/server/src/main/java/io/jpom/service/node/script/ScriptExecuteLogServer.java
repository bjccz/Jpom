package io.jpom.service.node.script;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.jpom.common.BaseServerController;
import io.jpom.common.forward.NodeForward;
import io.jpom.common.forward.NodeUrl;
import io.jpom.model.BaseDbModel;
import io.jpom.model.data.*;
import io.jpom.service.h2db.BaseNodeService;
import io.jpom.service.node.NodeService;
import io.jpom.service.system.WorkspaceService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 脚本默认执行记录
 *
 * @author bwcx_jzy
 * @since 2021/12/12
 */
@Service
public class ScriptExecuteLogServer extends BaseNodeService<ScriptExecuteLogModel> {

	public ScriptExecuteLogServer(NodeService nodeService,
								  WorkspaceService workspaceService) {
		super(nodeService, workspaceService, "脚本模版日志");
	}

	@Override
	protected String[] clearTimeColumns() {
		return new String[]{"createTimeMillis"};
	}

	@Override
	public JSONObject getItem(NodeModel nodeModel, String id) {
		return null;
	}

	@Override
	public JSONArray getLitDataArray(NodeModel nodeModel) {
		JsonMessage<Object> jsonMessage = NodeForward.requestBySys(nodeModel, NodeUrl.SCRIPT_PULL_EXEC_LOG, "pullCount", 100);
		if (jsonMessage.getCode() != HttpStatus.HTTP_OK) {
			throw new IllegalStateException(jsonMessage.getMsg());
		}
		Object data = jsonMessage.getData();
		//
		JSONArray jsonArray = (JSONArray) JSONArray.toJSON(data);
		for (Object o : jsonArray) {
			JSONObject jsonObject = (JSONObject) o;
			jsonObject.put("nodeId", nodeModel.getId());
			jsonObject.put("triggerExecType", 1);
		}
		return jsonArray;

	}

	@Override
	public void syncAllNode() {
		//
	}

	/**
	 * 同步执行 同步节点信息(增量)
	 *
	 * @param nodeModel 节点信息
	 * @return json
	 */
	public Collection<String> syncExecuteNodeInc(NodeModel nodeModel) {
		String nodeModelName = nodeModel.getName();
		if (!nodeModel.isOpenStatus()) {
			DefaultSystemLog.getLog().debug("{} 节点未启用", nodeModelName);
			return null;
		}
		try {
			JSONArray jsonArray = this.getLitDataArray(nodeModel);
			if (CollUtil.isEmpty(jsonArray)) {
				//
				return null;
			}
			//
			List<ScriptExecuteLogModel> models = jsonArray.toJavaList(this.tClass).stream()
					.filter(item -> {
						// 检查对应的工作空间 是否存在
						return workspaceService.exists(new WorkspaceModel(item.getWorkspaceId()));
					})
					.filter(item -> {
						// 避免重复同步
						return StrUtil.equals(nodeModel.getWorkspaceId(), item.getWorkspaceId());
					})
					.collect(Collectors.toList());
			// 设置 临时缓存，便于放行检查
			BaseServerController.resetInfo(UserModel.EMPTY);
			//
			models.forEach(ScriptExecuteLogServer.super::upsert);
			String format = StrUtil.format(
					"{} 节点拉取到 {} 个执行记录,更新 {} 个执行记录",
					nodeModelName, CollUtil.size(jsonArray),
					CollUtil.size(models));
			DefaultSystemLog.getLog().debug(format);
			return models.stream().map(BaseDbModel::getId).collect(Collectors.toList());
		} catch (Exception e) {
			this.checkException(e, nodeModelName);
			return null;
		} finally {
			BaseServerController.removeEmpty();
		}
	}

	@Override
	protected void executeClearImpl(int h2DbLogStorageCount) {
		super.autoLoopClear("createTimeMillis", h2DbLogStorageCount,
				null,
				executeLogModel -> {
					try {
						NodeModel nodeModel = nodeService.getByKey(executeLogModel.getNodeId());
						JsonMessage<Object> jsonMessage = NodeForward.requestBySys(nodeModel, NodeUrl.SCRIPT_DEL_LOG,
								"id", executeLogModel.getScriptId(), "executeId", executeLogModel.getId());
						if (jsonMessage.getCode() != HttpStatus.HTTP_OK) {
							DefaultSystemLog.getLog().info(jsonMessage.toString());
						}
					} catch (Exception e) {
						DefaultSystemLog.getLog().error("自动清除数据错误", e);
					}
				});
	}
}
