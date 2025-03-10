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
package io.jpom.controller.system;

import cn.hutool.core.util.StrUtil;
import cn.jiangzeyin.common.JsonMessage;
import com.alibaba.fastjson.JSONObject;
import io.jpom.common.BaseServerController;
import io.jpom.model.data.MailAccountModel;
import io.jpom.monitor.EmailUtil;
import io.jpom.permission.SystemPermission;
import io.jpom.plugin.*;
import io.jpom.service.system.SystemParametersServer;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控邮箱配置
 *
 * @author bwcx_jzy
 * @date 2019/7/16
 */
@RestController
@RequestMapping(value = "system")
@Feature(cls = ClassFeature.SYSTEM_EMAIL)
@SystemPermission
public class SystemMailConfigController extends BaseServerController {

	private final SystemParametersServer systemParametersServer;

	public SystemMailConfigController(SystemParametersServer systemParametersServer) {
		this.systemParametersServer = systemParametersServer;
	}

	/**
	 * load mail config data
	 * 加载邮件配置
	 *
	 * @return json
	 * @author Hotstrip
	 */
	@RequestMapping(value = "mail-config-data", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Feature(method = MethodFeature.LIST)
	public String mailConfigData() {
		MailAccountModel item = systemParametersServer.getConfig(MailAccountModel.ID, MailAccountModel.class);
		if (item != null) {
			item.setPass(null);
		}
		return JsonMessage.getString(200, "success", item);
	}

	@RequestMapping(value = "mailConfig_save.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Feature(method = MethodFeature.EDIT)
	public String listData(MailAccountModel mailAccountModel) {
		Assert.notNull(mailAccountModel, "请填写信息,并检查是否填写合法");
		Assert.hasText(mailAccountModel.getHost(), "请填写host");
		Assert.hasText(mailAccountModel.getUser(), "请填写user");
		Assert.hasText(mailAccountModel.getFrom(), "请填写from");
		// 验证是否正确
		MailAccountModel item = systemParametersServer.getConfig(MailAccountModel.ID, MailAccountModel.class);
		if (item != null) {
			mailAccountModel.setPass(StrUtil.emptyToDefault(mailAccountModel.getPass(), item.getPass()));
		} else {
			Assert.hasText(mailAccountModel.getPass(), "请填写pass");
		}
		IPlugin plugin = PluginFactory.getPlugin(DefaultPlugin.Email);
		boolean checkInfo = plugin.check("checkInfo", JSONObject.toJSON(mailAccountModel), null);
		Assert.state(checkInfo, "验证邮箱信息失败，请检查配置的邮箱信息。端口号、授权码等。");
//		try {
//			MailAccount account = EmailUtil.getAccount(mailAccountModel);
//			Session session = MailUtil.getSession(account, false);
//			Transport transport = session.getTransport("smtp");
//			transport.connect();
//			transport.close();
//		} catch (Exception e) {
//			return JsonMessage.getString(406, "验证邮箱信息失败，请检查配置的邮箱信息。端口号、授权码等。" + e.getMessage());
//		}
		systemParametersServer.upsert(MailAccountModel.ID, mailAccountModel, MailAccountModel.ID);
		//
		EmailUtil.refreshConfig();
		return JsonMessage.getString(200, "保存成功");
	}
}
