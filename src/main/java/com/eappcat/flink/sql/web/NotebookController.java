package com.eappcat.flink.sql.web;

import com.alibaba.fastjson.JSONObject;
import com.eappcat.flink.sql.web.entity.NotebookEntity;
import com.eappcat.flink.sql.web.service.ExecuteService;
import com.eappcat.flink.sql.web.service.NoteBookService;
import com.eappcat.flink.sql.web.vo.NotebookVO;
import com.eappcat.flink.sql.web.vo.NotebooksSearchVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;


/**
 * Notebook控制器类
 * 提供前端查询接口
 * @author Xuan Yue Bo
 */
@RestController
@Slf4j
public class NotebookController {
    @Autowired
    private NoteBookService service;
    @Autowired
    private ExecuteService executeService;

    /**
     * Notebook的查询接口
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/notebooks")
    public Page<NotebookEntity> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.findPage(PageRequest.of(page, size));
    }
    /**
     * Notebook的查询接口
     * 支持时间范围查询
     * @param notebooksSearchVO 查询消息体：{"page": 0,"size": 10,"orderBy": "executeDate"}
     *
     * @return
     */
    @PostMapping("/notebooks")
    public Page<NotebookVO> list(@RequestBody NotebooksSearchVO notebooksSearchVO) throws ParseException {
        if (notebooksSearchVO.getPage() < 0) {
            notebooksSearchVO.setPage(0);
        }
        if (notebooksSearchVO.getSize() == 0) {
            notebooksSearchVO.setSize(20);
        }
        return service.findPage(notebooksSearchVO);
    }

    /**
     * 获取Notebook的内容
     * 提供给flink集群远程加载sql文件的功能
     * @param id
     * @param response
     * @throws Exception
     */
    @GetMapping("/notebook/{id}")
    public void notebookContent(@PathVariable("id") String id, HttpServletResponse response) throws Exception {
        log.info("notebook:{}", id);
        NotebookEntity notebook = service.findById(id);
        response.setHeader("Content-Disposition", "attachment;filename=job.sql");
        response.setContentType("application/octet-stream; charset=utf-8");
        response.getWriter().println(notebook.getContent());

    }
    /**
     * 获取udf相关内容
     * 提供给flink集群远程加载udf.jar文件的功能
     * @param id
     * @param response
     * @throws Exception
     */
    @GetMapping(value = "/jar/{id}/udf.jar")
    public void jar(@PathVariable("id") String id, HttpServletResponse response) throws Exception {
        log.info("jar:{}", id);
        NotebookEntity notebook = service.findById(id);
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=udf.jar");
        response.setContentType("application/java-archive");
        if (StringUtils.isNotEmpty(notebook.getUserLibPath())) {
            try (FileInputStream fileInputStream = new FileInputStream(notebook.getUserLibPath())) {
                IOUtils.copy(fileInputStream, response.getOutputStream());
            }
        } else {
            response.sendError(404, "file not found");
        }
    }

    /**
     * 添加Notebook接口
     * @param notebook
     * @return
     */
    @PostMapping("/notebook")
    public String add(@RequestBody NotebookEntity notebook) {
        return service.save(notebook).getId();
    }
    /**
     * notebook执行接口
     * @param file sql文件
     * @return
     */
    @PostMapping("/run")
    public NotebookEntity run(@RequestParam("file") MultipartFile file) throws Exception {
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".sql")) {
            throw new IllegalArgumentException("必须是sql文件");
        }
        return executeService.run(file.getInputStream());
    }
    /**
     * notebook执行接口，提供给前端用
     * @param contentMap 前端的请求参数
     * @return
     */
    @PostMapping("/run/content")
    public NotebookEntity runContent(@RequestBody Map<String, Object> contentMap) throws Exception {
        if (contentMap == null || StringUtils.isEmpty((String) contentMap.get("content"))) {
            throw new IllegalArgumentException("内容不能为空");
        }
        InputStream stream = new ByteArrayInputStream(contentMap.get("content").toString().getBytes());
        return executeService.run(stream);
    }

    /**
     * 取消整个notebook的执行
     * @param noteId 要取消的notebook的唯一标识
     * @return
     * @throws Exception
     */
    @PostMapping("/cancel")
    public String cancel(@RequestParam("id") String noteId) throws Exception {
        return executeService.cancel(noteId);
    }
    /**
     * 获取notebook的执行状态等
     * @param noteId 要取消的notebook的唯一标识
     * @return 统计结果
     * @throws Exception
     */
    @GetMapping("/statistic")
    public JSONObject stats(@RequestParam("id") String noteId) throws Exception {
        return executeService.statistic(noteId);
    }
}
