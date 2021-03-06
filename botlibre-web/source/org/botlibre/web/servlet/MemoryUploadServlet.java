/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.web.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.BotException;

import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.MemoryBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/memory-upload")
@MultipartConfig
@SuppressWarnings("serial")
public class MemoryUploadServlet extends BeanServlet {
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		BotBean botBean = loginBean.getBotBean();
		MemoryBean bean = loginBean.getBean(MemoryBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				request.getRequestDispatcher("memory.jsp").forward(request, response);
				return;
			}
			botBean.checkAdmin();
			String importFormat = (String)request.getParameter("import-format");
			String encoding = (String)request.getParameter("import-encoding");
			String pin = (String)request.getParameter("pin");
			Part filePart = request.getPart("file");
			if ((filePart != null) && (filePart.getSize() > 0)) {
				if (filePart.getContentType() != null && filePart.getContentType().equals("csv")) {
					importFormat = "csv";
				}
				String fileName = getFileName(filePart);
				InputStream stream = filePart.getInputStream();
				bean.importFile(fileName, stream, importFormat, encoding, "on".equals(pin));
			} else {
				throw new BotException("Missing file");
			}
			request.getRequestDispatcher("memory.jsp").forward(request, response);
		} catch (Exception failed) {
			botBean.error(failed);
			request.getRequestDispatcher("memory.jsp").forward(request, response);
		}
	}
}
