package io.jpom.controller.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.jiangzeyin.common.JsonMessage;
import cn.jiangzeyin.common.validator.ValidatorItem;
import cn.jiangzeyin.common.validator.ValidatorRule;
import io.jpom.common.BaseServerController;
import io.jpom.model.BaseWorkspaceModel;
import io.jpom.model.PageResultDto;
import io.jpom.model.data.WorkspaceModel;
import io.jpom.permission.SystemPermission;
import io.jpom.plugin.ClassFeature;
import io.jpom.plugin.Feature;
import io.jpom.plugin.MethodFeature;
import io.jpom.service.h2db.TableName;
import io.jpom.service.system.WorkspaceService;
import io.jpom.service.user.UserBindWorkspaceService;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * @author bwcx_jzy
 * @since 2021/12/3
 */
@RestController
@Feature(cls = ClassFeature.SYSTEM_WORKSPACE)
@RequestMapping(value = "/system/workspace/")
@SystemPermission
public class WorkspaceController extends BaseServerController {

	private final WorkspaceService workspaceService;
	private final UserBindWorkspaceService userBindWorkspaceService;

	public WorkspaceController(WorkspaceService workspaceService,
							   UserBindWorkspaceService userBindWorkspaceService) {
		this.workspaceService = workspaceService;
		this.userBindWorkspaceService = userBindWorkspaceService;
	}

	/**
	 * 编辑工作空间
	 *
	 * @param name        工作空间名称
	 * @param description 描述
	 * @return json
	 */
	@PostMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
	@Feature(method = MethodFeature.EDIT)
	public String create(String id, @ValidatorItem String name, @ValidatorItem String description) {
		this.checkInfo(id, name);
		//
		WorkspaceModel workspaceModel = new WorkspaceModel();
		workspaceModel.setName(name);
		workspaceModel.setDescription(description);
		if (StrUtil.isEmpty(id)) {
			// 创建
			workspaceService.insert(workspaceModel);
		} else {
			workspaceModel.setId(id);
			workspaceService.update(workspaceModel);
		}
		return JsonMessage.getString(200, "操作成功");
	}

	private void checkInfo(String id, String name) {
		Entity entity = Entity.create();
		entity.set("name", name);
		if (StrUtil.isNotEmpty(id)) {
			entity.set("id", StrUtil.format(" <> {}", id));
		}
		boolean exists = workspaceService.exists(entity);
		Assert.state(!exists, "对应的工作空间名称已经存在啦");
	}

	/**
	 * 工作空间分页列表
	 *
	 * @return json
	 */
	@PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	@Feature(method = MethodFeature.LIST)
	public String list() {
		PageResultDto<WorkspaceModel> listPage = workspaceService.listPage(getRequest());
		return JsonMessage.getString(200, "", listPage);
	}

	/**
	 * 查询工作空间列表
	 *
	 * @return json
	 */
	@GetMapping(value = "/list_all")
	@Feature(method = MethodFeature.LIST)
	public String listAll() {
		List<WorkspaceModel> list = workspaceService.list();
		return JsonMessage.getString(200, "", list);
	}

	/**
	 * 删除工作空间
	 *
	 * @param id 工作空间 ID
	 * @return json
	 */
	@GetMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	@Feature(method = MethodFeature.DEL)
	@SystemPermission(superUser = true)
	public Object delete(@ValidatorItem(value = ValidatorRule.NOT_BLANK, msg = "数据 id 不能为空") String id) {
		//
		Assert.state(!StrUtil.equals(id, WorkspaceModel.DEFAULT_ID), "不能删除默认工作空间");
		// 判断是否存在关联数据
		Set<Class<?>> classes = ClassUtil.scanPackage("io.jpom.model", BaseWorkspaceModel.class::isAssignableFrom);
		for (Class<?> aClass : classes) {
			TableName tableName = aClass.getAnnotation(TableName.class);
			if (tableName == null) {
				continue;
			}
			String sql = "select  count(1) as cnt from " + tableName.value() + " where workspaceId=?";
			List<Entity> query = workspaceService.query(sql, id);
			Entity first = CollUtil.getFirst(query);
			if (first != null) {
				Assert.notEmpty(first, "没有对应的用户信息");
				Integer cnt = first.getInt("cnt");
				Assert.state(cnt == null || cnt <= 0, "当前工作空间下还存在关联数据：" + tableName.value());
			}
		}
		// 判断用户绑定关系
		boolean workspace = userBindWorkspaceService.existsWorkspace(id);
		Assert.state(!workspace, "当前工作空间下还绑定着用户信息");
		// 删除信息
		workspaceService.delByKey(id);
		return JsonMessage.toJson(200, "删除成功");
	}
}
