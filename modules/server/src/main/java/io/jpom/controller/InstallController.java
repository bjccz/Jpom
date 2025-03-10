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
package io.jpom.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import cn.jiangzeyin.common.validator.ValidatorConfig;
import cn.jiangzeyin.common.validator.ValidatorItem;
import cn.jiangzeyin.common.validator.ValidatorRule;
import io.jpom.JpomApplication;
import io.jpom.common.BaseServerController;
import io.jpom.common.interceptor.LoginInterceptor;
import io.jpom.common.interceptor.NotLogin;
import io.jpom.model.data.UserModel;
import io.jpom.model.dto.UserLoginDto;
import io.jpom.service.user.UserService;
import io.jpom.util.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 初始化程序
 *
 * @author jiangzeyin
 * @date 2019/2/22
 */
@Controller
public class InstallController extends BaseServerController {


	private final UserService userService;

	public InstallController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 初始化提交
	 *
	 * @param userName 系统管理员登录名
	 * @param userPwd  系统管理员的登录密码
	 * @return json
	 */
	@RequestMapping(value = "install_submit.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@NotLogin
	@ResponseBody
	public String installSubmit(
			@ValidatorConfig(value = {
					@ValidatorItem(value = ValidatorRule.NOT_EMPTY, msg = "登录名不能为空"),
					@ValidatorItem(value = ValidatorRule.NOT_BLANK, range = "3:20", msg = "登录名长度范围3-20"),
					@ValidatorItem(value = ValidatorRule.WORD, msg = "登录名不能包含汉字并且不能包含特殊字符")
			}) String userName,
			@ValidatorConfig(value = {
					@ValidatorItem(value = ValidatorRule.NOT_BLANK, msg = "密码不能为空")
			}) String userPwd) {
		//
		Assert.state(!userService.canUse(), "系统已经初始化过啦，请勿重复初始化");

		boolean systemOccupyUserName = StrUtil.equalsAnyIgnoreCase(userName, UserModel.DEMO_USER, JpomApplication.SYSTEM_ID, UserModel.SYSTEM_ADMIN);
		Assert.state(!systemOccupyUserName, "当前登录名已经被系统占用啦");
		// 创建用户
		UserModel userModel = new UserModel();
		userModel.setName(UserModel.SYSTEM_OCCUPY_NAME);
		userModel.setId(userName);
		userModel.setSalt(userService.generateSalt());
		userModel.setPassword(SecureUtil.sha1(userPwd + userModel.getSalt()));
		userModel.setSystemUser(1);
		userModel.setParent(UserModel.SYSTEM_ADMIN);
		try {
			BaseServerController.resetInfo(userModel);
			userService.insert(userModel);
		} catch (Exception e) {
			DefaultSystemLog.getLog().error("初始化用户失败", e);
			return JsonMessage.getString(400, "初始化失败");
		}
		// 自动登录
		setSessionAttribute(LoginInterceptor.SESSION_NAME, userModel);
		String jwtId = userService.getUserJwtId(userName);
		UserLoginDto userLoginDto = new UserLoginDto(JwtUtil.builder(userModel, jwtId), jwtId);
		return JsonMessage.getString(200, "初始化成功", userLoginDto);
	}
}
