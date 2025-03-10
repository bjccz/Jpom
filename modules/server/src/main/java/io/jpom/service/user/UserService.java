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
package io.jpom.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.db.Entity;
import io.jpom.model.data.UserModel;
import io.jpom.service.h2db.BaseDbService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author bwcx_jzy
 * @since 2021/12/3
 */
@Service
public class UserService extends BaseDbService<UserModel> {

	/**
	 * 是否需要初始化
	 *
	 * @return true 有系统管理员账号，系统可以正常使用
	 */
	public boolean canUse() {
		UserModel userModel = new UserModel();
		userModel.setSystemUser(1);
		return super.exists(userModel);
	}

	@Override
	protected void fillSelectResult(UserModel data) {
		if (data == null) {
			return;
		}
		data.setSalt(null);
		data.setPassword(null);
	}

	/**
	 * 生成 随机盐值
	 *
	 * @return 随机盐值
	 */
	public synchronized String generateSalt() {
		while (true) {
			String salt = RandomUtil.randomString(UserModel.SALT_LEN);
			UserModel userModel = new UserModel();
			userModel.setSalt(salt);
			boolean exists = super.exists(userModel);
			if (exists) {
				continue;
			}
			return salt;
		}
	}

	/**
	 * 验证用户md5
	 *
	 * @param userMd5 用户md5
	 * @return userModel 用户对象
	 */
	public UserModel checkUser(String userMd5) {
		UserModel userModel = new UserModel();
		userModel.setPassword(userMd5);
		return super.queryByBean(userModel);
	}

	/**
	 * 查询用户 jwt id
	 *
	 * @param id 用户id
	 * @return jwt id
	 */
	public String getUserJwtId(String id) {
		String sql = "select password from USER_INFO where id=?";
		List<Entity> query = super.query(sql, id);
		Entity first = CollUtil.getFirst(query);
		Assert.notEmpty(first, "没有对应的用户信息");
		String password = (String) first.get("password");
		Assert.hasText(password, "没有对应的用户信息");
		return password;
	}

	/**
	 * 是否返回系统管理员信息
	 *
	 * @param hiddenSystem 隐藏系统管理员
	 * @return list
	 */
	public List<UserModel> list(boolean hiddenSystem) {
		UserModel userModel = new UserModel();
		userModel.setSystemUser(hiddenSystem ? 0 : 1);
		return super.listByBean(userModel);
	}

	/**
	 * 当前系统中的系统管理员的数量
	 *
	 * @return int
	 */
	public long systemUserCount() {
		UserModel userModel = new UserModel();
		userModel.setSystemUser(1);
		return super.count(super.dataBeanToEntity(userModel));
	}

	/**
	 * 修改密码
	 *
	 * @param id     账号ID
	 * @param newPwd 新密码
	 */
	public void updatePwd(String id, String newPwd) {
		String salt = this.generateSalt();
		UserModel userModel = new UserModel();
		userModel.setId(id);
		userModel.setSalt(salt);
		userModel.setPassword(SecureUtil.sha1(newPwd + salt));
		userModel.unLock();
		super.update(userModel);
	}

	/**
	 * 用户登录
	 *
	 * @param name 用户名
	 * @param pwd  密码
	 * @return 登录
	 */
	public UserModel simpleLogin(String name, String pwd) {
		UserModel userModel = super.getByKey(name, false);
		if (userModel == null) {
			return null;
		}
		String obj = SecureUtil.sha1(pwd + userModel.getSalt());
		if (StrUtil.equals(obj, userModel.getPassword())) {
			super.fillSelectResult(userModel);
			return userModel;
		}
		return null;
	}

	/**
	 * 重置超级管理账号密码
	 *
	 * @return 新密码
	 */
	public String restSuperUserPwd() {
		UserModel userModel = new UserModel();
		userModel.setParent(UserModel.SYSTEM_ADMIN);
		UserModel queryByBean = super.queryByBean(userModel);
		if (queryByBean == null) {
			return null;
		}
		String newPwd = RandomUtil.randomString(UserModel.SALT_LEN);
		this.updatePwd(queryByBean.getId(), SecureUtil.sha1(newPwd));
		return StrUtil.format("重置超级管理员账号密码成功,登录账号为：{} 新密码为：{}", queryByBean.getId(), newPwd);
	}
}
