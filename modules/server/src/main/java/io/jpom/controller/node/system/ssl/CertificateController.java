package io.jpom.controller.node.system.ssl;

import cn.jiangzeyin.common.JsonMessage;
import io.jpom.common.BaseServerController;
import io.jpom.common.forward.NodeForward;
import io.jpom.common.forward.NodeUrl;
import io.jpom.permission.SystemPermission;
import io.jpom.plugin.ClassFeature;
import io.jpom.plugin.Feature;
import io.jpom.plugin.MethodFeature;
import io.jpom.service.system.WhitelistDirectoryService;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 证书管理
 *
 * @author Arno
 */
@Controller
@RequestMapping(value = "/node/system/certificate")
@Feature(cls = ClassFeature.SSL)
@SystemPermission
public class CertificateController extends BaseServerController {

	private final WhitelistDirectoryService whitelistDirectoryService;

	public CertificateController(WhitelistDirectoryService whitelistDirectoryService) {
		this.whitelistDirectoryService = whitelistDirectoryService;
	}

	/**
	 * @return
	 * @author Hotstrip
	 * load Cert white list data
	 */
	@RequestMapping(value = "white-list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String loadWhiteList() {
		List<String> list = whitelistDirectoryService.getCertificateDirectory(getNode());
		return JsonMessage.getString(200, "success", list);
	}

	/**
	 * 保存证书
	 *
	 * @return json
	 */
	@RequestMapping(value = "/saveCertificate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Feature(method = MethodFeature.EDIT)
	public String saveCertificate() {
		if (ServletFileUpload.isMultipartContent(getRequest())) {
			return NodeForward.requestMultipart(getNode(), getMultiRequest(), NodeUrl.System_Certificate_saveCertificate).toString();
		}
		return NodeForward.request(getNode(), getRequest(), NodeUrl.System_Certificate_saveCertificate).toString();
	}


	/**
	 * 证书列表
	 *
	 * @return json
	 */
	@RequestMapping(value = "/getCertList", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Feature(method = MethodFeature.LIST)
	public String getCertList() {
		return NodeForward.request(getNode(), getRequest(), NodeUrl.System_Certificate_getCertList).toString();
	}

	/**
	 * 删除证书
	 *
	 * @return json
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Feature(method = MethodFeature.DEL)
	public String delete() {
		return NodeForward.request(getNode(), getRequest(), NodeUrl.System_Certificate_delete).toString();
	}


	/**
	 * 导出证书
	 */
	@RequestMapping(value = "/export", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Feature(method = MethodFeature.DOWNLOAD)
	public void export() {
		NodeForward.requestDownload(getNode(), getRequest(), getResponse(), NodeUrl.System_Certificate_export);
	}
}
